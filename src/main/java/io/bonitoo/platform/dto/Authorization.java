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
package io.bonitoo.platform.dto;

import java.util.List;
import java.util.StringJoiner;

import com.squareup.moshi.Json;

/**
 * Authorization is a authorization.
 *
 * @author Jakub Bednar (bednar@github) (17/09/2018 11:05)
 */
public final class Authorization extends AbstractHasLinks {

    private String id;

    private String token;

    private String userID;

    @Json(name = "user")
    private String userName;

    private Status status;

    private List<Permission> permissions;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(final List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Authorization.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("token='-'")
                .add("userID='" + userID + "'")
                .add("userName='" + userName + "'")
                .add("status=" + status)
                .add("permissions=" + permissions)
                .toString();
    }
}