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
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bonitoo.platform.dto.Authorization;
import io.bonitoo.platform.dto.Bucket;
import io.bonitoo.platform.dto.Organization;
import io.bonitoo.platform.dto.Permission;
import io.bonitoo.platform.dto.Status;
import io.bonitoo.platform.dto.User;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 12:02)
 */
@RunWith(JUnitPlatform.class)
class ITAuthorizationClientTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITAuthorizationClientTest.class.getName());

    private AuthorizationClient authorizationClient;

    private User user;

    @BeforeEach
    void setUp() {

        super.setUp();

        authorizationClient = platformService.createAuthorizationClient();

        user = platformService.createUserClient().createUser(generateName("Auth User"));
    }

    @Test
    void createAuthorization() {

        Permission readUsers = new Permission();
        readUsers.setAction(Permission.READ_ACTION);
        readUsers.setResource(Permission.USER_RESOURCE);

        Permission writeOrganizations = new Permission();
        writeOrganizations.setAction(Permission.WRITE_ACTION);
        writeOrganizations.setResource(Permission.ORGANIZATION_RESOURCE);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readUsers);
        permissions.add(writeOrganizations);

        Authorization authorization = authorizationClient.createAuthorization(user, permissions);

        LOG.log(Level.INFO, "Created authorization: {0}", authorization);

        Assertions.assertThat(authorization).isNotNull();
        Assertions.assertThat(authorization.getId()).isNotBlank();
        Assertions.assertThat(authorization.getToken()).isNotBlank();
        Assertions.assertThat(authorization.getUserID()).isEqualTo(user.getId());
        Assertions.assertThat(authorization.getUserName()).isEqualTo(user.getName());
        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.ACTIVE);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource()).isEqualTo("user");
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo("read");
        Assertions.assertThat(authorization.getPermissions().get(1).getResource()).isEqualTo("org");
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo("write");

        Assertions.assertThat(authorization.getLinks()).hasSize(2);
        Assertions.assertThat(authorization.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/v1/authorizations/" + authorization.getId()));
        Assertions.assertThat(authorization.getLinks()).hasEntrySatisfying("user", value -> Assertions.assertThat(value).isEqualTo("/v1/users/" + user.getId()));
    }

    @Test
    void createAuthorizationTask() {

        Organization organization = platformService.createOrganizationClient().createOrganization(generateName("Auth Organization"));

        String taskResource = Permission.taskResource(organization.getId());

        Permission createTask = new Permission();
        createTask.setResource(taskResource);
        createTask.setAction(Permission.CREATE_ACTION);

        Permission deleteTask = new Permission();
        deleteTask.setResource(taskResource);
        deleteTask.setAction(Permission.DELETE_ACTION);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(createTask);
        permissions.add(deleteTask);

        Authorization authorization = authorizationClient.createAuthorization(user, permissions);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource()).isEqualTo(taskResource);
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo("create");
        Assertions.assertThat(authorization.getPermissions().get(1).getResource()).isEqualTo(taskResource);
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo("delete");
    }

    @Test
    void createAuthorizationBucket() {

        Organization organization = platformService.createOrganizationClient().createOrganization(generateName("Auth Organization"));
        Bucket bucket = platformService.createBucketClient().createBucket(generateName("Auth Bucket"), organization);

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

        Authorization authorization = authorizationClient.createAuthorization(user, permissions);

        Assertions.assertThat(authorization.getPermissions()).hasSize(2);
        Assertions.assertThat(authorization.getPermissions().get(0).getResource()).isEqualTo(bucketResource);
        Assertions.assertThat(authorization.getPermissions().get(0).getAction()).isEqualTo("read");
        Assertions.assertThat(authorization.getPermissions().get(1).getResource()).isEqualTo(bucketResource);
        Assertions.assertThat(authorization.getPermissions().get(1).getAction()).isEqualTo("write");
    }

    @Test
    void findAuthorizationsByID() {

        Authorization authorization = authorizationClient.createAuthorization(user, new ArrayList<>());

        Authorization foundAuthorization = authorizationClient.findAuthorizationByID(authorization.getId());

        Assertions.assertThat(foundAuthorization).isNotNull();
        Assertions.assertThat(authorization.getId()).isEqualTo(foundAuthorization.getId());
        Assertions.assertThat(authorization.getToken()).isEqualTo(foundAuthorization.getToken());
        Assertions.assertThat(authorization.getUserID()).isEqualTo(foundAuthorization.getUserID());
        Assertions.assertThat(authorization.getUserName()).isEqualTo(foundAuthorization.getUserName());
        Assertions.assertThat(authorization.getStatus()).isEqualTo(foundAuthorization.getStatus());
    }

    @Test
    void findAuthorizationsByIDNull() {

        Authorization authorization = authorizationClient.findAuthorizationByID("00");

        Assertions.assertThat(authorization).isNull();
    }

    @Test
    void findAuthorizations() {

        int size = authorizationClient.findAuthorizations().size();

        authorizationClient.createAuthorization(user, new ArrayList<>());

        List<Authorization> authorizations = authorizationClient.findAuthorizations();
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByUser() {

        int size = authorizationClient.findAuthorizationsByUser(user).size();

        authorizationClient.createAuthorization(user, new ArrayList<>());

        List<Authorization> authorizations = authorizationClient.findAuthorizationsByUser(user);
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void findAuthorizationsByUserName() {

        int size = authorizationClient.findAuthorizationsByUserName(user.getName()).size();

        authorizationClient.createAuthorization(user, new ArrayList<>());

        List<Authorization> authorizations = authorizationClient.findAuthorizationsByUserName(user.getName());
        Assertions.assertThat(authorizations).hasSize(size + 1);
    }

    @Test
    void updateAuthorizationStatus() {

        Permission readUsers = new Permission();
        readUsers.setAction(Permission.READ_ACTION);
        readUsers.setResource(Permission.USER_RESOURCE);

        List<Permission> permissions = new ArrayList<>();
        permissions.add(readUsers);

        Authorization authorization = authorizationClient.createAuthorization(user, permissions);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.ACTIVE);

        authorization.setStatus(Status.INACTIVE);
        authorization = authorizationClient.updateAuthorizationStatus(authorization);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.INACTIVE);

        authorization.setStatus(Status.ACTIVE);
        authorization = authorizationClient.updateAuthorizationStatus(authorization);

        Assertions.assertThat(authorization.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void deleteAuthorization() {

        Authorization createdAuthorization = authorizationClient.createAuthorization(user, new ArrayList<>());
        Assertions.assertThat(createdAuthorization).isNotNull();

        Authorization foundAuthorization = authorizationClient.findAuthorizationByID(createdAuthorization.getId());
        Assertions.assertThat(foundAuthorization).isNotNull();

        // delete authorization
        authorizationClient.deleteAuthorization(createdAuthorization);

        foundAuthorization = authorizationClient.findAuthorizationByID(createdAuthorization.getId());
        Assertions.assertThat(foundAuthorization).isNull();
    }
}