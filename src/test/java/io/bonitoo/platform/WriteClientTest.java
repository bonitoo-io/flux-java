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
package io.bonitoo.platform;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import io.bonitoo.core.GzipRequestInterceptor;
import io.bonitoo.core.InfluxException;
import io.bonitoo.core.event.UnhandledErrorEvent;
import io.bonitoo.platform.event.BackpressureEvent;
import io.bonitoo.platform.event.WriteSuccessEvent;
import io.bonitoo.platform.impl.AbstractPlatformClientTest;
import io.bonitoo.platform.option.WriteOptions;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 11:36)
 */
@RunWith(JUnitPlatform.class)
class WriteClientTest extends AbstractPlatformClientTest {

    private WriteClient writeClient;
    private TestScheduler batchScheduler;
    private TestScheduler jitterScheduler;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        batchScheduler = new TestScheduler();
        jitterScheduler = new TestScheduler();
    }

    @AfterEach
    void tearDown() {
        if (writeClient != null) {
            writeClient.close();
        }
    }

    @Test
    void gzip() {

        writeClient = createWriteClient();

        Assertions.assertThat(writeClient.isGzipEnabled()).isFalse();

        // Enable GZIP
        writeClient.enableGzip();
        Assertions.assertThat(writeClient.isGzipEnabled()).isTrue();

        // Disable GZIP
        writeClient.disableGzip();
        Assertions.assertThat(writeClient.isGzipEnabled()).isFalse();
    }

    @Test
    void requestParameters() throws InterruptedException {

        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);

        writeClient.write("b1", "org1", "token1",
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        RecordedRequest request = platformServer.takeRequest(10L, TimeUnit.SECONDS);

        // token
        Assertions.assertThat(request.getHeader("Authorization")).isEqualTo("Token token1");
        // organization
        Assertions.assertThat(request.getRequestUrl().queryParameter("org")).isEqualTo("org1");
        // bucket
        Assertions.assertThat(request.getRequestUrl().queryParameter("bucket")).isEqualTo("b1");
        // precision
        Assertions.assertThat(request.getRequestUrl().queryParameter("precision")).isEqualTo("ns");
    }

    @Test
    void emptyRequest() {

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);
        writeClient.write("b1", "org1", "token1", Lists.emptyList());
        writeClient.write("b1", "org1", "token1", (String) null);
        writeClient.write("b1", "org1", "token1", "");

        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(0);
    }

    @Test
    void precision() throws InterruptedException {

        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        writeClient.write("b1", "org1", "token1", TimeUnit.NANOSECONDS, record1);

        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        writeClient.write("b1", "org1", "token1", TimeUnit.MICROSECONDS, record2);

        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        writeClient.write("b1", "org1", "token1", TimeUnit.MILLISECONDS, record3);

        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";
        writeClient.write("b1", "org1", "token1", TimeUnit.SECONDS, record4);

        RecordedRequest request1 = platformServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request1.getRequestUrl().queryParameter("precision")).isEqualTo("ns");

        RecordedRequest request2 = platformServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request2.getRequestUrl().queryParameter("precision")).isEqualTo("us");

        RecordedRequest request3 = platformServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request3.getRequestUrl().queryParameter("precision")).isEqualTo("ms");

        RecordedRequest request4 = platformServer.takeRequest(10L, TimeUnit.SECONDS);
        Assertions.assertThat(request4.getRequestUrl().queryParameter("precision")).isEqualTo("s");

        Assertions.assertThatThrownBy(() ->
                writeClient.write("b1", "org1", "token1", TimeUnit.MINUTES, record1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS]");

        Assertions.assertThatThrownBy(() ->
                writeClient.write("b1", "org1", "token1", TimeUnit.HOURS, record1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS]");

        Assertions.assertThatThrownBy(() ->
                writeClient.write("b1", "org1", "token1", TimeUnit.DAYS, record1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precision must be one of: [NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS]");
    }

    @Test
    void batching() {

        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.builder().batchSize(2).build());

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        writeClient.write("b1", "org1", "token1", record1);
        writeClient.write("b1", "org1", "token1", record2);
        writeClient.write("b1", "org1", "token1", record3);
        writeClient.write("b1", "org1", "token1", record4);

        String body1 = getRequestBody(platformServer);
        Assertions.assertThat(body1).isEqualTo(record1 + "\n" + record2);

        String body2 = getRequestBody(platformServer);
        Assertions.assertThat(body2).isEqualTo(record3 + "\n" + record4);
    }

    @Test
    void batchingDisabled() {

        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        writeClient.write("b1", "org1", "token1", record1);
        writeClient.write("b1", "org1", "token1", record2);
        writeClient.write("b1", "org1", "token1", record3);
        writeClient.write("b1", "org1", "token1", record4);

        String body1 = getRequestBody(platformServer);
        Assertions.assertThat(body1).isEqualTo(record1);

        String body2 = getRequestBody(platformServer);
        Assertions.assertThat(body2).isEqualTo(record2);

        String body3 = getRequestBody(platformServer);
        Assertions.assertThat(body3).isEqualTo(record3);

        String body4 = getRequestBody(platformServer);
        Assertions.assertThat(body4).isEqualTo(record4);
    }

    @Test
    void listAsMoreBatchUnits() {

        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));
        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);

        String record1 = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
        String record2 = "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 2";
        String record3 = "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 3";
        String record4 = "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 4";

        List<String> records = Lists.list(record1, record2, record3, record4);
        writeClient.write("b1", "org1", "token1", records);

        String body1 = getRequestBody(platformServer);
        Assertions.assertThat(body1).isEqualTo(record1);

        String body2 = getRequestBody(platformServer);
        Assertions.assertThat(body2).isEqualTo(record2);

        String body3 = getRequestBody(platformServer);
        Assertions.assertThat(body3).isEqualTo(record3);

        String body4 = getRequestBody(platformServer);
        Assertions.assertThat(body4).isEqualTo(record4);
    }

    @Test
    void flushByDuration() {

        platformServer.enqueue(createResponse("{}"));

        WriteOptions writeOptions = WriteOptions.disabled()
                .batchSize(10)
                .flushInterval(1_000_000)
                .writeScheduler(Schedulers.trampoline())
                .build();

        writeClient = createWriteClient(writeOptions);

        writeClient.write("b1", "org1", "token1",
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(0);

        batchScheduler.advanceTimeBy(1_000, TimeUnit.SECONDS);

        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void jitterInterval() {

        platformServer.enqueue(createResponse("{}"));

        // after 5 batchSize or 10 seconds + 5 seconds jitter interval
        WriteOptions writeOptions = WriteOptions.disabled()
                .batchSize(5)
                .flushInterval(10_000)
                .jitterInterval(5_000)
                .writeScheduler(Schedulers.trampoline())
                .build();

        writeClient = createWriteClient(writeOptions);

        writeClient.write("b1", "org1", "token1",
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // move time to feature by 10 seconds - flush interval elapsed
        batchScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        // without call remote api
        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(0);

        // move time to feature by 5 seconds - jitter interval elapsed
        jitterScheduler.advanceTimeBy(6, TimeUnit.SECONDS);

        // was call remote API
        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void flushBeforeClose() {

        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient();

        writeClient.write("b1", "org1", "token1",
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(0);

        writeClient.close();

        // wait for request
        getRequestBody(platformServer);

        Assertions.assertThat(platformServer.getRequestCount()).isEqualTo(1);
    }

    @Test
    void eventWriteSuccessEvent() {

        platformServer.enqueue(createResponse("{}"));

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);
        TestObserver<WriteSuccessEvent> listener = writeClient.listenEvents(WriteSuccessEvent.class).test();

        writeClient.write("b1", "org1", "token1",
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(platformServer);

        listener
                .assertValue(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getBucket()).isEqualTo("b1");
                    Assertions.assertThat(event.getOrganization()).isEqualTo("org1");
                    Assertions.assertThat(event.getToken()).isEqualTo("token1");
                    Assertions.assertThat(event.getLineProtocol()).isEqualTo("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

                    return true;
                })
                .assertSubscribed()
                .assertNotComplete();
    }

    @Test
    void eventUnhandledErrorEvent() {

        platformServer.enqueue(createErrorResponse("Failed to find bucket"));

        writeClient = createWriteClient(WriteOptions.DISABLED_BATCHING);
        TestObserver<UnhandledErrorEvent> listener = writeClient.listenEvents(UnhandledErrorEvent.class).test();

        writeClient.write("b1", "org1", "token1",
                "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1");

        // wait for request
        getRequestBody(platformServer);

        listener
                .assertValue(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getThrowable()).isNotNull();
                    Assertions.assertThat(event.getThrowable())
                            .isInstanceOf(InfluxException.class)
                            .hasMessage("Failed to find bucket");

                    return true;
                })
                .assertSubscribed()
                .assertNotComplete();
    }

    @Test
    void eventBackpressureEvent() {

        platformServer.enqueue(new MockResponse().setBodyDelay(1, TimeUnit.SECONDS));

        writeClient = platformClient.createWriteClient(WriteOptions.builder().bufferLimit(1).build());

        TestObserver<BackpressureEvent> listener = writeClient
                .listenEvents(BackpressureEvent.class)
                .test();

        Flowable
                .range(0, 1005)
                .map(index -> String.format("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 %s", index))
                .subscribeOn(Schedulers.newThread())
                .subscribe(record -> writeClient.write("b1", "org1", "token1", record));

        listener
                .awaitCount(1)
                .assertValueAt(0, event -> {
                    Assertions.assertThat(event).isNotNull();
                    return true;
                });

    }

    @Nonnull
    private WriteClient createWriteClient() {
        return createWriteClient(WriteOptions.DEFAULTS);
    }

    @Nonnull
    private WriteClient createWriteClient(WriteOptions writeOptions) {
        return createWriteClient(writeOptions, new GzipRequestInterceptor(), batchScheduler, jitterScheduler, new TestScheduler());
    }
}