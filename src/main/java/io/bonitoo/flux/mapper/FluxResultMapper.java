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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import io.bonitoo.flux.options.FluxCsvParserOptions;

import okio.Buffer;
import okio.BufferedSource;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:04)
 */
@ThreadSafe
public class FluxResultMapper {

    @Nullable
    public List<FluxTable> toFluxTables(@Nonnull final BufferedSource source,
                                        @Nonnull final FluxCsvParserOptions parserOptions)
            throws FluxResultMapperException, IOException {

        Objects.requireNonNull(source, "BufferedSource is required");
        Objects.requireNonNull(parserOptions, "FluxCsvParserOptions are required");

        Buffer buffer = new Buffer();
        source.readAll(buffer);
        Reader reader = new InputStreamReader(buffer.inputStream());
        FluxCsvParser tableCsvParser = new FluxCsvParser();

        return tableCsvParser.parseFluxResponse(reader, parserOptions);
    }
}
