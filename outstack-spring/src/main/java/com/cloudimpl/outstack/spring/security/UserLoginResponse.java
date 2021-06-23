/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.spring.security;

import com.cloudimpl.outstack.runtime.domain.PolicyStatementEntity;
import java.util.List;

/**
 *
 * @author nuwan
 */
public class UserLoginResponse {

    private final String userId;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean locked;
    private final boolean active;
    private final List<PolicyStatementEntity> stmts;
    private Object userdata;
    public UserLoginResponse(String userId,String firstname,String lastName, String username, String email,boolean locked, boolean active, List<PolicyStatementEntity> stmts) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.locked = locked;
        this.active = active;
        this.stmts = stmts;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isActive() {
        return active;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    
    public List<PolicyStatementEntity> getStmts() {
        return stmts;
    }

    public UserLoginResponse setUserData(Object userdata)
    {
        this.userdata = userdata;
        return this;
    }

    public Object getUserdata() {
        return userdata;
    }
    
    
}
