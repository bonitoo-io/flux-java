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
package io.bonitoo.platform.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The wrapper for "/v1/orgs" response.
 *
 * @author Jakub Bednar (bednar@github) (12/09/2018 10:12)
 */
public final class Organizations {

    /**
     * The URIs of resources.
     */
    private Map<String, String> links = new HashMap<>();

    private List<Organization> orgs = new ArrayList<>();

    public Map<String, String> getLinks() {
        return links;
    }

    public Organizations setLinks(final Map<String, String> links) {
        this.links = links;
        return this;
    }

    public List<Organization> getOrgs() {
        return orgs;
    }

    public Organizations setOrgs(final List<Organization> orgs) {
        this.orgs = orgs;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Organizations.class.getSimpleName() + "[", "]")
                .add("links=" + links)
                .add("orgs=" + orgs)
                .toString();
    }
}