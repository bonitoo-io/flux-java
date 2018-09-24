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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.GzipRequestInterceptor;
import io.bonitoo.InfluxException;
import io.bonitoo.Preconditions;
import io.bonitoo.flux.events.UnhandledErrorEvent;
import io.bonitoo.platform.WriteClient;
import io.bonitoo.platform.options.WriteOptions;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import org.reactivestreams.Publisher;
import retrofit2.HttpException;

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

    private final PublishProcessor<WriteData> processor;

    WriteClientImpl(@Nonnull final WriteOptions writeOptions,
                    @Nonnull final PlatformService platformService,
                    @Nonnull final GzipRequestInterceptor interceptor) {

        this(writeOptions, platformService, interceptor,
                Schedulers.newThread(),
                Schedulers.computation(),
                Schedulers.trampoline(),
                Schedulers.trampoline());
    }

    protected WriteClientImpl(@Nonnull final WriteOptions writeOptions,
                              @Nonnull final PlatformService platformService,
                              @Nonnull final GzipRequestInterceptor interceptor,
                              @Nonnull final Scheduler processorScheduler,
                              @Nonnull final Scheduler batchScheduler,
                              @Nonnull final Scheduler jitterScheduler,
                              @Nonnull final Scheduler retryScheduler) {

        this.platformService = platformService;
        this.interceptor = interceptor;
        this.writeOptions = writeOptions;

        this.processor = PublishProcessor.create();
        this.processor
                //
                // Backpressure
                //
                .onBackpressureBuffer(
                        writeOptions.getBufferLimit(),
                        () -> publish("BackpressureEvent"),
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
                // Jitter interval
                //
                .compose(jitter(jitterScheduler))
                .doOnError(throwable -> publish(new UnhandledErrorEvent(throwable)))
                .subscribe(new WritePointsConsumer(retryScheduler));
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

        write(bucket, organization, token, precision, String.join("\n", records));
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nullable final String records) {

        write(bucket, organization, token, WRITE_PRECISION, records);
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nonnull final TimeUnit precision,
                      @Nullable final String records) {


        Preconditions.checkNonEmptyString(bucket, "bucket");
        Preconditions.checkNonEmptyString(organization, "organization");
        Preconditions.checkNonEmptyString(token, "token");
        Objects.requireNonNull(precision, "TimeUnit.precision is required");

        if (!ALLOWED_PRECISION.contains(precision)) {
            throw new IllegalArgumentException("Precision must be one of:" + ALLOWED_PRECISION);
        }

        if (records == null || records.isEmpty()) {
            return;
        }

        WriteData writeData = new WriteData();
        writeData.writeKey = new WriteKey(bucket, organization, token, precision);
        writeData.lineProtocol = records;
        processor.onNext(writeData);
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

        LOG.log(Level.INFO, "Flushing any cached metrics before shutdown.");

        processor.onComplete();

        return this;
    }

    @Nonnull
    private FlowableTransformer<Flowable<WriteData>, Flowable<WriteData>> jitter(
            @Nonnull final Scheduler scheduler) {

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
            return source.delay((Function<Flowable<WriteData>, Flowable<Long>>) pointFlowable -> {

                int delay = jitterDelay();

                LOG.log(Level.FINEST, "Generated Jitter dynamic delay: {0}", delay);

                return Flowable.timer(delay, TimeUnit.MILLISECONDS, scheduler);
            });
        };
    }

    private int jitterDelay() {

        return (int) (Math.random() * writeOptions.getJitterInterval());
    }

    private void publish(@Nonnull final Object event) {

        Objects.requireNonNull(event, "Event is required");

        //TODO are the events necessary?
        LOG.info("Event: " + event);
    }

    private final class WritePointsConsumer implements Consumer<Flowable<WriteData>> {

        private final Scheduler retryScheduler;

        private WritePointsConsumer(@Nonnull final Scheduler retryScheduler) {

            Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

            this.retryScheduler = retryScheduler;
        }

        @Override
        public void accept(final Flowable<WriteData> flowablePoints) {

            flowablePoints
                    //
                    // Group by key - same bucket, same org
                    //
                    .groupBy(writeData -> writeData.writeKey)
                    .subscribeOn(writeOptions.getWriteScheduler())
                    .subscribe(group -> {

                        WriteKey writeKey = group.getKey();
                        group
                                .toList()
                                .filter(dataPoints -> !dataPoints.isEmpty())
                                .subscribe(
                                        dataPoints -> writeDataPoints(writeKey, dataPoints),
                                        throwable -> publish(new UnhandledErrorEvent(throwable)));
                    }, throwable -> publish(new UnhandledErrorEvent(throwable)));

        }

        private void writeDataPoints(@Nonnull final WriteKey writeKey,
                                     @Nonnull final List<WriteData> dataPoints) {

            Objects.requireNonNull(writeOptions, "WriteOptions are required");
            Objects.requireNonNull(dataPoints, "DatePoints are required");


            //
            // Fail action
            //
            Consumer<Throwable> fail = throwable -> {

                // HttpException is handled in retryHandler
                if (throwable instanceof HttpException) {
                    return;
                }

                publish(new UnhandledErrorEvent(throwable));
            };

            //
            // Data => InfluxDB Line Protocol
            //

            String body = dataPoints.stream()
                    .map(data -> data.lineProtocol)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));

            if (body.isEmpty()) {

                String message = "The points {0} are parsed to empty request body => skip call InfluxDB server.";

                LOG.log(Level.FINE, message, dataPoints);

                return;
            }

            //
            // InfluxDB Line Protocol => to Request Body
            //
            RequestBody requestBody = createBody(body);

            //
            // Parameters
            Completable completable = platformService.writePoints(
                    writeKey.organization, writeKey.bucket, toPrecisionParameter(writeKey.precision),
                    "Token " + writeKey.token, requestBody)
                    //
                    // Retry strategy
                    //
                    .retryWhen(retryHandler(retryScheduler, writeOptions));


            completable.subscribe(() -> publish("Success"), fail);
        }
    }

    /**
     * The retry handler that tries to retry a write if it failed previously and
     * the reason of the failure is not permanent.
     *
     * @param retryScheduler for scheduling retry write
     * @param writeOptions   options for write to Platform
     * @return the retry handler
     */
    @Nonnull
    private Function<Flowable<Throwable>, Publisher<?>> retryHandler(@Nonnull final Scheduler retryScheduler,
                                                                     @Nonnull final WriteOptions writeOptions) {

        Objects.requireNonNull(writeOptions, "WriteOptions are required");
        Objects.requireNonNull(retryScheduler, "RetryScheduler is required");

        return errors -> errors.flatMap(throwable -> {

            if (throwable instanceof HttpException) {

                InfluxException influxDBException = InfluxException.fromCause(throwable);

                //
                // Partial Write => skip retry
                //
                if (influxDBException.getMessage().startsWith("partial write")) {
                    publish("WritePartialEvent");

                    return Flowable.error(throwable);
                }

                publish("WriteErrorEvent");

                //
                // Retry request
                //
                //TODO retry
                if (false) {

                    int retryInterval = writeOptions.getRetryInterval() + jitterDelay();

                    return Flowable.just("notify").delay(retryInterval, TimeUnit.MILLISECONDS, retryScheduler);
                }
            }

            //
            // This type of throwable is not able to retry
            //
            return Flowable.error(throwable);
        });
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
                throw new IllegalArgumentException("Precision must be one of:" + ALLOWED_PRECISION);
        }
    }

    private class WriteData {
        private WriteKey writeKey;
        private String lineProtocol;
    }

    private class WriteKey {
        private String bucket;
        private String organization;
        private String token;
        private TimeUnit precision;

        WriteKey(@Nonnull final String bucket,
                 @Nonnull final String organization,
                 @Nonnull final String token,
                 @Nonnull final TimeUnit precision) {

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
            if (!(o instanceof WriteKey)) {
                return false;
            }
            WriteKey writeKey = (WriteKey) o;
            return Objects.equals(bucket, writeKey.bucket)
                    && Objects.equals(organization, writeKey.organization)
                    && Objects.equals(token, writeKey.token)
                    && precision == writeKey.precision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(bucket, organization, token, precision);
        }
    }
}