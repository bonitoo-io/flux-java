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

import io.bonitoo.platform.dto.Organization;
import io.bonitoo.platform.impl.AbstractPlatformClientTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (12/09/2018 08:52)
 */
@RunWith(JUnitPlatform.class)
class OrganizationClientTest extends AbstractPlatformClientTest {

    private OrganizationClient organizationClient;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        organizationClient = platformClient.createOrganizationClient();
    }

    @Test
    void mappingLinks() {

        String data = "{\"links\":"
                + "{\"buckets\":\"/v2/buckets?org=TechnologiesRT\","
                + "\"dashboards\":\"/v2/dashboards?org=TechnologiesRT\","
                + "\"self\":\"/v2/orgs/029f2b0bc5aa4000\","
                + "\"tasks\":\"/v2/tasks?org=TechnologiesRT\","
                + "\"users\":\"/v2/orgs/029f2b0bc5aa4000/users\"},"
                + "\"id\":\"029f2b0bc5aa4000\","
                + "\"name\":\"TechnologiesRT\"}";

        platformServer.enqueue(createResponse(data));

        Organization organization = organizationClient.createOrganization("TechnologiesRT");

        Assertions.assertThat(organization.getLinks()).hasSize(5);
        Assertions.assertThat(organization.getLinks()).hasEntrySatisfying("buckets", value -> Assertions.assertThat(value).isEqualTo("/v2/buckets?org=TechnologiesRT"));
        Assertions.assertThat(organization.getLinks()).hasEntrySatisfying("dashboards", value -> Assertions.assertThat(value).isEqualTo("/v2/dashboards?org=TechnologiesRT"));
        Assertions.assertThat(organization.getLinks()).hasEntrySatisfying("self", value -> Assertions.assertThat(value).isEqualTo("/v2/orgs/029f2b0bc5aa4000"));
        Assertions.assertThat(organization.getLinks()).hasEntrySatisfying("tasks", value -> Assertions.assertThat(value).isEqualTo("/v2/tasks?org=TechnologiesRT"));
        Assertions.assertThat(organization.getLinks()).hasEntrySatisfying("users", value -> Assertions.assertThat(value).isEqualTo("/v2/orgs/029f2b0bc5aa4000/users"));
    }

    @Test
    void mappingOrganizations() {

        String data = "{"
                + "\"links\":{\"self\":\"/v2/orgs\"},"
                + "\"orgs\":["
                + "{\"links\":{"
                + "\"buckets\":\"/v2/buckets?org=PT Cons\","
                + "\"dashboards\":\"/v2/dashboards?org=PT Cons\","
                + "\"self\":\"/v2/orgs/029f341081d64000\","
                + "\"tasks\":\"/v2/tasks?org=PT Cons\","
                + "\"users\":\"/v2/orgs/029f341081d64000/users\"},"
                + "\"id\":\"029f341081d64000\",\"name\":\"PT Cons\"},"
                + "{\"links\":{"
                + "\"buckets\":\"/v2/buckets?org=HT Dia\","
                + "\"dashboards\":\"/v2/dashboards?org=HT Dia\","
                + "\"self\":\"/v2/orgs/029f341fc7d64000\","
                + "\"tasks\":\"/v2/tasks?org=HT Dia\","
                + "\"users\":\"/v2/orgs/029f341fc7d64000/users\"},"
                + "\"id\":\"029f341fc7d64000\",\"name\":\"HT Dia\"}]"
                + "}\n";

        platformServer.enqueue(createResponse(data));

        List<Organization> organizations = organizationClient.findOrganizations();
        Assertions.assertThat(organizations).hasSize(2);
    }
}