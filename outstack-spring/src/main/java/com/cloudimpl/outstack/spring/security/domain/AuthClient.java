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

import com.cloudimpl.outstack.runtime.domain.ServiceModuleProvisioned;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwan
 */
public class AuthClient extends RootEntity{

    private final String clientId;
    private String secret;
    private List<String> redirectUrls = new LinkedList<>();

    public AuthClient(String clientId) {
        this.clientId = clientId;
    }
    
    
    @Override
    public String entityId() {
        return clientId;
    }

    private void applyEvent(AuthClientRegistered event)
    {
        this.secret = event.getSecret();
        this.redirectUrls = event.getRedirectUrls();
    }
    
    private void applyEvent(AuthClientUpdated event)
    {
        this.secret = event.getSecret();
        this.redirectUrls = event.getRedirectUrls();
    }
    
    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "AuthClientRegistered": {
                applyEvent((AuthClientRegistered) event);
                break;
            }
            case "AuthClientUpdated": {
                applyEvent((AuthClientUpdated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "clientId";
    }
    
}
