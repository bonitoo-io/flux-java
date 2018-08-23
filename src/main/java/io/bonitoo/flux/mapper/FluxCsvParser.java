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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.options.FluxCsvParserOptions;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This class us used to construct FluxResult from CSV.
 */
class FluxCsvParser {

    @Nonnull
    List<FluxTable> parseFluxResponse(@Nonnull final Reader reader) throws FluxResultMapperException, IOException {
        return parseFluxResponse(reader, FluxCsvParserOptions.DEFAULTS);
    }

    @Nonnull
    List<FluxTable> parseFluxResponse(@Nonnull final Reader reader, @Nonnull final FluxCsvParserOptions settings)
            throws FluxResultMapperException, IOException {

        Objects.requireNonNull(reader, "Reader is required");
        Objects.requireNonNull(settings, "FluxCsvParserOptions is required");

        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
        final List<CSVRecord> records = parser.getRecords();

        final List<FluxTable> tables = new ArrayList<>();

        boolean startNewTable = false;
        FluxTable table = null;

        int tableIndex = 0;
        int tableColumnIndex = -1;

        for (int i = 0, recordsSize = records.size(); i < recordsSize; i++) {
            CSVRecord csvRecord = records.get(i);
            String token = csvRecord.get(0);
            //// start new table
            if ("#datatype".equals(token)) {
                startNewTable = true;

                table = new FluxTable();
                tables.add(tableIndex, table);
                tableIndex++;

            } else if (table == null) {
                String message = "Unable to parse CSV response. FluxTable definition was not found. Row:" + i;
                throw new FluxResultMapperException(message);
            }
            //#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string
            if ("#datatype".equals(token)) {
                table.addDataTypes(toList(csvRecord));

            } else if ("#group".equals(token)) {
                table.addGroups(toList(csvRecord));

            } else if (token.startsWith("#")) {
                table.addDefaultEmptyValues(toList(csvRecord));

            } else {
                // parse column names
                if (startNewTable) {
                    tableColumnIndex = table.addColumnNamesAndTags(toList(csvRecord), settings);
                    startNewTable = false;
                    continue;
                }

                //create the new table object if tableColumnIndex is incremented
                if (tableColumnIndex < 0) {
                    throw new FluxResultMapperException("table index is not found in CSV header");
                }

                int currentIndex = Integer.parseInt(csvRecord.get(tableColumnIndex + 1));

                if (currentIndex > (tableIndex - 1)) {
                    //create new table with previous column headers settings
                    List<FluxColumn> fluxColumns = table.getColumns();
                    table = new FluxTable();
                    table.setColumns(fluxColumns);
                    tables.add(tableIndex, table);
                    tableIndex++;
                }

                FluxRecord r = parseRecord(table, csvRecord);
                table.getRecords().add(r);
            }
        }
        return tables;
    }

    private FluxRecord parseRecord(final FluxTable table, final CSVRecord csvRecord)
            throws FluxResultMapperException {

        FluxRecord record = new FluxRecord();

        for (FluxColumn fluxColumn : table.getColumns()) {

            int index = fluxColumn.getIndex();
            String columnName = fluxColumn.getLabel();

            record.getValues().put(columnName, fluxColumn.toValue(csvRecord.get(index + 1)));
        }
        return record;
    }


    private List<String> toList(final CSVRecord csvRecord) {
        List<String> ret = new ArrayList<>(csvRecord.size());
        int size = csvRecord.size();

        for (int i = 1; i < size; i++) {
            String rec = csvRecord.get(i);
            ret.add(rec);
        }
        return ret;
    }

}
