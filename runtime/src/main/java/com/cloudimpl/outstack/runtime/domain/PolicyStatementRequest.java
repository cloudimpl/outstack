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
    private final String domainOwner;
    private final String domainContext;
    private final String rootType;
    private final EffectType effect;
    private final Collection<String> cmdActions;
    private final Collection<String> queryActions;
    private final Collection<String> resources;
    private final Collection<String> tags;

    public PolicyStatementRequest(Builder builder) {
        super(builder);
        this.sid = builder.sid;
        this.domainOwner = builder.domainOwner;
        this.domainContext = builder.domainContext;
        this.rootType= builder.rootType;
        this.effect = builder.effect;
        this.cmdActions = Collections.unmodifiableCollection(builder.cmdActions);
        this.queryActions = Collections.unmodifiableCollection(builder.queryActions);
        this.resources = Collections.unmodifiableCollection(builder.resources);
        this.tags = Collections.unmodifiableCollection(builder.tags);
    }

    public Collection<String> getCmdActions() {
        return cmdActions;
    }

    public Collection<String> getQueryActions() {
        return queryActions;
    }

    public EffectType getEffect() {
        return effect;
    }

    public String getRootType() {
        return rootType;
    }

    public Collection<String> getTags() {
        return tags;
    }

    
    public Collection<String> getResources() {
        return resources;
    }

    public String getSid() {
        return sid;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder {

        private String sid;
        private String domainOwner;
        private String domainContext;
        private String rootType;
        private EffectType effect;
        private Collection<String> cmdActions = new LinkedList<>();
        private Collection<String> queryActions = new LinkedList<>();
        private Collection<String> resources = new LinkedList<>();
        private Collection<String> tags = new LinkedList<>();
        
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
        
        public Builder withCmdAction(String action)
        {
            this.cmdActions.add(action);
            return this;
        }
        
        public Builder withQueryAction(String action)
        {
            this.queryActions.add(action);
            return this;
        }
        
        public Builder withResource(String resource)
        {
            this.resources.add(resource);
            return this;
        }
        
        public Builder withDomainContext(String domainContext)
        {
            this.domainContext = domainContext;
            return this;
        }
        
        public Builder withDomainOwner(String domainOwner)
        {
            this.domainOwner = domainOwner;
            return this;
        }
        
        public Builder withTag(String tag)
        {
            this.tags.add(tag);
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
       // PolicyStatementCreated pd = PolicyStatemetParser.parseStatement(stmt);
        System.out.println("");
    }
}
