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
package io.bonitoo.flux.options;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import io.bonitoo.flux.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Dialect is an object defining the options to use when encoding the response.
 *
 * @author Jakub Bednar (bednar@github) (22/08/2018 09:17)
 * @since 1.0.0
 */
public final class FluxDialect {

    public static final FluxDialect DEFAULTS = FluxDialect.builder()
            .header(true)
            .addAnnotation("datatype")
            .addAnnotation("group")
            .addAnnotation("default")
            .build();

    private final JSONObject jsonDialect;

    private FluxDialect(@Nonnull final Builder builder) {

        jsonDialect = new JSONObject()
                .put("header", builder.header)
                .put("delimiter", builder.delimiter)
                .put("quoteChar", builder.quoteChar)
                .put("commentPrefix", builder.commentPrefix);

        if (!builder.annotations.isEmpty()) {

            JSONArray annotations = new JSONArray();

            builder.annotations.forEach(annotations::put);

            jsonDialect.put("annotations", annotations);
        }
    }

    /**
     * @return configured dialect
     */
    @Nonnull
    public JSONObject getJson() {
        return jsonDialect;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static FluxDialect.Builder builder() {
        return new FluxDialect.Builder();
    }

    /**
     * A builder for {@code FluxDialect}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private boolean header = true;
        private String delimiter = ",";
        private String quoteChar = "\"";
        private List<String> annotations = new ArrayList<>();
        private String commentPrefix = "#";

        /**
         * Set if the header row is included.
         *
         * @param header if {@link Boolean#TRUE} the header row is included, otherwise its is omitted.
         *               Defaults to {@link Boolean#TRUE}.
         * @return {@code this}
         */
        @Nonnull
        public Builder header(final boolean header) {
            this.header = header;

            return this;
        }


        /**
         * Set the character to use as the delimiting value between columns.
         *
         * @param delimiter Delimiter is a character to use as the delimiting value between columns. Defaults to ",".
         * @return {@code this}
         */
        @Nonnull
        public Builder delimiter(@Nonnull final String delimiter) {

            Preconditions.checkOneCharString(delimiter, "delimiter");

            this.delimiter = delimiter;
            return this;
        }

        /**
         * Set character to use to quote values containing the delimiter.
         *
         * @param quoteChar QuoteChar is a character to use to quote values containing the delimiter. Defaults to ".
         * @return {@code this}
         */
        @Nonnull
        public Builder quoteChar(@Nonnull final String quoteChar) {

            Preconditions.checkOneCharString(quoteChar, "quoteChar");
            this.quoteChar = quoteChar;
            return this;
        }

        /**
         * Annotation describing properties about the columns of the table.
         * <p>
         * Annotations is a list of annotations that should be encoded.
         * If the list is empty the annotation column is omitted entirely. Defaults to an empty list.
         *
         * @param annotation annotation describing properties about the columns of the table
         * @return {@code this}
         */
        @Nonnull
        public Builder addAnnotation(@Nonnull final String annotation) {

            Preconditions.checkNonEmptyString(annotation, "annotation");

            this.annotations.add(annotation);
            return this;
        }

        /**
         * Set string prefix to add to comment rows.
         *
         * @param commentPrefix String prefix to add to comment rows. Defaults to "#".
         *                      Annotations are always comment rows.
         * @return {@code this}
         */
        @Nonnull
        public Builder commentPrefix(@Nonnull final String commentPrefix) {

            Preconditions.checkOneCharString(commentPrefix, "commentPrefix");

            this.commentPrefix = commentPrefix;
            return this;
        }

        /**
         * Build an instance of FluxDialect.
         *
         * @return {@link FluxDialect}
         */
        @Nonnull
        public FluxDialect build() {

            return new FluxDialect(this);
        }
    }
}