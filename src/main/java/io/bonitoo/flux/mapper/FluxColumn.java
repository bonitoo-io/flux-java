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
import javax.annotation.Nullable;

/**
 * This class represents column header specification of {@link FluxTable}.
 */
public final class FluxColumn {


    //TODO implement rest of data => to enum
    private static final String STRING_DATATYPE = "string";
    private static final String DATETIME_DATATYPE = "dateTime:RFC3339";
    private static final String LONG_DATATYPE = "long";
    private static final String DOUBLE_DATATYPE = "double";

    private String label;
    private String dataType;

    /**
     * Column index in record.
     */
    private int index;

    /**
     * Boolean flag indicating if the column is part of the table's group key.
     */
    private boolean group;
    /**
     * Default value to be used for rows whose string value is the empty string.
     */
    private String defaultValue;

    @Nullable
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

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
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

    public boolean isGroup() {
        return group;
    }

    public void setGroup(final boolean group) {
        this.group = group;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
