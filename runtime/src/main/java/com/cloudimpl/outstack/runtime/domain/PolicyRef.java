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
package com.cloudimpl.outstack.runtime.domain;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ITenantOptional;
import com.cloudimpl.outstack.runtime.domainspec.Id;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "PolicyRef", version = "v1")
public class PolicyRef extends ChildEntity<Role> implements ITenantOptional {

    @Id
    private final String policyRef;
    private final String tenantId;
    private String domainContext;
    private String domainOwner;
    private String version;
    
    public PolicyRef(String policyRef, String tenantId) {
        this.policyRef = policyRef;
        this.tenantId = tenantId;
    }

    @Override
    public Class<Role> rootType() {
        return Role.class;
    }

    @Override
    public String entityId() {
        return policyRef;
    }

    public String getPolicyRef() {
        return policyRef;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getVersion() {
        return version;
    }
    
    private void applyEvent(PolicyRefCreated event) {
        this.domainContext = event.getDomainContext();
        this.domainOwner = event.getDomainOwner();
        this.version = event.getVersion();
    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "PolicyRefCreated": {
                applyEvent((PolicyRefCreated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "policyRef";
    }

}
