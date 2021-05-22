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

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;

/**
 *
 * @author nuwan
 */
public class PolicyStatementCreated extends Event<PolicyStatementDescriptor> {

    private final String sid;
    private final String rootType;
    private final PolicyStatementDescriptor.EffectType effect;
    private final Collection<ActionDescriptor> actions;
    private final Collection<ResourceDescriptor> resources;

    public PolicyStatementCreated(String sid, String rootType,PolicyStatementDescriptor.EffectType effect, Collection<ActionDescriptor> actions, Collection<ResourceDescriptor> resources) {
        this.sid = sid;
        this.rootType = rootType;
        this.effect = effect;
        this.actions = actions;
        this.resources = resources;
    }

    public String getSid() {
        return sid;
    }

    public String getRootType() {
        return rootType;
    }

    public PolicyStatementDescriptor.EffectType getEffect() {
        return effect;
    }

    public Collection<ActionDescriptor> getActions() {
        return actions;
    }

    public Collection<ResourceDescriptor> getResources() {
        return resources;
    }

    public boolean isTenantStatement()
    {
        return getResources().stream().filter(r->!r.isTenantResource()).findAny().isEmpty();
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        return PolicyStatementDescriptor.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return PolicyStatementGroup.class;
    }

    @Override
    public String entityId() {
        return sid;
    }

    @Override
    public String rootEntityId() {
        return rootType;
    }

}
