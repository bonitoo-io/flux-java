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
package io.bonitoo.flux.mapper;

import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.flux.FluxException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This class us used to construct FluxResult from CSV.
 */
class FluxCsvParser {

    private static final int FRACTION_MIN_WIDTH = 0;
    private static final int FRACTION_MAX_WIDTH = 9;
    private static final boolean ADD_DECIMAL_POINT = true;

    private static final DateTimeFormatter RFC3339_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_INSTANT)
            .appendPattern("[.SSSSSSSSS][.SSSSSS][.SSS][.]")
            .appendOffset("+HH:mm", "Z")
            .toFormatter();

    private static final DateTimeFormatter RFC3339_NANO_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, FRACTION_MIN_WIDTH, FRACTION_MAX_WIDTH, ADD_DECIMAL_POINT)
            .appendPattern("X")
            .toFormatter();

    /**
     * Synchronously parse Flux CSV response to {@link FluxTable}s.
     *
     * @param reader with data
     * @return parsed data to {@link FluxTable}s
     * @throws IOException throw by {@link CSVParser}
     */
    @Nonnull
    List<FluxTable> parseFluxResponse(@Nonnull final Reader reader) throws IOException {

        Objects.requireNonNull(reader, "Reader is required");

        final List<FluxTable> tables = new ArrayList<>();

        parseFluxResponse(reader, new FluxResponseConsumer() {
            @Override
            public void addTable(final int tableIndex, @Nonnull final FluxTable table) {
                tables.add(tableIndex, table);
            }

            @Override
            public void addRecord(final int tableIndex, @Nonnull final FluxRecord record) {

                tables.get(tableIndex).getRecords().add(record);
            }
        });

        return tables;
    }

    /**
     * Asynchronously parse Flux CSV response to {@link FluxColumn}s.
     *
     * @param reader   with data
     * @param consumer of response
     * @throws IOException throw by {@link CSVParser}
     */
    void parseFluxResponse(@Nonnull final Reader reader,
                           @Nonnull final Consumer<FluxRecord> consumer) throws IOException {

        Objects.requireNonNull(reader, "Reader is required");
        Objects.requireNonNull(consumer, "Consumer<FluxRecord> is required");

        parseFluxResponse(reader, new FluxResponseConsumer() {
            @Override
            public void addTable(final int tableIndex, @Nonnull final FluxTable fluxTable) {

            }

            @Override
            public void addRecord(final int tableIndex, @Nonnull final FluxRecord fluxRecord) {
                consumer.accept(fluxRecord);
            }
        });

    }

    private enum ParsingState {
        NORMAL,

        IN_ERROR
    }

    private interface FluxResponseConsumer {
        void addTable(final int tableIndex, @Nonnull final FluxTable fluxTable);

        void addRecord(final int tableIndex, @Nonnull final FluxRecord fluxRecord);
    }

    private void parseFluxResponse(@Nonnull final Reader reader,
                                   @Nonnull final FluxResponseConsumer consumer) throws IOException {

        Objects.requireNonNull(reader, "Reader is required");
        Objects.requireNonNull(consumer, "FluxResponseConsumer is required");

        ParsingState parsingState = ParsingState.NORMAL;

        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);

        int tableIndex = 0;
        boolean startNewTable = false;
        FluxTable table = null;

        for (CSVRecord csvRecord : parser) {

            long recordNumber = csvRecord.getRecordNumber();

            //
            // Response has HTTP status ok, but response is error.
            //
            if (1 == recordNumber && csvRecord.get(0).equals("error") && csvRecord.get(1).equals("reference")) {
                parsingState = ParsingState.IN_ERROR;
                continue;

            }

            //
            // Throw FluxException with error response
            //
            if (ParsingState.IN_ERROR.equals(parsingState)) {
                String error = csvRecord.get(0);
                String reference = csvRecord.get(1);

                if (!reference.isEmpty()) {
                    error += String.format(" [reference: %s]", reference);
                }

                throw new FluxException(error);
            }

            String token = csvRecord.get(0);
            //// start new table
            if ("#datatype".equals(token)) {
                startNewTable = true;

                table = new FluxTable();
                consumer.addTable(tableIndex, table);
                tableIndex++;

            } else if (table == null) {
                String message = "Unable to parse CSV response. FluxTable definition was not found. Record: %d";

                throw new FluxResultMapperException(String.format(message, recordNumber));
            }

            //#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string
            if ("#datatype".equals(token)) {
                table.addDataTypes(toList(csvRecord));

            } else if ("#group".equals(token)) {
                table.addGroups(toList(csvRecord));

            } else if ("#default".equals(token)) {
                table.addDefaultEmptyValues(toList(csvRecord));

            } else {
                // parse column names
                if (startNewTable) {
                    table.addColumnNamesAndTags(toList(csvRecord));
                    startNewTable = false;
                    continue;
                }

                int currentIndex = Integer.parseInt(csvRecord.get(1 + 1));

                if (currentIndex > (tableIndex - 1)) {
                    //create new table with previous column headers settings
                    List<FluxColumn> fluxColumns = table.getColumns();
                    table = new FluxTable();
                    table.setColumns(fluxColumns);
                    consumer.addTable(tableIndex, table);
                    tableIndex++;
                }

                FluxRecord r = parseRecord(tableIndex - 1, table, csvRecord);
                consumer.addRecord(tableIndex - 1, r);
            }
        }
    }

    private FluxRecord parseRecord(final int tableIndex, final FluxTable table, final CSVRecord csvRecord)
            throws FluxResultMapperException {

        FluxRecord record = new FluxRecord(tableIndex);

        for (FluxColumn fluxColumn : table.getColumns()) {

            String columnName = fluxColumn.getLabel();

            String strValue = csvRecord.get(fluxColumn.getIndex() + 1);

            record.getValues().put(columnName, toValue(strValue, fluxColumn));
        }
        return record;
    }

    @Nonnull
    private List<String> toList(final CSVRecord csvRecord) {
        List<String> ret = new ArrayList<>(csvRecord.size());
        int size = csvRecord.size();

        for (int i = 1; i < size; i++) {
            String rec = csvRecord.get(i);
            ret.add(rec);
        }
        return ret;
    }

    @Nullable
    private Object toValue(@Nullable final String strValue, final @Nonnull FluxColumn column)
            throws FluxResultMapperException {

        Objects.requireNonNull(column, "FluxColumn is required");

        // Default value
        if (strValue == null || strValue.isEmpty()) {
            String defaultValue = column.getDefaultValue();
            if (defaultValue == null || defaultValue.isEmpty()) {
                return null;
            }

            return toValue(defaultValue, column);
        }

        String dataType = column.getDataType();
        switch (dataType) {
            case "boolean":
                return Boolean.valueOf(strValue);
            case "unsignedLong":
                return Long.parseUnsignedLong(strValue);
            case "long":
                return Long.parseLong(strValue);
            case "double":
                return Double.parseDouble(strValue);
            case "string":
                return strValue;
            case "base64Binary":
                return Base64.getDecoder().decode(strValue);
            case "dateTime:RFC3339":
                return RFC3339_NANO_FORMATTER.parse(strValue, Instant::from);
            case "dateTime:RFC3339Nano":
                return RFC3339_FORMATTER.parse(strValue, Instant::from);
            case "duration":
                return Duration.ofNanos(Long.parseUnsignedLong(strValue));
            default:
                throw new FluxResultMapperException("Unsupported datatype: " + dataType);
        }
    }
}
