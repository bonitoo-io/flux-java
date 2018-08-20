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
package io.bonitoo.flux.operators;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.utils.Preconditions;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#window">window</a> - Groups the results by
 * a given time range.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>every</b> - Duration of time between windows. Defaults to <i>period's</i> value. [duration]
 * <li><b>period</b> - Duration of the windowed partition. Defaults to <i>every's</i> value. [duration]
 * <li><b>start</b> - The time of the initial window partition. [time]
 * <li><b>round</b> - Rounds a window's bounds to the nearest duration. Defaults to <i>every's</i> value. [duration]
 * <li><b>column</b> - Name of the time column to use. Defaults to <i>_time</i>. [string]
 * <li><b>startCol</b> - Name of the column containing the window start time. Defaults to <i>_start</i>. [string]
 * <li><b>stopCol</b> - Name of the column containing the window stop time. Defaults to <i>_stop</i>. [string]
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .window(15L, ChronoUnit.MINUTES)
 *     .max();
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .window(15L, ChronoUnit.MINUTES,
 *             20L, ChronoUnit.SECONDS,
 *             -50L, ChronoUnit.WEEKS,
 *             1L, ChronoUnit.SECONDS)
 *     .max();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (27/06/2018 12:01)
 * @since 3.0.0
 */
//TODO refactored
public final class WindowFlux extends AbstractParametrizedFlux {

    public WindowFlux(@Nonnull final Flux flux) {
        super(flux);
    }


    @Nonnull
    @Override
    protected String operatorName() {
        return "window";
    }

    /**
     * @param every     duration of time between windows
     * @param everyUnit a {@code ChronoUnit} determining how to interpret the {@code every}
     * @return this
     */
    @Nonnull
    public WindowFlux withEvery(@Nonnull final Long every, @Nonnull final ChronoUnit everyUnit) {

        Objects.requireNonNull(every, "Every is required");
        Objects.requireNonNull(everyUnit, "Every ChronoUnit is required");

        this.withPropertyValue("every", every, everyUnit);

        return this;
    }

    /**
     * @param every     duration of time between windows
     * @return this
     */
    @Nonnull
    public WindowFlux withEvery(@Nonnull final String every) {

        Preconditions.checkDuration(every, "Every");

        this.withPropertyValue("every", every);

        return this;
    }

    /**
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @return this
     */
    @Nonnull
    public WindowFlux withPeriod(@Nonnull final Long period, @Nonnull final ChronoUnit periodUnit) {

        Objects.requireNonNull(period, "Period is required");
        Objects.requireNonNull(periodUnit, "Period ChronoUnit is required");

        this.withPropertyValue("period", period, periodUnit);

        return this;
    }

    /**
     * @param period     duration of the windowed partition
     * @return this
     */
    @Nonnull
    public WindowFlux withPeriod(@Nonnull final String period) {

        Preconditions.checkDuration(period, "Period");

        this.withPropertyValue("period", period);

        return this;
    }

    /**
     * @param start     the time of the initial window partition
     * @param startUnit a {@code ChronoUnit} determining how to interpret the {@code start}
     * @return this
     */
    @Nonnull
    public WindowFlux withStart(@Nonnull final Long start, @Nonnull final ChronoUnit startUnit) {

        Objects.requireNonNull(start, "Start is required");
        Objects.requireNonNull(startUnit, "Start ChronoUnit is required");

        this.withPropertyValue("start", start, startUnit);

        return this;
    }

    /**
     * @param start the time of the initial window partition
     * @return this
     */
    @Nonnull
    public WindowFlux withStart(@Nonnull final Instant start) {

        Objects.requireNonNull(start, "Start is required");

        this.withPropertyValue("start", start);

        return this;
    }


    /**
     * @param start the time of the initial window partition
     * @return this
     */
    @Nonnull
    public WindowFlux withStart(@Nonnull final String start) {

        Preconditions.checkDuration(start, "Start");

        this.withPropertyValue("start", start);

        return this;
    }

    /**
     * @param round     rounds a window's bounds to the nearest duration
     * @param roundUnit a {@code ChronoUnit} determining how to interpret the {@code round}
     * @return this
     */
    @Nonnull
    public WindowFlux withRound(@Nonnull final Long round, @Nonnull final ChronoUnit roundUnit) {

        Objects.requireNonNull(round, "Round is required");
        Objects.requireNonNull(roundUnit, "Round ChronoUnit is required");

        this.withPropertyValue("round", round, roundUnit);

        return this;
    }

    /**
     * @param timeColumn name of the time column to use
     * @return this
     */
    @Nonnull
    public WindowFlux withColumn(@Nonnull final String timeColumn) {

        Preconditions.checkNonEmptyString(timeColumn, "Time column");

        this.withPropertyValueEscaped("column", timeColumn);

        return this;
    }

    /**
     * @param startCol name of the column containing the window start time
     * @return this
     */
    @Nonnull
    public WindowFlux withStartCol(@Nonnull final String startCol) {

        Preconditions.checkNonEmptyString(startCol, "Start column");

        this.withPropertyValueEscaped("startCol", startCol);

        return this;
    }

    /**
     * @param stopCol name of the column containing the window stop time
     * @return this
     */
    @Nonnull
    public WindowFlux withStopCol(@Nonnull final String stopCol) {

        Preconditions.checkNonEmptyString(stopCol, "Strop column");

        this.withPropertyValueEscaped("stopCol", stopCol);

        return this;
    }
}
