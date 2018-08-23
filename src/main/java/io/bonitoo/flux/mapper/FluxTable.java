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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.options.FluxCsvParserOptions;

/**
 * This class represents table structure in FluxRequest.
 */
public final class FluxTable {

    //column header specification
    private List<ColumnHeader> columnHeaders = new ArrayList<>();

    //list of records
    private List<FluxRecord> records = new ArrayList<>();

    public List<ColumnHeader> getColumnHeaders() {
        return columnHeaders;
    }

    void setColumnHeaders(final List<ColumnHeader> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public List<FluxRecord> getRecords() {
        return records;
    }

    void addDataTypes(final List<String> datatypes) {

        for (int i = 0; i < datatypes.size(); i++) {
            String s = datatypes.get(i);

            ColumnHeader columnDef = new ColumnHeader();
            columnDef.setDataType(s);
            columnDef.setIndex(i);

            columnHeaders.add(columnDef);

        }
    }

    void addGroups(final List<String> groups) throws FluxResultMapperException {

        for (int i = 0; i < groups.size(); i++) {
            String s = groups.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #group header";
                throw new FluxResultMapperException(message);
            }

            def.addGroup(s);
        }

    }

    void addDefaultEmptyValues(final List<String> defaultEmptyValues) throws FluxResultMapperException {

        for (int i = 0; i < defaultEmptyValues.size(); i++) {
            String s = defaultEmptyValues.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #group header";
                throw new FluxResultMapperException(message);
            }

            def.setDefaultEmptyValue(s);
        }

    }

    /**
     * Sets the column names and tags and returns index of "table" column.
     *
     * @param columnNames
     * @param settings    of parsing
     * @return index of "table" column
     * @throws FluxResultMapperException
     */
    int addColumnNamesAndTags(final List<String> columnNames, @Nonnull final FluxCsvParserOptions settings)
            throws FluxResultMapperException {

        Objects.requireNonNull(settings, "FluxCsvParserOptions is required");

        int size = columnNames.size();
        int tableIndexColumn = -1;

        for (int i = 0; i < size; i++) {
            String columnName = columnNames.get(i);

            if (columnHeaders.isEmpty()) {
                throw new FluxResultMapperException("Unable to parse response, no #datatypes header found.");
            }
            ColumnHeader def = columnHeaders.get(i);

            if (def == null) {
                String message = "Unable to parse response, inconsistent  #datatypes and #group header";
                throw new FluxResultMapperException(message);
            }

            def.setColumnName(columnName);

            if ("table".equals(columnName)) {
                tableIndexColumn = i;
            }

            if (!(columnName.startsWith("_")
                    || columnName.isEmpty()
                    || "result".equals(columnName)
                    || "table".equals(columnName)
                    || settings.getValueDestinations().contains(columnName))) {
                def.setTag(true);
            }
        }

        return tableIndexColumn;
    }

}
