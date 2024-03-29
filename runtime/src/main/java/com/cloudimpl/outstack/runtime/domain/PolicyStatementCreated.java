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

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.iam.ActionDescriptor;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import java.util.Collection;

/**
 *
 * @author nuwan
 */
public class PolicyStatementCreated extends Event<PolicyStatement> {

    private final String sid;
    private final String domainOwner;
    private final String domainContext;
    private final PolicyStatement.EffectType effect;
    private final Collection<ActionDescriptor> cmdActions;
    private final Collection<ActionDescriptor> queryActions;
    private final Collection<ResourceDescriptor> resources;
    private final Collection<String> tags;
    private final boolean validateAction;
    public PolicyStatementCreated(String sid,String domainOwner,String domainContext,PolicyStatement.EffectType effect, Collection<ActionDescriptor> cmdActions,Collection<ActionDescriptor> queryActions, Collection<ResourceDescriptor> resources,Collection<String> tags, boolean validateAction) {
        this.sid = sid;
        this.domainOwner = domainOwner;
        this.domainContext = domainContext;
        this.effect = effect;
        this.cmdActions = cmdActions;
        this.queryActions = queryActions;
        this.resources = resources;
        this.tags = tags;
        this.validateAction = validateAction;
    }

    public String getSid() {
        return sid;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public Collection<String> getTags() {
        return tags;
    }
    
    
    public PolicyStatement.EffectType getEffect() {
        return effect;
    }

    public Collection<ActionDescriptor> getCmdActions() {
        return cmdActions;
    }

    public Collection<ActionDescriptor> getQueryActions() {
        return queryActions;
    }

    public Collection<ResourceDescriptor> getResources() {
        return resources;
    }

    public boolean isValidateAction() {
        return validateAction;
    }

    public boolean isTenantStatement()
    {
        return getResources().stream().filter(r->!r.isTenantResource()).findAny().isEmpty();
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        return PolicyStatement.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return PolicyStatement.class;
    }

    @Override
    public String entityId() {
        return sid;
    }

    @Override
    public String rootEntityId() {
        return sid;
    }

}
