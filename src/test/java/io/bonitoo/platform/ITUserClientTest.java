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

import io.bonitoo.platform.dto.User;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 11:26)
 */
@RunWith(JUnitPlatform.class)
class ITUserClientTest extends AbstractITClientTest {

    private UserClient userClient;

    @BeforeEach
    void setUp() {

        super.setUp();

        userClient = platformService.getUserClient();
    }

    @Test
    void createUser() {

        String userName = generateName("John Ryzen");

        User user = userClient.createUser(userName);

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getId()).isNotBlank();
        Assertions.assertThat(user.getName()).isEqualTo(userName);
    }

    @Test
    void findUserByID() {

        String userName = generateName("John Ryzen");

        User user = userClient.createUser(userName);

        User userByID = userClient.findUserByID(user.getId());

        Assertions.assertThat(userByID).isNotNull();
        Assertions.assertThat(userByID.getId()).isEqualTo(user.getId());
        Assertions.assertThat(userByID.getName()).isEqualTo(user.getName());
    }

    @Test
    void findUserByIDNull() {

        User user = userClient.findUserByID("00");

        Assertions.assertThat(user).isNull();
    }

    @Test
    void findUsers() {

        int size = userClient.findUsers().size();

        userClient.createUser(generateName("John Ryzen"));

        List<User> users = userClient.findUsers();
        Assertions.assertThat(users).hasSize(size + 1);
    }


    @Test
    void deleteUser() {

        User createdUser = userClient.createUser(generateName("John Ryzen"));
        Assertions.assertThat(createdUser).isNotNull();

        User foundUser = userClient.findUserByID(createdUser.getId());
        Assertions.assertThat(foundUser).isNotNull();

        // delete task
        userClient.deleteUser(createdUser);

        foundUser = userClient.findUserByID(createdUser.getId());
        Assertions.assertThat(foundUser).isNull();
    }

    @Test
    void updateUser() {

        User createdUser = userClient.createUser(generateName("John Ryzen"));
        createdUser.setName("Tom Push");

        User updatedUser = userClient.updateUser(createdUser);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        Assertions.assertThat(updatedUser.getName()).isEqualTo("Tom Push");
    }
}