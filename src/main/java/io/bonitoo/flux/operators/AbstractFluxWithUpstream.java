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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;

/**
 * Abstract base class for operators that take an upstream source of {@link Flux}.
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 07:29)
 */
abstract class AbstractFluxWithUpstream extends Flux {

    @Nullable
    Flux source;

    AbstractFluxWithUpstream() {
    }

    AbstractFluxWithUpstream(@Nonnull final Flux source) {

        Objects.requireNonNull(source, "Source is required");

        this.source = source;
    }

    @Override
    protected final void appendActual(@Nonnull final FluxChain fluxChain) {

        if (source != null) {
            fluxChain.append(source);
        }

        appendAfterUpstream(fluxChain);
    }

    /**
     * Append the actual operator to {@link FluxChain}.
     *
     * @param fluxChain the incoming {@link FluxChain}, never null
     */
    abstract void appendAfterUpstream(@Nonnull final FluxChain fluxChain);
}
