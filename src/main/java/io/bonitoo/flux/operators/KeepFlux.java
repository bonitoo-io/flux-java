package io.bonitoo.flux.operators;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.utils.Preconditions;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#keep">keep</a> -
 * Keep is the inverse of drop. It will return a table containing only columns that are specified, ignoring all others.
 * Only columns in the group key that are also specified in keep will be kept in the resulting group key.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>columns</b> - The list of columns that should be included in the resulting table.
 * Cannot be used with <i>fn</i>. [array of strings]
 * </li>
 * <li>
 * <b>fn</b> - The function which takes a column name as a parameter and returns a boolean indicating whether
 * or not the column should be included in the resulting table. Cannot be used with `columns`. [function(column)]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .keep(new String[]{"_time", "_value"});
 *
 * Flux flux = Flux
 *     .from("telegraf")
 *     .keep()
 *         .withFunction("col =~ /*inodes/");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (02/08/2018 11:22)
 * @since 3.0.0
 */
public final class KeepFlux extends AbstractParametrizedFlux {

    public KeepFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "keep";
    }

    /**
     * @param columns The list of columns that should be included in the resulting table.
     * @return this
     */
    @Nonnull
    public KeepFlux withColumns(@Nonnull final String[] columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }

    /**
     * @param columns The list of columns that should be included in the resulting table.
     * @return this
     */
    @Nonnull
    public KeepFlux withColumns(@Nonnull final Collection<String> columns) {

        Objects.requireNonNull(columns, "Columns are required");

        this.withPropertyValue("columns", columns);

        return this;
    }


    /**
     * @param function The function which takes a column name as a parameter and returns a boolean indicating whether
     *                 or not the column should be included in the resulting table.
     * @return this
     */
    @Nonnull
    public KeepFlux withFunction(@Nonnull final String function) {

        Preconditions.checkNonEmptyString(function, "Function");

        this.withPropertyValue("fn: (col)", function);

        return this;
    }

    @Nonnull
    @Override
    protected String propertyDelimiter(@Nonnull final String operatorName) {

        switch (operatorName) {
            case "fn: (col)":
                return " => ";

            default:
                return super.propertyDelimiter(operatorName);
        }
    }
}