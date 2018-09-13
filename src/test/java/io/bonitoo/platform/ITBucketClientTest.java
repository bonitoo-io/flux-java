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
package io.bonitoo.platform;

import java.util.List;

import io.bonitoo.platform.dto.Bucket;
import io.bonitoo.platform.dto.Organization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (13/09/2018 10:49)
 */
@RunWith(JUnitPlatform.class)
class ITBucketClientTest extends AbstractITClientTest {

    private Organization organization;

    private BucketClient bucketClient;
    private OrganizationClient organizationClient;

    @BeforeEach
    void setUp() {

        super.setUp();

        bucketClient = platformService.getBucketClient();
        organizationClient = platformService.getOrganizationClient();

        organization = organizationClient.createOrganization(generateName("Org"));
    }

    @Test
    void createBucket() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketClient.createBucket(bucketName, organization);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getId()).isNotBlank();
        Assertions.assertThat(bucket.getName()).isEqualTo(bucketName);
        Assertions.assertThat(bucket.getOrganizationID()).isEqualTo(organization.getId());
        Assertions.assertThat(bucket.getOrganizationName()).isEqualTo(organization.getName());
        Assertions.assertThat(bucket.getRetentionPeriod()).isEqualTo(0);
        Assertions.assertThat(bucket.getLinks()).hasSize(2);
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("org", value -> Assertions.assertThat(value).isEqualTo("/v1/orgs/" + organization.getId()));
        Assertions.assertThat(bucket.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/v1/buckets/" + bucket.getId()));
    }

    @Test
    void createBucketWithRetentionPolicy() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketClient.createBucket(bucketName, 10_000L, organization);

        Assertions.assertThat(bucket).isNotNull();
        Assertions.assertThat(bucket.getRetentionPeriod()).isEqualTo(10_000L);
        
    }

    @Test
    void findBucketByID() {

        String bucketName = generateName("robot sensor");

        Bucket bucket = bucketClient.createBucket(bucketName, organization);

        Bucket bucketByID = bucketClient.findBucketByID(bucket.getId());

        Assertions.assertThat(bucketByID).isNotNull();
        Assertions.assertThat(bucketByID.getId()).isEqualTo(bucket.getId());
        Assertions.assertThat(bucketByID.getName()).isEqualTo(bucket.getName());
        Assertions.assertThat(bucketByID.getOrganizationID()).isEqualTo(bucket.getOrganizationID());
        Assertions.assertThat(bucketByID.getOrganizationName()).isEqualTo(bucket.getOrganizationName());
        Assertions.assertThat(bucketByID.getRetentionPeriod()).isEqualTo(bucket.getRetentionPeriod());
        Assertions.assertThat(bucketByID.getLinks()).hasSize(bucket.getLinks().size());
    }

    @Test
    void findBucketByIDNull() {

        Bucket bucket = bucketClient.findBucketByID("00");

        Assertions.assertThat(bucket).isNull();
    }

    @Test
    void findBuckets() {

        int size = bucketClient.findBuckets().size();

        bucketClient.createBucket(generateName("robot sensor"), organization);

        Organization organization2 = organizationClient.createOrganization(generateName("Second"));
        bucketClient.createBucket(generateName("robot sensor"), organization2);

        List<Bucket> buckets = bucketClient.findBuckets();
        Assertions.assertThat(buckets).hasSize(size + 3);
    }

    @Test
    void findBucketsByOrganization() {

        Assertions.assertThat(bucketClient.findBucketsByOrganization(organization)).hasSize(1);

        bucketClient.createBucket(generateName("robot sensor"), organization);

        Organization organization2 = organizationClient.createOrganization(generateName("Second"));
        bucketClient.createBucket(generateName("robot sensor"), organization2);

        Assertions.assertThat(bucketClient.findBucketsByOrganization(organization)).hasSize(2);
    }

    @Test
    void deleteBucket() {

        Bucket createBucket = bucketClient.createBucket(generateName("robot sensor"), organization);
        Assertions.assertThat(createBucket).isNotNull();

        Bucket foundBucket = bucketClient.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNotNull();

        // delete task
        bucketClient.deleteBucket(createBucket);

        foundBucket = bucketClient.findBucketByID(createBucket.getId());
        Assertions.assertThat(foundBucket).isNull();
    }

    @Test
    void updateOrganization() {

        Bucket createBucket = bucketClient.createBucket(generateName("robot sensor"), organization);
        createBucket.setName("Therm sensor 2000");
        createBucket.setRetentionPeriod(5_000L);

        Bucket updatedBucket = bucketClient.updateBucket(createBucket);

        Assertions.assertThat(updatedBucket).isNotNull();
        Assertions.assertThat(updatedBucket.getId()).isEqualTo(createBucket.getId());
        Assertions.assertThat(updatedBucket.getName()).isEqualTo("Therm sensor 2000");
        Assertions.assertThat(updatedBucket.getRetentionPeriod()).isEqualTo(5_000L);
    }
}