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
package io.bonitoo.flux.option.query;

import java.util.StringJoiner;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import io.bonitoo.core.Preconditions;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 12:35)
 * @since 1.0.0
 */
@ThreadSafe
public abstract class AbstractOption {

    private final String name;
    protected String value;

    AbstractOption(@Nonnull final String name) {
        Preconditions.checkNonEmptyString(name, "Name of option");

        this.name = name;
    }

    @Override
    public String toString() {

        return new StringJoiner(" ")
                .add("option")
                .add(name)
                .add("=")
                .add(value)
                .toString();
    }
}
