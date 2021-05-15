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

import java.util.StringTokenizer;

/**
 *
 * @author nuwan
 */
public class ResourceDescriptor {

    public enum Scope {
        ALL, //* | /RootEntity/**
        ROOT_ONLY, //RootEntity/*
        CHILD_TYPE_ONLY, //RootEntity/*/ChildType/*
        ROOT_ID_ONLY,  // /RootEntity/1234
        ROOT_ID_CHILD_ID_ONLY,     // /RootEntity/1234/ChildType/34124
        CHILD_ID_ONLY, // /RootEntity/*/ChildType/34124
    }
    
    private final Scope scope;
    private final String domainOwner;
    private final String domainContext;
    private final String tenantId;
    private final String rootType;
    private final String rootId;
    private final String childType;
    private final String childId;
    
    public ResourceDescriptor(String domainOwner, String domainContext, String tenantId, String rootType, String rootId,String childType,String childId,Scope scope) {
        this.domainOwner = domainOwner;
        this.domainContext = domainContext;
        this.tenantId = tenantId;
        this.rootType = rootType;
        this.rootId = rootId;
        this.childType = childType;
        this.childId = childId;
        this.scope = scope;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRootType() {
        return rootType;
    }

    public String getRootId() {
        return rootId;
    }

    public String getChildType() {
        return childType;
    }

    public String getChildId() {
        return childId;
    }

    public Scope getScope() {
        return scope;
    }

    public static final class Builder {

        private  String domainOwner;
        private  String domainContext;
        private  String tenantId;
        private  String rootType;
        private  String rootId;
        private String childType;
        private String childId;
        private Scope scope;
        public Builder withDomainOwner(String domainOwner)
        {
            this.domainOwner = domainOwner;
            return this;
        }
        
        public Builder withDomainContext(String domainContext)
        {
            this.domainContext = domainContext;
            return this;
        }
        
        public Builder withTenantId(String tenantId)
        {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder withRootType(String rootType)
        {
            this.rootType = rootType;
            return this;
        }
        
        public Builder withRootId(String rootId)
        {
            this.rootType = rootId;
            return this;
        }

        public Builder withChildType(String childType) {
            this.childType = childType;
            return this;
        }
        
        public Builder withChildId(String childId){
            this.childId = childId;
            return this;
        }
        
        public Builder withScope(Scope scope){
            this.scope = scope;
            return this;
        }
        
        public ResourceDescriptor build()
        {
            return new ResourceDescriptor(domainOwner, domainContext, tenantId, rootType, rootId, childType, childId, scope);
        }
    }
}
