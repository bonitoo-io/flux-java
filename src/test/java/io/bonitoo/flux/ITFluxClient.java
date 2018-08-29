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
package io.bonitoo.flux;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import io.bonitoo.flux.dto.FluxColumn;
import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.dto.FluxTable;
import io.bonitoo.flux.operators.restriction.Restrictions;
import io.bonitoo.flux.options.FluxDialect;
import io.bonitoo.flux.options.FluxOptions;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:30)
 */
@RunWith(JUnitPlatform.class)
class ITFluxClient extends AbstractITFluxClient {

    private static final Logger LOG = Logger.getLogger(ITFluxClient.class.getName());

    @BeforeEach
    void prepareDate() {
        Point point1 = Point.measurement("mem")
                .tag("host", "A").tag("region", "west")
                .addField("free", 10)
                .time(10, TimeUnit.SECONDS)
                .build();
        Point point2 = Point.measurement("mem")
                .tag("host", "A").tag("region", "west")
                .addField("free", 11)
                .time(20, TimeUnit.SECONDS)
                .build();

        Point point3 = Point.measurement("mem")
                .tag("host", "B").tag("region", "west")
                .addField("free", 20)
                .time(10, TimeUnit.SECONDS)
                .build();
        Point point4 = Point.measurement("mem")
                .tag("host", "B").tag("region", "west")
                .addField("free", 22)
                .time(20, TimeUnit.SECONDS)
                .build();

        Point point5 = Point.measurement("cpu")
                .tag("host", "A").tag("region", "west")
                .addField("user_usage", 45)
                .addField("usage_system", 35)
                .time(10, TimeUnit.SECONDS)
                .build();
        Point point6 = Point.measurement("cpu")
                .tag("host", "A").tag("region", "west")
                .addField("user_usage", 49)
                .addField("usage_system", 38)
                .time(20, TimeUnit.SECONDS)
                .build();

        Point point7 = Point.measurement("cpu")
                .tag("host", "A")
                .tag("region", "west")
                .tag("hyper-threading", "true")
                .addField("user_usage", 49)
                .addField("usage_system", 38)
                .time(20, TimeUnit.SECONDS)
                .build();

        influxDB.write(point1);
        influxDB.write(point2);
        influxDB.write(point3);
        influxDB.write(point4);
        influxDB.write(point5);
        influxDB.write(point6);
        influxDB.write(point7);
    }

    @Test
    void chunkedOneTable() {

        prepareChunkRecords();

        Flux flux = Flux
                .from(DATABASE_NAME)
                .filter(Restrictions.measurement().equal("chunked"))
                .range().withStart(Instant.EPOCH);

        fluxClient.flux(flux, fluxRecord -> {

            // +1 record
            countDownLatch.countDown();

            if (countDownLatch.getCount() % 100_000 == 0) {
                LOG.info(String.format("Remaining parsed: %s records", countDownLatch.getCount()));
            }
        });

        waitToCallback(30);
    }

    @Test
    void chunkedMoreTables() {

        prepareChunkRecords();

        Flux flux = Flux
                .from(DATABASE_NAME)
                .filter(Restrictions.measurement().equal("chunked"))
                .range()
                .withStart(Instant.EPOCH)
                .window()
                .withEvery("10m");

        fluxClient.flux(flux, fluxRecord -> {

            // +1 record
            countDownLatch.countDown();

            if (countDownLatch.getCount() % 100_000 == 0) {
                LOG.info(String.format("Remaining parsed: %s records", countDownLatch.getCount()));
            }
        });

        waitToCallback(30);
    }

    @Test
    void chunkedCancel() {

        prepareChunkRecords();

        Flux flux = Flux
                .from(DATABASE_NAME)
                .filter(Restrictions.measurement().equal("chunked"))
                .range()
                .withStart(Instant.EPOCH)
                .window()
                .withEvery("10m");

        countDownLatch = new CountDownLatch(10_000);
        CountDownLatch cancelCountDown = new CountDownLatch(1);

        FluxClient.Cancellable cancellable = fluxClient.flux(flux, fluxRecord -> {

            // +1 record
            countDownLatch.countDown();

            if (countDownLatch.getCount() % 1_000 == 0 && this.countDownLatch.getCount() != 0) {
                LOG.info(String.format("Remaining parsed: %s records", this.countDownLatch.getCount()));
            }
        }, success -> {
            Assertions.assertThat(success).isFalse();
            cancelCountDown.countDown();
        });

        // Not canceled
        Assertions.assertThat(cancellable.isCancelled()).isFalse();

        // After ten Records cancel
        waitToCallback(30);
        cancellable.cancel();

        // wait to cancel callback
        waitToCallback(cancelCountDown, 1);

        // Canceled
        Assertions.assertThat(cancellable.isCancelled()).isTrue();
    }

    @Test
    void query() {

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        List<FluxTable> fluxTables = fluxClient.flux(flux);

        assertFluxResult(fluxTables);
    }

    @Test
    void queryDifferentSchemas() {

        Flux flux = Flux
                .from(DATABASE_NAME)
                .range()
                .withStart(Instant.EPOCH);

        List<FluxTable> fluxTables = fluxClient.flux(flux);

        Assertions.assertThat(fluxTables).hasSize(6);
    }

