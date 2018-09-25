/*
 * The MIT License
 * Copyright © 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.bonitoo.platform.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.core.GzipRequestInterceptor;
import io.bonitoo.core.InfluxException;
import io.bonitoo.core.Preconditions;
import io.bonitoo.core.event.AbstractInfluxEvent;
import io.bonitoo.core.event.UnhandledErrorEvent;
import io.bonitoo.platform.WriteClient;
import io.bonitoo.platform.event.BackpressureEvent;
import io.bonitoo.platform.event.WriteSuccessEvent;
import io.bonitoo.platform.option.WriteOptions;

import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.flowables.GroupedFlowable;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.RequestBody;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 11:02)
 */
final class WriteClientImpl extends AbstractRestClient implements WriteClient {

    private static final Logger LOG = Logger.getLogger(WriteClientImpl.class.getName());

    private static final TimeUnit WRITE_PRECISION = NANOSECONDS;
    private static final EnumSet<TimeUnit> ALLOWED_PRECISION = EnumSet.of(TimeUnit.NANOSECONDS,
            TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    private final PlatformService platformService;
    private final GzipRequestInterceptor interceptor;
    private final WriteOptions writeOptions;

    private final PublishProcessor<BatchWrite> processor;
    private final PublishSubject<AbstractInfluxEvent> eventPublisher;

    WriteClientImpl(@Nonnull final WriteOptions writeOptions,
                    @Nonnull final PlatformService platformService,
                    @Nonnull final GzipRequestInterceptor interceptor) {

        this(writeOptions, platformService, interceptor,
                Schedulers.newThread(),
                Schedulers.computation(),
                Schedulers.trampoline(),
                Schedulers.trampoline());
    }

    WriteClientImpl(@Nonnull final WriteOptions writeOptions,
                    @Nonnull final PlatformService platformService,
                    @Nonnull final GzipRequestInterceptor interceptor,
                    @Nonnull final Scheduler processorScheduler,
                    @Nonnull final Scheduler batchScheduler,
                    @Nonnull final Scheduler jitterScheduler,
                    @Nonnull final Scheduler retryScheduler) {

        this.platformService = platformService;
        this.interceptor = interceptor;
        this.writeOptions = writeOptions;

        this.eventPublisher = PublishSubject.create();
        this.processor = PublishProcessor.create();
        this.processor
                //
                // Backpressure
                //
                .onBackpressureBuffer(
                        writeOptions.getBufferLimit(),
                        () -> publish(new BackpressureEvent()),
                        writeOptions.getBackpressureStrategy())
                .observeOn(processorScheduler)
                //
                // Batching
                //
                .window(writeOptions.getFlushInterval(),
                        TimeUnit.MILLISECONDS,
                        batchScheduler,
                        writeOptions.getBatchSize(),
                        true)
                //
                // Group by key - same bucket, same org
                //
                .concatMap(it -> it.groupBy(batchWrite -> batchWrite.batchWriteOptions))
                //
                // Create Write Point = bucket, org, ... + data
                //
                .concatMapSingle((Function<GroupedFlowable<BatchWriteOptions, BatchWrite>, SingleSource<BatchWrite>>)
                        grouped -> {

                            //
                            // Create Line Protocol
                            //
                            Single<String> reduce = grouped
                                    .reduce("", (lineProtocol, batchWrite) -> {
                                        if (lineProtocol.isEmpty()) {
                                            return batchWrite.lineProtocol;
                                        }
                                        return String.join("\n", lineProtocol, batchWrite.lineProtocol);
                                    });

                            return Single.just(grouped.getKey()).zipWith(reduce, BatchWrite::new);
                        })
                //
                // Jitter interval
                //
                .compose(jitter(jitterScheduler))
                //
                // To WritePoints "request creator"
                //
                .concatMapCompletable(new ToWritePointsCompletable(retryScheduler))
                //
                // Publish Error event
                //
                .doOnError(throwable -> publish(new UnhandledErrorEvent(InfluxException.fromCause(throwable))))
                .subscribe();
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nonnull final List<String> records) {


        write(bucket, organization, token, WRITE_PRECISION, records);
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nonnull final TimeUnit precision,
                      @Nonnull final List<String> records) {

        Objects.requireNonNull(records, "records are required");

        records.forEach(record -> write(bucket, organization, token, precision, record));
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nullable final String record) {

        write(bucket, organization, token, WRITE_PRECISION, record);
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nonnull final TimeUnit precision,
                      @Nullable final String record) {


        Preconditions.checkNonEmptyString(bucket, "bucket");
        Preconditions.checkNonEmptyString(organization, "organization");
        Preconditions.checkNonEmptyString(token, "token");
        Objects.requireNonNull(precision, "TimeUnit.precision is required");

        if (!ALLOWED_PRECISION.contains(precision)) {
            throw new IllegalArgumentException("Precision must be one of: " + ALLOWED_PRECISION);
        }

        if (record == null || record.isEmpty()) {
            return;
        }

        BatchWriteOptions batchWriteOptions = new BatchWriteOptions(bucket, organization, token, precision);
        BatchWrite batchWrite = new BatchWrite(batchWriteOptions, record);
        processor.onNext(batchWrite);
    }

    @Nonnull
    @Override
    public <T extends AbstractInfluxEvent> Observable<T> listenEvents(@Nonnull final Class<T> eventType) {

        Objects.requireNonNull(eventType, "EventType is required");

        return eventPublisher.ofType(eventType);
    }

    @Nonnull
    @Override
    public WriteClient enableGzip() {
        interceptor.enable();
        return this;
    }

    @Nonnull
    @Override
    public WriteClient disableGzip() {
        interceptor.disable();
        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return interceptor.isEnabled();
    }

    @Nonnull
    @Override
    public WriteClient close() {

        LOG.log(Level.INFO, "Flushing any cached BatchWrites before shutdown.");

        processor.onComplete();
        eventPublisher.onComplete();

        return this;
    }

    @Nonnull
    private FlowableTransformer<BatchWrite, BatchWrite> jitter(@Nonnull final Scheduler scheduler) {

        Objects.requireNonNull(scheduler, "Jitter scheduler is required");

        return source -> {

            //
            // source without jitter
            //
            if (writeOptions.getJitterInterval() <= 0) {
                return source;
            }

            //
            // Add jitter => dynamic delay
            //
            return source.delay((Function<BatchWrite, Flowable<Long>>) pointFlowable -> {

                int delay = jitterDelay();

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
            });
        };
    }

    private int jitterDelay() {

        return (int) (Math.random() * writeOptions.getJitterInterval());
    }

    private <T extends AbstractInfluxEvent> void publish(@Nonnull final T event) {

        Objects.requireNonNull(event, "Event is required");

        event.logEvent();
        eventPublisher.onNext(event);
    }

    @Nonnull
    private String toPrecisionParameter(@Nonnull final TimeUnit precision) {

        switch (precision) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "us";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            default:
                throw new IllegalArgumentException("Precision must be one of: " + ALLOWED_PRECISION);
        }
    }

