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
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents the record in CSV response.
 */
public final class FluxRecord {

    //TODO generic + getValueByIndex
    private Instant start;
    private Instant stop;
    private Instant time;
    private Object value;
    private Map<String, Object> values = new HashMap<>();

    private String field;
    private String measurement;
    private Map<String, String> tags = new HashMap<>();

    public Instant getStart() {
        return start;
    }

    public void setStart(final Instant start) {
        this.start = start;
    }

    public Instant getStop() {
        return stop;
    }

    public void setStop(final Instant stop) {
        this.stop = stop;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(final Instant time) {
        this.time = time;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(final String measurement) {
        this.measurement = measurement;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(final Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(final Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FluxRecord.class.getSimpleName() + "[", "]")
                .add("measurement='" + measurement + "'")
                .add("field='" + field + "'")
                .add("start=" + start)
                .add("stop=" + stop)
                .add("time=" + time)
                .add("tags=" + tags)
                .add("value=" + value)
                .toString();
    }
}
