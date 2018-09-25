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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.bonitoo.platform.dto.Authorization;
import io.bonitoo.platform.dto.Bucket;
import io.bonitoo.platform.dto.Organization;
import io.bonitoo.platform.dto.Permission;
import io.bonitoo.platform.dto.User;
import io.bonitoo.platform.event.WriteSuccessEvent;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/09/2018 13:39)
 */
@RunWith(JUnitPlatform.class)
class ITWriteClientTest extends AbstractITClientTest {

    private WriteClient writeClient;
    private Bucket bucket;
    private Organization organization;
    private Authorization authorization;

    @BeforeEach
    void setUp() {

        super.setUp();

        writeClient = platformService.createWriteClient();

        User user = platformService.createUserClient().createUser(generateName("Write User"));
        organization = platformService.createOrganizationClient().createOrganization(generateName("Write Organization"));
        bucket = platformService.createBucketClient().createBucket(generateName("Write Bucket"), organization);

        String bucketResource = Permission.bucketResource(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.WRITE_ACTION);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readBucket);
        permissions.add(writeBucket);

        authorization = platformService.createAuthorizationClient().createAuthorization(user, permissions);
    }

    @AfterEach
    void tearDown() {

        writeClient.close();
    }

    @Test
    void success() throws InterruptedException {

        String record = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";

        CountDownLatch countDownLatch = new CountDownLatch(1);
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getBucket()).isEqualTo(bucket.getId());
                    Assertions.assertThat(event.getOrganization()).isEqualTo(organization.getId());
                    Assertions.assertThat(event.getToken()).isEqualTo(authorization.getToken());
                    Assertions.assertThat(event.getLineProtocol()).isEqualTo(record);

                    countDownLatch.countDown();
                });

        writeClient.write(bucket.getId(), organization.getId(), authorization.getToken(),
                record);

        Assertions.assertThat(countDownLatch.getCount()).isEqualTo(1);

        boolean wasWritten = countDownLatch.await(2, TimeUnit.SECONDS);
        Assertions.assertThat(wasWritten).isTrue();

        Assertions.assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
}