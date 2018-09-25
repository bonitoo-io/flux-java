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
import io.bonitoo.platform.AuthorizationClient;
import io.bonitoo.platform.dto.Authorization;
import io.bonitoo.platform.dto.Authorizations;
import io.bonitoo.platform.dto.Permission;
import io.bonitoo.platform.dto.User;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import org.json.JSONObject;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 12:00)
 */
final class AuthorizationClientImpl extends AbstractRestClient implements AuthorizationClient {

    private static final Logger LOG = Logger.getLogger(AuthorizationClientImpl.class.getName());

    private final PlatformService platformService;
    private final JsonAdapter<Authorization> adapter;

    AuthorizationClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {
        this.platformService = platformService;
        this.adapter = moshi.adapter(Authorization.class);
    }

    @Nonnull
    @Override
    public Authorization createAuthorization(@Nonnull final User user, @Nonnull final List<Permission> permissions) {

        Objects.requireNonNull(user, "User is required");
        Objects.requireNonNull(permissions, "Permissions are required");

        return createAuthorization(user.getId(), permissions);
    }

    @Nonnull
    @Override
    public Authorization createAuthorization(@Nonnull final String userID,
                                             @Nonnull final List<Permission> permissions) {

        Preconditions.checkNonEmptyString(userID, "UserID");
        Objects.requireNonNull(permissions, "Permissions are required");

        Authorization authorization = new Authorization();
        authorization.setUserID(userID);
        authorization.setPermissions(permissions);

        String json = adapter.toJson(authorization);

        Call<Authorization> call = platformService.createAuthorization(createBody(json));

        return execute(call);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizations() {
        return findAuthorizationsByUserID(null);
    }

    @Nullable
    @Override
    public Authorization findAuthorizationByID(@Nonnull final String authorizationID) {

        Preconditions.checkNonEmptyString(authorizationID, "authorizationID");

        Call<Authorization> call = platformService.findAuthorization(authorizationID);

        return execute(call, "authorization not found");
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUser(@Nonnull final User user) {

        Objects.requireNonNull(user, "User is required");

        return findAuthorizations(user.getId(), null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUserID(@Nullable final String userID) {
        return findAuthorizations(userID, null);
    }

    @Nonnull
    @Override
    public List<Authorization> findAuthorizationsByUserName(@Nullable final String userName) {
        return findAuthorizations(null, userName);
    }

    @Nonnull
    @Override
    public Authorization updateAuthorizationStatus(@Nonnull final Authorization authorization) {

        Objects.requireNonNull(authorization, "Authorization is required");

        JSONObject json = new JSONObject();
        json.put("userID", authorization.getUserID());
        json.put("status", authorization.getStatus().name().toLowerCase());

        Call<Authorization> authorizationCall = platformService
                .updateAuthorization(authorization.getId(), createBody(json));

        return execute(authorizationCall);
    }

    @Override
    public void deleteAuthorization(@Nonnull final Authorization authorization) {

        Objects.requireNonNull(authorization, "Authorization is required");

        deleteAuthorization(authorization.getId());
    }

    @Override
    public void deleteAuthorization(@Nonnull final String authorizationID) {

        Preconditions.checkNonEmptyString(authorizationID, "authorizationID");

        Call<Void> call = platformService.deleteAuthorization(authorizationID);
        execute(call);
    }

    @Nonnull
    private List<Authorization> findAuthorizations(@Nullable final String userID, @Nullable final String userName) {

        Call<Authorizations> authorizationsCall = platformService.findAuthorizations(userID, userName);

        Authorizations authorizations = execute(authorizationsCall);
        LOG.log(Level.FINEST, "findAuthorizations found: {0}", authorizations);

        return authorizations.getAuths();
    }
}