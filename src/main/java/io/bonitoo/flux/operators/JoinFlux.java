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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.utils.Preconditions;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#join">join</a> -
 * Join two time series together on time and the list of <i>on</i> keys.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>tables</b> - Map of tables to join. Currently only two tables are allowed. [map of tables]</li>
 * <li><b>on</b> - List of tag keys that when equal produces a result set. [array of strings]</li>
 * <li>
 * <b>fn</b> - Defines the function that merges the values of the tables.
 * The function must defined to accept a single parameter.
 * The parameter is a map, which uses the same keys found in the tables map.
 * The function is called for each joined set of records from the tables. [function(tables)]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux cpu = Flux.from("telegraf")
 *     .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
 *     .range(-30L, ChronoUnit.MINUTES);
 *
 * Flux mem = Flux.from("telegraf")
 *     .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
 *     .range(-30L, ChronoUnit.MINUTES);
 *
 * Flux flux = Flux.join()
 *     .withTable("cpu", cpu)
 *     .withTable("mem", mem)
 *     .withOn("host")
 *     .withFunction("tables.cpu[\"_value\"] + tables.mem[\"_value\"]");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 14:47)
 * @since 1.0.0
 */
public final class JoinFlux extends AbstractParametrizedFlux {

    private Map<String, Flux> tables = new LinkedHashMap<>();

    public JoinFlux() {
        super();

        // add tables: property
        withPropertyValue("tables", (Supplier<String>) () -> {

            StringJoiner tablesValue = new StringJoiner(", ", "{", "}");

            tables.keySet().forEach(key -> tablesValue.add(String.format("%s:%s", key, key)));
            return tablesValue.toString();
        });
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "join";
    }

    @Override
    protected void beforeAppendOperatorName(@Nonnull final StringBuilder operator,
                                            @Nonnull final FluxChain fluxChain) {

        // add tables Flux scripts
        tables.keySet().forEach(key -> {
            FluxChain tableFluxChain = new FluxChain().addParameters(fluxChain.getParameters());

            operator.append(String.format("%s = %s\n", key, tables.get(key).print(tableFluxChain)));
        });
    }

    @Nonnull
    @Override
    protected String propertyDelimiter(@Nonnull final String operatorName) {

        switch (operatorName) {
            case "fn: (tables)":
                return " => ";

            default:
                return super.propertyDelimiter(operatorName);
        }
    }

    /**
     * Map of table to join. Currently only two tables are allowed.
     *
     * @param name  table name
     * @param table Flux script to map table
     * @return this
     */
    @Nonnull
    public JoinFlux withTable(@Nonnull final String name, @Nonnull final Flux table) {

        Preconditions.checkNonEmptyString(name, "Table name");
        Objects.requireNonNull(table, "Flux script to map table");

        tables.put(name, table);

        return this;
    }

    /**
     * @param tag Tag key that when equal produces a result set.
     * @return this
     */
    @Nonnull
    public JoinFlux withOn(@Nonnull final String tag) {

        Preconditions.checkNonEmptyString(tag, "Tag name");

        return withOn(new String[]{tag});
    }

    /**
     * @param tags List of tag keys that when equal produces a result set.
     * @return this
     */
    @Nonnull
    public JoinFlux withOn(@Nonnull final String[] tags) {

        Objects.requireNonNull(tags, "Tags are required");

        withPropertyValue("on", tags);

        return this;
    }

    /**
     * @param tags List of tag keys that when equal produces a result set.
     * @return this
     */
    @Nonnull
    public JoinFlux withOn(@Nonnull final Collection<String> tags) {

        Objects.requireNonNull(tags, "Tags are required");

        withPropertyValue("on", tags);

        return this;
    }

    /**
     * @param function Defines the function that merges the values of the tables.
     * @return this
     */
    @Nonnull
    public JoinFlux withFunction(@Nonnull final String function) {

        Preconditions.checkNonEmptyString(function, "Function");

        this.withPropertyValue("fn: (tables)", function);

        return this;
    }
}