    /**
     * The Batch Write.
     */
    private final class BatchWrite {

        private BatchWriteOptions batchWriteOptions;
        private String lineProtocol;

        private BatchWrite(@Nonnull final BatchWriteOptions batchWriteOptions, @Nonnull final String lineProtocol) {

            Objects.requireNonNull(batchWriteOptions, "BatchWriteOptions is required");
            Preconditions.checkNonEmptyString(lineProtocol, "lineProtocol");

            this.batchWriteOptions = batchWriteOptions;
            this.lineProtocol = lineProtocol;
        }
    }

    /**
     * The options to apply to a @{@link BatchWrite}.
     */
    private final class BatchWriteOptions {

        private String bucket;
        private String organization;
        private String token;
        private TimeUnit precision;

        private BatchWriteOptions(@Nonnull final String bucket,
                                  @Nonnull final String organization,
                                  @Nonnull final String token,
                                  @Nonnull final TimeUnit precision) {

            Preconditions.checkNonEmptyString(bucket, "bucket");
            Preconditions.checkNonEmptyString(organization, "organization");
            Preconditions.checkNonEmptyString(token, "token");
            Objects.requireNonNull(precision, "TimeUnit.precision is required");

            this.bucket = bucket;
            this.organization = organization;
            this.token = token;
            this.precision = precision;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BatchWriteOptions)) {
                return false;
            }
            BatchWriteOptions batchWriteOptions = (BatchWriteOptions) o;
            return Objects.equals(bucket, batchWriteOptions.bucket)
                    && Objects.equals(organization, batchWriteOptions.organization)
                    && Objects.equals(token, batchWriteOptions.token)
                    && precision == batchWriteOptions.precision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(bucket, organization, token, precision);
        }
    }

    private final class ToWritePointsCompletable implements Function<BatchWrite, CompletableSource> {

        private final Scheduler retryScheduler;

        private ToWritePointsCompletable(@Nonnull final Scheduler retryScheduler) {
            this.retryScheduler = retryScheduler;
        }

        @Override
        public CompletableSource apply(final BatchWrite batchWrite) {

            //
            // InfluxDB Line Protocol => to Request Body
            //
            RequestBody requestBody = createBody(batchWrite.lineProtocol);

            //
            // Parameters
            String organization = batchWrite.batchWriteOptions.organization;
            String bucket = batchWrite.batchWriteOptions.bucket;
            String precision = WriteClientImpl.this.toPrecisionParameter(batchWrite.batchWriteOptions.precision);
            String token = "Token " + batchWrite.batchWriteOptions.token;

            return platformService
                    .writePoints(organization, bucket, precision, token, requestBody)
                    .doOnComplete(() -> publish(toSuccessEvent(batchWrite)));
        }

        @Nonnull
        private WriteSuccessEvent toSuccessEvent(@Nonnull final BatchWrite batchWrite) {

            return new WriteSuccessEvent(
                    batchWrite.batchWriteOptions.organization,
                    batchWrite.batchWriteOptions.bucket,
                    batchWrite.batchWriteOptions.precision,
                    batchWrite.batchWriteOptions.token,
                    batchWrite.lineProtocol);
        }
    }

