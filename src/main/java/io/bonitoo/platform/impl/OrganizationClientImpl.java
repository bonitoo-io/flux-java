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
package io.bonitoo.platform.impl;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.core.Preconditions;
import io.bonitoo.platform.OrganizationClient;
import io.bonitoo.platform.dto.Organization;
import io.bonitoo.platform.dto.Organizations;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 08:57)
 */
final class OrganizationClientImpl extends AbstractRestClient implements OrganizationClient {

    private static final Logger LOG = Logger.getLogger(OrganizationClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<Organization> adapter;

    OrganizationClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {
        this.platformService = platformService;
        this.adapter = moshi.adapter(Organization.class);
    }

    @Nullable
    @Override
    public Organization findOrganizationByID(@Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(organizationID, "Organization ID");

        Call<Organization> organization = platformService.findOrganizationByID(organizationID);

        return execute(organization, "organization not found");
    }

    @Nonnull
    @Override
    public List<Organization> findOrganizations() {

        Call<Organizations> organizationsCall = platformService.findOrganizations();

        Organizations organizations = execute(organizationsCall);
        LOG.log(Level.FINEST, "findOrganizations found: {0}", organizations);

        return organizations.getOrgs();
    }

    @Nonnull
    @Override
    public Organization createOrganization(@Nonnull final String name) {

        Preconditions.checkNonEmptyString(name, "Organization name");

        Organization organization = new Organization();
        organization.setName(name);

        String json = adapter.toJson(organization);

        Call<Organization> call = platformService.createOrganization(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public Organization updateOrganization(@Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        String json = adapter.toJson(organization);

        Call<Organization> orgCall = platformService.updateOrganization(organization.getId(), createBody(json));

        return execute(orgCall);
    }

    @Override
    public void deleteOrganization(@Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        deleteOrganization(organization.getId());
    }

    @Override
    public void deleteOrganization(@Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(organizationID, "organizationID");

        Call<Void> call = platformService.deleteOrganization(organizationID);
        execute(call);
    }

}