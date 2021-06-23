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
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.PolicyStatementEntity;
import com.cloudimpl.outstack.runtime.iam.ActionDescriptor;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwan
 */
public class PolicyStatement implements IPolicyStatement {

    private final String sid;
    private final PolicyStatementEntity.EffectType effect;
    private final Collection<ActionDescriptor> actions;
    private final Collection<ResourceDescriptor> resources;

    public PolicyStatement(Builder builder) {
        this.sid = builder.sid;
        this.effect = builder.effect;
        this.actions = Collections.unmodifiableList(builder.actions);
        this.resources = Collections.unmodifiableList(builder.resources);
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public PolicyStatementEntity.EffectType getEffect() {
        return effect;
    }

    @Override
    public Collection<ActionDescriptor> getActions() {
        return actions;
    }

    @Override
    public Collection<ResourceDescriptor> getResources() {
        return resources;
    }

    @Override
    public boolean isActionMatched(String action) {
        return actions.stream().filter(ad -> ad.isActionMatched(action)).findFirst().isPresent();
    }

    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder {

        private String sid;
        private PolicyStatementEntity.EffectType effect;
        private List<ActionDescriptor> actions;
        private List<ResourceDescriptor> resources;

        public Builder() {
            this.effect = PolicyStatementEntity.EffectType.DENY;
            this.resources = new LinkedList<>();
            this.actions = new LinkedList<>();
        }

        public List<ActionDescriptor> getActions() {
            return actions;
        }

        public PolicyStatementEntity.EffectType getEffect() {
            return effect;
        }

        public List<ResourceDescriptor> getResources() {
            return resources;
        }

        public String getSid() {
            return sid;
        }

        public Builder withSid(String sid) {
            this.sid = sid;
            return this;
        }

        public Builder withEffect(PolicyStatementEntity.EffectType effectType) {
            this.effect = effectType;
            return this;
        }

        public Builder withAction(ActionDescriptor action) {
            this.actions.add(action);
            return this;
        }

        public Builder withResource(ResourceDescriptor resource) {
            this.resources.add(resource);
            return this;
        }

        public PolicyStatement build() {
            return new PolicyStatement(this);
        }
    }
}
