package io.bonitoo.flux.mapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.logging.Logger;
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

    private static final Logger LOG = Logger.getLogger(FluxResultMapper.class.getName());

    @Nullable
    public FluxResult toFluxResult(@Nonnull final BufferedSource source,
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
