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

import java.time.Instant;

/**
 * This class represents column header specification of {@link FluxTable}.
 *
 * TODO naming same as in SPEC
 */
public class ColumnHeader {



    //string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string

    //TODO implement rest of data
    private static final String STRING_DATATYPE = "string";
    private static final String DATETIME_DATATYPE = "dateTime:RFC3339";
    private static final String LONG_DATATYPE = "long";
    private static final String DOUBLE_DATATYPE = "double";

    //flux datatype
    private String dataType;

    //column index in csv
    private int index;

    //column name in csv
    private String columnName;

    //group
    private String group;
    private String defaultEmptyValue;
    private boolean tag;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    public Object toValue(final String strValue) throws FluxResultMapperException {

        if (STRING_DATATYPE.equals(dataType)) {
            return strValue;
        }

        if (DATETIME_DATATYPE.equals(dataType)) {
            return Instant.parse(strValue);
        }

        if (LONG_DATATYPE.equals(dataType)) {
            return Long.parseLong(strValue);
        }

        if (DOUBLE_DATATYPE.equals(dataType)) {
            return Double.parseDouble(strValue);
        }

        throw new FluxResultMapperException("Unsupported datatype: " + dataType);

    }

    public void addGroup(final String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public String getDefaultEmptyValue() {
        return defaultEmptyValue;
    }

    public void setDefaultEmptyValue(final String defaultEmptyValue) {
        this.defaultEmptyValue = defaultEmptyValue;
    }

    public boolean getTag() {
        return tag;
    }

    public void setTag(final boolean tag) {
        this.tag = tag;
    }
}
