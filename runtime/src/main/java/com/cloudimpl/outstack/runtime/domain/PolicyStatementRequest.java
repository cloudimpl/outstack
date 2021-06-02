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

import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement.EffectType;
import com.cloudimpl.outstack.runtime.iam.PolicyStatemetParser;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author nuwan
 */
public class PolicyStatementRequest extends Command {

    private final String sid;
    private final String rootType;
    private final EffectType effect;
    private final Collection<String> actions;
    private final Collection<String> resources;

    public PolicyStatementRequest(Builder builder) {
        super(builder);
        this.sid = builder.sid;
        this.rootType= builder.rootType;
        this.effect = builder.effect;
        this.actions = Collections.unmodifiableCollection(builder.actions);
        this.resources = Collections.unmodifiableCollection(builder.resources);
    }

    public Collection<String> getActions() {
        return actions;
    }

    public EffectType getEffect() {
        return effect;
    }

    public String getRootType() {
        return rootType;
    }

    
    public Collection<String> getResources() {
        return resources;
    }

    public String getSid() {
        return sid;
    }

    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder {

        private String sid;
        private String rootType;
        private EffectType effect;
        private Collection<String> actions = new LinkedList<>();
        private Collection<String> resources = new LinkedList<>();
        
        public Builder withSid(String sid)
        {
            this.sid = sid;
            return this;
        }
        
        public Builder withRootType(String rootType)
        {
            this.rootType = rootType;
            return this;
        }
        
        public Builder withEffect(EffectType type)
        {
            this.effect = type;
            return this;
        }
        
        public Builder withAction(String action)
        {
            this.actions.add(action);
            return this;
        }
        
        public Builder withResource(String resource)
        {
            this.resources.add(resource);
            return this;
        }
        
        @Override
        public PolicyStatementRequest build()
        {
            return new PolicyStatementRequest(this);
        }
    }

    public static void main(String[] args) {
        String json = "{"
                + "sid:ReadOnlyAccess,"
                + "effect:ALLOW,"
                + "actions:[GetTenant],"
                + "resources:[\"v1/**\"]"
                + "}";
        PolicyStatementRequest stmt = GsonCodecRuntime.decode(PolicyStatementRequest.class, json);
        PolicyStatementCreated pd = PolicyStatemetParser.parseStatement(stmt);
        System.out.println("");
    }
}