    //TODO GZIP
    @Test
    @DisabledIfSystemProperty(named = "FLUX_DISABLE", matches = "true")
    void queryGZIP() {

        fluxClient.enableGzip();
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(true);

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        List<FluxTable> fluxTables = fluxClient.flux(flux);

        assertFluxResult(fluxTables);
    }

    @Test
    void errorWithStatusOK() {

        Assertions.assertThatThrownBy(() -> fluxClient.flux(Flux.from(DATABASE_NAME)))
                .isInstanceOf(FluxException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time");
    }

    @Test
    void callback() {

        countDownLatch = new CountDownLatch(3);
        List<FluxRecord> records = new ArrayList<>();

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        fluxClient.flux(flux, record -> {

            records.add(record);

            countDownLatch.countDown();
        }, success -> {
            Assertions.assertThat(success).isTrue();
            countDownLatch.countDown();
        });

        waitToCallback();
        assertFluxRecords(records);
    }

    // TODO ping
    @Test
    @DisabledIfSystemProperty(named = "FLUX_DISABLE", matches = "true")
    void ping() {

        Assertions.assertThat(fluxClient.ping()).isTrue();
    }

    @Test
    @DisabledIfSystemProperty(named = "FLUX_DISABLE", matches = "true")
    void pingGZIP() {

        fluxClient.enableGzip();
        Assertions.assertThat(fluxClient.isGzipEnabled()).isTrue();

        Assertions.assertThat(fluxClient.ping()).isTrue();
    }

    @Test
    void dialect() throws IOException {

        Point point = Point.measurement("cpu_dialect")
                .tag("region", "we!st")
                .addField("usage_system", 38)
                .time(20, TimeUnit.SECONDS)
                .build();

        influxDB.write(point);

        Flux flux = Flux.from(DATABASE_NAME).filter(Restrictions.and(Restrictions.measurement().equal("cpu_dialect"))).last();

        FluxDialect fluxDialect = FluxDialect.builder()
                .commentPrefix("=")
                .delimiter("!")
                .quoteChar("'")
                .addAnnotation("datatype")
                .build();

        FluxOptions options = FluxOptions.builder().dialect(fluxDialect).build();
        Response<ResponseBody> response = fluxClient.fluxRaw(flux, options);

        Assertions.assertThat(response.isSuccessful()).isTrue();

        BufferedSource bufferedSource = response.body().source();

        // commentPrefix
        String line = bufferedSource.readUtf8Line();
        // TODO comment prefix not works
        // Assertions.assertThat(line).startsWith("=");

        // delimiter
        line = bufferedSource.readUtf8Line();
        Assertions.assertThat(line).startsWith("!");

        // quoteChar
        line = bufferedSource.readUtf8Line();
        // TODO quoteChar not works
        // Assertions.assertThat(line).endsWith("'we!st'");
    }

    private void assertFluxResult(@Nonnull final List<FluxTable> tables) {

        Assertions.assertThat(tables).isNotNull();

        Assertions.assertThat(tables).hasSize(2);

        FluxTable table1 = tables.get(0);
        // Data types
        Assertions.assertThat(table1.getColumns()).hasSize(10);
        Assertions.assertThat(table1.getColumns().stream().map(FluxColumn::getDataType))
                .containsExactlyInAnyOrder("string", "long", "dateTime:RFC3339", "dateTime:RFC3339", "dateTime:RFC3339", "long", "string", "string", "string", "string");

        // Columns
        Assertions.assertThat(table1.getColumns().stream().map(FluxColumn::getLabel))
                .containsExactlyInAnyOrder("result", "table", "_start", "_stop", "_time", "_value", "_field", "_measurement", "host", "region");

        // Records
        Assertions.assertThat(table1.getRecords()).hasSize(1);

        List<FluxRecord> records = new ArrayList<>();
        records.add(table1.getRecords().get(0));
        records.add(tables.get(1).getRecords().get(0));
        assertFluxRecords(records);
    }

    private void assertFluxRecords(@Nonnull final List<FluxRecord> records) {
        Assertions.assertThat(records).isNotNull();
        Assertions.assertThat(records).hasSize(2);

        // Record 1
        FluxRecord record1 = records.get(0);
        Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record1.getField()).isEqualTo("free");

        Assertions.assertThat(record1.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record1.getStop()).isNotNull();
        Assertions.assertThat(record1.getTime()).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(record1.getValue()).isEqualTo(21L);

        Assertions.assertThat(record1.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));

        // Record 2
        FluxRecord record2 = records.get(1);
        Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record2.getField()).isEqualTo("free");

        Assertions.assertThat(record2.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record2.getStop()).isNotNull();
        Assertions.assertThat(record2.getTime()).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(record2.getValue()).isEqualTo(42L);

        Assertions.assertThat(record2.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
    }

    private void prepareChunkRecords() {
        int totalRecords = 500_000;
        countDownLatch = new CountDownLatch(totalRecords);

        List<String> points = new ArrayList<>();

        IntStream.range(1, totalRecords + 1).forEach(i -> {

            String format = String.format("chunked,host=A,region=west free=%1$si %1$s", i);
            points.add(format);

            if (i % 1_000 == 0) {
                influxDB.write(DATABASE_NAME, "autogen", InfluxDB.ConsistencyLevel.ONE, TimeUnit.SECONDS, points);
                points.clear();
            }
        });
    }
}