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

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ITenantOptional;
import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "Policies", version = "v1")
public class Policy extends RootEntity implements ITenantOptional {

    private String policyContext;
    @Id
    private final String policyName;
    private final String tenantId;

    private String domainOwner;
    private String domainContext;
    private String apiContext;

    private String rootType;

    public Policy(String policyName, String tenantId) {
        this.policyName = policyName;
        this.tenantId = tenantId;
    }

    public String getPolicyName() {
        return policyName;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String entityId() {
        return policyName;
    }

    public String getPolicyContext() {
        return policyContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getApiContext() {
        return apiContext;
    }

    public String getRootType() {
        return rootType;
    }
    
    private void applyEvent(PolicyCreated policyCreated) {
        this.policyContext = policyCreated.getPolicyContext();
        this.domainContext = policyCreated.getDomainContext();
        this.domainOwner = policyCreated.getDomainOwner();
        this.apiContext = policyCreated.getApiContext();
        this.rootType = policyCreated.getRootType();
    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "PolicyCreated": {
                applyEvent((PolicyCreated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "policyName";
    }
}
