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
import com.cloudimpl.outstack.runtime.iam.ActionDescriptor;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "PolicyStatements" , version = "v1")
public class PolicyStatement extends RootEntity implements ITenantOptional{

    public enum EffectType {
        ALLOW, DENY
    }
    @Id
    private final String sid;
    private  EffectType effect;
    private  Collection<ActionDescriptor> actions;
    private  Collection<ResourceDescriptor> resources;
    private final String tenantId;
    public PolicyStatement(String sid,String tenantId) {
        this.sid = sid;
        this.tenantId = tenantId;
        this.actions = Collections.EMPTY_LIST;
        this.resources = Collections.EMPTY_LIST;
    }

    public Collection<ActionDescriptor> getActions() {
        return actions;
    }

    public EffectType getEffect() {
        return effect;
    }

    public Collection<ResourceDescriptor> getResources() {
        return resources;
    }

    public String getSid() {
        return sid;
    }

    @Override
    public String getTenantId()
    {
        return this.tenantId;
    }
    
    public boolean isActionMatched(String action) {
        return actions.stream().filter(ad -> ad.isActionMatched(action)).findFirst().isPresent();
    }

    @Override
    public String entityId() {
        return sid;
    }

    private void applyEvent(PolicyStatementCreated stmtCreated)
    {
        this.effect = stmtCreated.getEffect();
        this.actions = stmtCreated.getActions();
        this.resources = stmtCreated.getResources();
    }
    
    private void applyEvent(PolicyStatementUpdated stmtUpdated)
    {
        this.effect = stmtUpdated.getEffect();
        this.actions = stmtUpdated.getActions();
        this.resources = stmtUpdated.getResources();
    }
    
    @Override
    protected void apply(Event event) {
        switch(event.getClass().getSimpleName())
        {
            case "PolicyStatementCreated":
            {
                applyEvent((PolicyStatementCreated)event);
                break;
            }
            case "PolicyStatementUpdated":
            {
                applyEvent((PolicyStatementUpdated)event);
                break;
            }
            default:
            {
                 throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "sid";
    }

    @Override
    public String toString() {
        return "PolicyStatement{" + "sid=" + sid + ", effect=" + effect + ", actions=" + actions + ", resources=" + resources + ", tenantId=" + tenantId + '}';
    }

    
}
