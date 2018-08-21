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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import io.bonitoo.flux.mapper.ColumnHeader;
import io.bonitoo.flux.mapper.FluxResult;
import io.bonitoo.flux.mapper.Record;
import io.bonitoo.flux.mapper.Table;
import io.bonitoo.flux.operators.restriction.Restrictions;

import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:30)
 */
@RunWith(JUnitPlatform.class)
class ITFluxClient extends AbstractITFluxClient {

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

        influxDB.write(point1);
        influxDB.write(point2);
        influxDB.write(point3);
        influxDB.write(point4);
        influxDB.write(point5);
        influxDB.write(point6);
    }

    //TODO test chunked, GZIP

    @Test
    void query() {

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        FluxResult fluxResult = fluxClient.flux(flux);

        assertFluxResult(fluxResult);
    }

    @Test
    void callback() {

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        fluxClient.flux(flux, fluxResult -> {

            assertFluxResult(fluxResult);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

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

    private void assertFluxResult(@Nonnull final FluxResult fluxResult) {

        Assertions.assertThat(fluxResult).isNotNull();

        List<Table> tables = fluxResult.getTables();

        Assertions.assertThat(tables).hasSize(2);

        Table table1 = tables.get(0);
        // Data types
        Assertions.assertThat(table1.getColumnHeaders()).hasSize(11);
        Assertions.assertThat(table1.getColumnHeaders().stream().map(ColumnHeader::getDataType))
                .containsExactlyInAnyOrder("#datatype", "string", "long", "dateTime:RFC3339", "dateTime:RFC3339", "dateTime:RFC3339", "long", "string", "string", "string", "string");

        // Columns
        Assertions.assertThat(table1.getColumnHeaders().stream().map(ColumnHeader::getColumnName))
                .containsExactlyInAnyOrder("", "result", "table", "_start", "_stop", "_time", "_value", "_field", "_measurement", "host", "region");

        // Records
        Assertions.assertThat(table1.getRecords()).hasSize(1);

        // Record 1
        Record record1 = table1.getRecords().get(0);
        Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record1.getField()).isEqualTo("free");

        Assertions.assertThat(record1.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record1.getStop()).isNotNull();
        Assertions.assertThat(record1.getTime()).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(record1.getValue()).isEqualTo(21L);

        Assertions.assertThat(record1.getTags()).hasSize(2);
        Assertions.assertThat(record1.getTags().get("host")).isEqualTo("A");
        Assertions.assertThat(record1.getTags().get("region")).isEqualTo("west");

        // Record 2
        Record record2 = tables.get(1).getRecords().get(0);
        Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record2.getField()).isEqualTo("free");

        Assertions.assertThat(record2.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record2.getStop()).isNotNull();
        Assertions.assertThat(record2.getTime()).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(record2.getValue()).isEqualTo(42L);

        Assertions.assertThat(record2.getTags()).hasSize(2);
        Assertions.assertThat(record2.getTags().get("host")).isEqualTo("B");
        Assertions.assertThat(record2.getTags().get("region")).isEqualTo("west");
    }
}