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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.Preconditions;
import io.bonitoo.platform.UserClient;
import io.bonitoo.platform.dto.User;

import org.json.JSONObject;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 10:16)
 */
final class UserClientImpl extends AbstractRestClient implements UserClient {

    private final PlatformService platformService;

    UserClientImpl(@Nonnull final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Nullable
    @Override
    public User findUserByID(@Nonnull final String userID) {
        Preconditions.checkNonEmptyString(userID, "User ID");

        Call<User> user = platformService.findUserByID(userID);

        return execute(user, "user not found");
    }

    @Nonnull
    @Override
    public List<User> findUsers() {

        Call<List<User>> users = platformService.findUsers();

        return execute(users);
    }

    @Nonnull
    @Override
    public User createUser(@Nonnull final String name) {

        Preconditions.checkNonEmptyString(name, "User name");

        JSONObject json = createUserJSON(name);

        Call<User> user = platformService.createUser(createBody(json));

        return execute(user);
    }

    @Nonnull
    @Override
    public User updateUser(@Nonnull final User user) {

        Objects.requireNonNull(user, "User is required");

        JSONObject json = createUserJSON(user.getName());

        Call<User> userCall = platformService.updateUser(user.getId(), createBody(json));

        return execute(userCall);
    }

    @Override
    public void deleteUser(@Nonnull final User user) {

        Objects.requireNonNull(user, "User is required");

        deleteUser(user.getId());
    }

    @Override
    public void deleteUser(@Nonnull final String userID) {

        Preconditions.checkNonEmptyString(userID, "userID");

        Call<Void> call = platformService.deleteUser(userID);
        execute(call);
    }

    @Nonnull
    private JSONObject createUserJSON(@Nonnull final String name) {

        Preconditions.checkNonEmptyString(name, "User name");

        return new JSONObject().put("name", name);
    }
}