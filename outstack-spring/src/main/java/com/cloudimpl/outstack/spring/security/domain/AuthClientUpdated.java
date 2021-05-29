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
package com.cloudimpl.outstack.spring.security.domain;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.List;

/**
 *
 * @author nuwan
 */
public class AuthClientUpdated extends Event<AuthClient> {

    private final String clientId;
    private final String secret;
    private final List<String> redirectUrls;

    public AuthClientUpdated(String clientId, String secret, List<String> redirectUrls) {
        this.clientId = clientId;
        this.secret = secret;
        this.redirectUrls = redirectUrls;
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        return AuthClient.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return AuthClient.class;
    }

    public String getClientId() {
        return clientId;
    }

    public List<String> getRedirectUrls() {
        return redirectUrls;
    }

    public String getSecret() {
        return secret;
    }

    
    @Override
    public String entityId() {
        return clientId;
    }

    @Override
    public String rootEntityId() {
        return clientId;
    }

}
