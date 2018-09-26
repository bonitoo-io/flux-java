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
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bonitoo.platform.dto.Organization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 09:51)
 */
@RunWith(JUnitPlatform.class)
class ITOrganizationClientTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITOrganizationClientTest.class.getName());

    private OrganizationClient organizationClient;

    @BeforeEach
    void setUp() {

        super.setUp();

        organizationClient = platformService.createOrganizationClient();
    }

    @Test
    void createOrganization() {

        String organizationName = generateName("Constant Pro");

        Organization organization = organizationClient.createOrganization(organizationName);

        LOG.log(Level.INFO, "Created organization: {0}", organization);

        Assertions.assertThat(organization).isNotNull();
        Assertions.assertThat(organization.getId()).isNotBlank();
        Assertions.assertThat(organization.getName()).isEqualTo(organizationName);
        Assertions.assertThat(organization.getLinks()).hasSize(5);
    }

    @Test
    void findOrganizationByID() {

        String organizationName = generateName("Constant Pro");

        Organization organization = organizationClient.createOrganization(organizationName);

        Organization organizationByID = organizationClient.findOrganizationByID(organization.getId());

        Assertions.assertThat(organizationByID).isNotNull();
        Assertions.assertThat(organizationByID.getId()).isEqualTo(organization.getId());
        Assertions.assertThat(organizationByID.getName()).isEqualTo(organization.getName());
        Assertions.assertThat(organizationByID.getLinks()).hasSize(5);
    }

    @Test
    void findOrganizationByIDNull() {

        Organization organization = organizationClient.findOrganizationByID("00");

        Assertions.assertThat(organization).isNull();
    }

    @Test
    void findOrganizations() {

        int size = organizationClient.findOrganizations().size();

        organizationClient.createOrganization(generateName("Constant Pro"));

        List<Organization> organizations = organizationClient.findOrganizations();
        Assertions.assertThat(organizations).hasSize(size + 1);
    }

    @Test
    void deleteOrganization() {

        Organization createdOrganization = organizationClient.createOrganization(generateName("Constant Pro"));
        Assertions.assertThat(createdOrganization).isNotNull();

        Organization foundOrganization = organizationClient.findOrganizationByID(createdOrganization.getId());
        Assertions.assertThat(foundOrganization).isNotNull();

        // delete task
        organizationClient.deleteOrganization(createdOrganization);

        foundOrganization = organizationClient.findOrganizationByID(createdOrganization.getId());
        Assertions.assertThat(foundOrganization).isNull();
    }

    @Test
    void updateOrganization() {

        Organization createdOrganization = organizationClient.createOrganization(generateName("Constant Pro"));
        createdOrganization.setName("Master Pb");

        Organization updatedOrganization = organizationClient.updateOrganization(createdOrganization);

        Assertions.assertThat(updatedOrganization).isNotNull();
        Assertions.assertThat(updatedOrganization.getId()).isEqualTo(createdOrganization.getId());
        Assertions.assertThat(updatedOrganization.getName()).isEqualTo("Master Pb");

        Assertions.assertThat(updatedOrganization.getLinks()).hasSize(5);
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("buckets", value -> Assertions.assertThat(value).isEqualTo("/v2/buckets?org=Master Pb"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("dashboards", value -> Assertions.assertThat(value).isEqualTo("/v2/dashboards?org=Master Pb"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/v2/orgs/" + updatedOrganization.getId()));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("tasks", value -> Assertions.assertThat(value).isEqualTo("/v2/tasks?org=Master Pb"));
        Assertions.assertThat(updatedOrganization.getLinks()).hasEntrySatisfying("members", value -> Assertions.assertThat(value).isEqualTo("/v2/orgs/" + updatedOrganization.getId() + "/members"));
    }
}