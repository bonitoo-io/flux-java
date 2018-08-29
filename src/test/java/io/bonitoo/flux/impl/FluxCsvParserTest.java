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
package io.bonitoo.flux.impl;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import io.bonitoo.flux.dto.FluxColumn;
import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.dto.FluxTable;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2018 12:26)
 */
@RunWith(JUnitPlatform.class)
class FluxCsvParserTest {

    private FluxCsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new FluxCsvParser();
    }

    @Test
    void responseWithMultipleValues() throws IOException {

        // curl -i -XPOST --data-urlencode 'q=from(db: "ubuntu_test") |> last()
        // |> map(fn: (r) => ({value1: r._value, _value2:r._value * r._value, value_str: "test"}))'
        // --data-urlencode "orgName=0" http://localhost:8093/v1/query

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long,long,string\n"
                + "#group,false,false,true,true,true,true,true,true,false,false,false\n"
                + "#default,_result,,,,,,,,,,\n"
                + ",result,table,_start,_stop,_field,_measurement,host,region,_value2,value1,value_str\n"
                + ",,0,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,A,west,121,11,test\n"
                + ",,1,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,free,mem,B,west,484,22,test\n"
                + ",,2,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,usage_system,cpu,A,west,1444,38,test\n"
                + ",,3,1677-09-21T00:12:43.145224192Z,2018-07-16T11:21:02.547596934Z,user_usage,cpu,A,west,2401,49,test";

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));

        List<FluxColumn> columnHeaders = tables.get(0).getColumns();
        Assertions.assertThat(columnHeaders).hasSize(11);
        Assertions.assertThat(columnHeaders.get(0).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(1).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(2).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(3).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(4).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(5).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(6).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(7).isGroup()).isTrue();
        Assertions.assertThat(columnHeaders.get(8).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(9).isGroup()).isFalse();
        Assertions.assertThat(columnHeaders.get(10).isGroup()).isFalse();

        Assertions.assertThat(tables).hasSize(4);

        // Record 1
        FluxTable fluxTable1 = tables.get(0);
        Assertions.assertThat(fluxTable1.getRecords()).hasSize(1);

        FluxRecord fluxRecord1 = fluxTable1.getRecords().get(0);
        Assertions.assertThat(0).isEqualTo(fluxRecord1.getTableIndex());
        Assertions.assertThat(fluxRecord1.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord1.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord1.getValue()).isNull();
        Assertions.assertThat(fluxRecord1.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(11L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(121L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));
        Assertions.assertThat(fluxRecord1.getValueByIndex(8)).isEqualTo(121L);
        Assertions.assertThat(fluxRecord1.getValueByIndex(9)).isEqualTo(11L);
        Assertions.assertThat(fluxRecord1.getValueByIndex(10)).isEqualTo("test");

        // Record 2
        FluxTable fluxTable2 = tables.get(1);
        Assertions.assertThat(fluxTable2.getRecords()).hasSize(1);

        FluxRecord fluxRecord2 = fluxTable2.getRecords().get(0);
        Assertions.assertThat(1).isEqualTo(fluxRecord2.getTableIndex());
        Assertions.assertThat(fluxRecord2.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("B"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord2.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord2.getValue()).isNull();
        Assertions.assertThat(fluxRecord2.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(22L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(484L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 3
        FluxTable fluxTable3 = tables.get(2);
        Assertions.assertThat(fluxTable3.getRecords()).hasSize(1);

        FluxRecord fluxRecord3 = fluxTable3.getRecords().get(0);
        Assertions.assertThat(2).isEqualTo(fluxRecord3.getTableIndex());
        Assertions.assertThat(fluxRecord3.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord3.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord3.getValue()).isNull();
        Assertions.assertThat(fluxRecord3.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(38L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(1444L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));

        // Record 4
        FluxTable fluxTable4 = tables.get(3);
        Assertions.assertThat(fluxTable4.getRecords()).hasSize(1);

        FluxRecord fluxRecord4 = fluxTable4.getRecords().get(0);
        Assertions.assertThat(3).isEqualTo(fluxRecord4.getTableIndex());
        Assertions.assertThat(fluxRecord4.getValues())
                .hasEntrySatisfying("host", value -> Assertions.assertThat(value).isEqualTo("A"))
                .hasEntrySatisfying("region", value -> Assertions.assertThat(value).isEqualTo("west"));
        Assertions.assertThat(fluxRecord4.getValues()).hasSize(11);
        Assertions.assertThat(fluxRecord4.getValue()).isNull();
        Assertions.assertThat(fluxRecord4.getValues())
                .hasEntrySatisfying("value1", value -> Assertions.assertThat(value).isEqualTo(49L))
                .hasEntrySatisfying("_value2", value -> Assertions.assertThat(value).isEqualTo(2401L))
                .hasEntrySatisfying("value_str", value -> Assertions.assertThat(value).isEqualTo("test"));
    }

    @Test
    void mappingBoolean() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,boolean\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,true\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,true\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,false\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,x\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));
        Assertions.assertThat((Object) tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(true);
        Assertions.assertThat((Object) tables.get(0).getRecords().get(1).getValueByKey("value")).isEqualTo(false);
        Assertions.assertThat((Object) tables.get(0).getRecords().get(2).getValueByKey("value")).isEqualTo(false);
        Assertions.assertThat((Object) tables.get(0).getRecords().get(3).getValueByKey("value")).isEqualTo(true);
    }

    @Test
    void mappingUnsignedLong() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,unsignedLong\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,17916881237904312345\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        long expected = Long.parseUnsignedLong("17916881237904312345");

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));
        Assertions.assertThat((Object) tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(expected);
        Assertions.assertThat((Object) tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingBase64Binary() throws IOException {

        String binaryData = "test value";
        String encodedString = Base64.getEncoder().encodeToString(binaryData.getBytes(UTF_8));

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,base64Binary\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A," + encodedString + "\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));

        byte[] value = tables.get(0).getRecords().get(0).getValueByKey("value");
        Assertions.assertThat(value).isNotEmpty();
        Assertions.assertThat(new String(value, UTF_8)).isEqualTo(binaryData);

        Assertions.assertThat((Object) tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingRFC3339() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,dateTime:RFC3339\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,1970-01-01T00:00:10Z\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));
        Assertions.assertThat((Object) tables.get(0).getRecords().get(0).getValueByKey("value")).isEqualTo(Instant.ofEpochSecond(10));
        Assertions.assertThat((Object) tables.get(0).getRecords().get(1).getValueByKey("value")).isNull();
    }

    @Test
    void mappingRFC3339Nano() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,dateTime:RFC3339Nano\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,1970-01-01T00:00:10.999999999Z+07:00\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));
        
        Assertions.assertThat((Object) tables.get(0).getRecords().get(0).getValueByKey("value"))
                .isEqualTo(Instant.ofEpochSecond(10).plusNanos(999999999));
        Assertions.assertThat((Object) tables.get(0).getRecords().get(1).getValueByKey("value"))
                .isNull();
    }

    @Test
    void mappingDuration() throws IOException {

        String data = "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,duration\n"
                + "#group,false,false,false,false,false,false,false,false,false,true\n"
                + "#default,_result,,,,,,,,,\n"
                + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,value\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,125\n"
                + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,\n";

        List<FluxTable> tables = parser.parseFluxResponse(new StringReader(data));

        Assertions.assertThat((Object) tables.get(0).getRecords().get(0).getValueByKey("value"))
                .isEqualTo(Duration.ofNanos(125));
        Assertions.assertThat((Object) tables.get(0).getRecords().get(1).getValueByKey("value"))
                .isNull();
    }
}