//    TODO implement retry, delete retry scheduler?
//    /**
//     * The retry handler that tries to retry a write if it failed previously and
//     * the reason of the failure is not permanent.
//     *
//     * @param retryScheduler for scheduling retry write
//     * @param writeOptions   options for write to Platform
//     * @return the retry handler
//     */
//    @Nonnull
//    private Function<Flowable<Throwable>, Publisher<?>> retryHandler(@Nonnull final Scheduler retryScheduler,
//                                                                     @Nonnull final WriteOptions writeOptions) {
//
//        Objects.requireNonNull(writeOptions, "WriteOptions are required");
//        Objects.requireNonNull(retryScheduler, "RetryScheduler is required");
//
//        return errors -> errors.flatMap(throwable -> {
//
//            if (throwable instanceof HttpException) {
//
//                InfluxException influxException = InfluxException.fromCause(throwable);
//
//                //
//                // Partial Write => skip retry
//                //
//                if (influxException.getMessage().startsWith("partial write")) {
//                    publish("WritePartialEvent");
//
//                    return Flowable.error(throwable);
//                }
//
//                publish("WriteErrorEvent");
//
//                //
//                // Retry request
//                //
//                if (influxException.isRetryWorth()) {
//
//                    int retryInterval = writeOptions.getRetryInterval() + jitterDelay();
//
//                    return Flowable.just("notify").delay(retryInterval, TimeUnit.MILLISECONDS, retryScheduler);
//                }
//            }
//
//            //
//            // This type of throwable is not able to retry
//            //
//            return Flowable.error(throwable);
//        });
//    }
}