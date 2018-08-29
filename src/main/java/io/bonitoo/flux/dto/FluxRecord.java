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
package io.bonitoo.flux.dto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.flux.utils.Preconditions;

/**
 * A record is a tuple of values.
 *
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#record">Specification</a>.
 */
public final class FluxRecord {

    private final Integer tableIndex;
    private LinkedHashMap<String, Object> values = new LinkedHashMap<>();

    public FluxRecord(@Nonnull final Integer tableIndex) {

        Objects.requireNonNull(tableIndex, "Table index is required");

        this.tableIndex = tableIndex;
    }

    /**
     * @return the inclusive lower time bound of all records
     */
    @Nullable
    public Instant getStart() {
        return getValueByKey("_start");
    }

    /**
     * @return the exclusive upper time bound of all records
     */
    @Nullable
    public Instant getStop() {
        return getValueByKey("_stop");
    }

    /**
     * @return the time of the record
     */
    @Nullable
    public Instant getTime() {
        return getValueByKey("_time");
    }

    /**
     * @return the value of the record
     */
    @Nullable
    public Object getValue() {
        return getValueByKey("_value");
    }

    /**
     * @return get value with key <i>_field</i>
     */
    @Nullable
    public String getField() {
        return getValueByKey("_field");
    }

    /**
     * @return get value with key <i>_measurement</i>
     */
    @Nullable
    public String getMeasurement() {
        return getValueByKey("_measurement");
    }

    /**
     * @return the index of table which contains the record
     */
    @Nonnull
    public Integer getTableIndex() {
        return tableIndex;
    }

    /**
     * @return tuple of values
     */
    @Nonnull
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * @param index of value in CSV response
     * @return value
     * @see ArrayIndexOutOfBoundsException
     */
    @Nullable
    public Object getValueByIndex(final int index) {
        return values.values().toArray()[index];
    }

    /**
     * @param key of value in CSV response
     * @param <T> type of value
     * @return value
     */
    @Nullable
    public <T> T getValueByKey(@Nonnull final String key) {

        Preconditions.checkNonEmptyString(key, "key");

        //noinspection unchecked
        return (T) values.get(key);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FluxRecord.class.getSimpleName() + "[", "]")
                .add("tableIndex=" + tableIndex)
                .add("values=" + values.size())
                .toString();
    }
}
