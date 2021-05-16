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

import com.cloudimpl.outstack.runtime.iam.PolicyStatement.EffectType;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author nuwan
 */
public class PolicyStatementDescriptor {

    private final String sid;
    private final EffectType effect;
    private final Collection<ActionDescriptor> actions;
    private final Collection<ResourceDescriptor> resources;

    public PolicyStatementDescriptor(String sid, EffectType effect, Collection<ActionDescriptor> actions, Collection<ResourceDescriptor> resources) {
        this.sid = sid;
        this.effect = effect;
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
    
}
