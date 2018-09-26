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
import io.bonitoo.platform.BucketClient;
import io.bonitoo.platform.dto.Bucket;
import io.bonitoo.platform.dto.Buckets;
import io.bonitoo.platform.dto.Organization;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:47)
 */
final class BucketClientImpl extends AbstractRestClient implements BucketClient {

    private static final Logger LOG = Logger.getLogger(BucketClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<Bucket> adapter;

    BucketClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {
        this.platformService = platformService;
        this.adapter = moshi.adapter(Bucket.class);
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
    public Bucket createBucket(@Nonnull final String name,
                               @Nonnull final String retentionPeriod,
                               @Nonnull final Organization organization) {

        Objects.requireNonNull(organization, "Organization is required");

        return createBucket(name, retentionPeriod, organization.getName());
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final String name,
                               @Nonnull final String retentionPeriod,
                               @Nonnull final String organizationName) {

        Preconditions.checkNonEmptyString(name, "Bucket name");
        Preconditions.checkNonEmptyString(organizationName, "Organization name");
        Preconditions.checkDuration(retentionPeriod, "Bucket.retentionPeriod");

        Bucket bucket = new Bucket();
        bucket.setName(name);
        bucket.setRetentionPeriod(retentionPeriod);
        bucket.setOrganizationName(organizationName);

        return createBucket(bucket);
    }

    @Nonnull
    @Override
    public Bucket createBucket(@Nonnull final Bucket bucket) {

        Objects.requireNonNull(bucket, "Bucket is required");
        Preconditions.checkNonEmptyString(bucket.getName(), "Bucket name");
        Preconditions.checkNonEmptyString(bucket.getOrganizationName(), "Organization name");
        Preconditions.checkDuration(bucket.getRetentionPeriod(), "Bucket.retentionPeriod");

        Call<Bucket> call = platformService.createBucket(createBody(adapter.toJson(bucket)));

        return execute(call);
    }

    @Nonnull
    @Override
    public Bucket updateBucket(@Nonnull final Bucket bucket) {

        Objects.requireNonNull(bucket, "Bucket is required");
        Preconditions.checkDuration(bucket.getRetentionPeriod(), "Bucket.retentionPeriod");

        String json = adapter.toJson(bucket);

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
}