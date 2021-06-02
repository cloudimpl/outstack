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

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "PolicyStatementRefs" , version = "v1")
public class PolicyStatementRef extends ChildEntity<Policy> implements ITenantOptional{
    private final String policyStatementRef;
    private final String tenantId;

    public PolicyStatementRef(String policyStatementRef, String tenantId) {
        this.policyStatementRef = policyStatementRef;
        this.tenantId = tenantId;
    }
    
    @Override
    public Class<Policy> rootType() {
        return Policy.class;
    }

    @Override
    public String entityId() {
        return policyStatementRef;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }
    
    private void applyEvent(PolicyStatementRefCreated created)
    {
        
    }
    
    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "PolicyStatementRefCreated": {
                applyEvent((PolicyStatementRefCreated)event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "PolicyRef";
    }
    
}
