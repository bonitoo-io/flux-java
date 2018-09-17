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
import io.bonitoo.Preconditions;
import io.bonitoo.platform.BucketClient;
import io.bonitoo.platform.dto.Bucket;
import io.bonitoo.platform.dto.Buckets;
import io.bonitoo.platform.dto.Organization;

import org.json.JSONObject;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:47)
 */
final class BucketClientImpl extends AbstractRestClient implements BucketClient {

    private static final Logger LOG = Logger.getLogger(BucketClientImpl.class.getName());

    private final PlatformService platformService;

    BucketClientImpl(@Nonnull final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Nullable
    @Override
    public Bucket findBucketByID(@Nonnull final String bucketID) {

        Preconditions.checkNonEmptyString(bucketID, "Bucket ID");

        Call<Bucket> bucket = platformService.findBucketByID(bucketID);

        return execute(bucket, "bucket not found");
    }

    @Nonnull
    @Override
    public List<Bucket> findBuckets() {
        return findBucketsByOrganizationName(null);
    }

    @Nonnull
    public List<Bucket> findBucketsByOrganization(@Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        return findBucketsByOrganizationName(organization.getName());
    }

    @Nonnull
    @Override
    public List<Bucket> findBucketsByOrganizationName(@Nullable final String organizationName) {

        Call<Buckets> bucketsCall = platformService.findBuckets(organizationName);

        Buckets buckets = execute(bucketsCall);
        LOG.log(Level.FINEST, "findBucketsByOrganizationName found: {0}", buckets);

        return buckets.getBuckets();
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name, @Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        return createBucket(name, organization.getName());
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name, @Nonnull final String organizationName) {
        return createBucket(name, null, organizationName);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name,
                               @Nullable final Long retentionPeriod,
                               @Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        return createBucket(name, retentionPeriod, organization.getName());
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name,
                               @Nullable final Long retentionPeriod,
                               @Nonnull final String organizationName) {

        Preconditions.checkNonEmptyString(name, "Bucket name");
        Preconditions.checkNonEmptyString(organizationName, "Organization name");

        JSONObject json = createBucketJSON(name, retentionPeriod)
                .put("organization", organizationName);

        Call<Bucket> bucket = platformService.createBucket(createBody(json));

        return execute(bucket);
    }

    @Nonnull
    @Override
    public Bucket updateBucket(@Nonnull final Bucket bucket) {

        Objects.requireNonNull(bucket, "Bucket is required");

        JSONObject json = createBucketJSON(bucket.getName(), bucket.getRetentionPeriod());

        Call<Bucket> bucketCall = platformService.updateBucket(bucket.getId(), createBody(json));

        return execute(bucketCall);
    }

    @Override
    public void deleteBucket(@Nonnull final Bucket bucket) {

        Objects.requireNonNull(bucket, "Bucket is required");

        deleteBucket(bucket.getId());
    }

    @Override
    public void deleteBucket(@Nonnull final String bucketID) {

        Preconditions.checkNonEmptyString(bucketID, "bucketID");

        Call<Void> call = platformService.deleteBucket(bucketID);
        execute(call);
    }

    @Nonnull
    private JSONObject createBucketJSON(@Nonnull final String name,
                                        @Nullable final Long retentionPeriod) {

        Preconditions.checkNonEmptyString(name, "Bucket name");

        return new JSONObject()
                .put("name", name)
                .put("retentionPeriod", retentionPeriod);
    }
}