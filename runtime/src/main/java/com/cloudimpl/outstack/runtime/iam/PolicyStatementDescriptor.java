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
package com.cloudimpl.outstack.runtime.iam;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author nuwan
 */
public class PolicyStatementDescriptor extends ChildEntity<PolicyStatementGroup> {

    public enum EffectType {
        ALLOW, DENY
    }

    private final String sid;
    private  EffectType effect;
    private  Collection<ActionDescriptor> actions;
    private  Collection<ResourceDescriptor> resources;

    public PolicyStatementDescriptor(String sid) {
        this.sid = sid;
        this.actions = Collections.unmodifiableCollection(actions);
        this.resources = Collections.unmodifiableCollection(resources);
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

    public boolean isActionMatched(String action) {
        return actions.stream().filter(ad -> ad.isActionMatched(action)).findFirst().isPresent();
    }

    @Override
    public Class<PolicyStatementGroup> rootType() {
        return PolicyStatementGroup.class;
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
    
    @Override
    protected void apply(Event event) {
        switch(event.getClass().getSimpleName())
        {
            case "PolicyStatementCreated":
            {
                applyEvent((PolicyStatementCreated)event);
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

}
