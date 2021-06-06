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

/**
 *
 * @author nuwan
 */
public class ResourceDescriptor {

    public enum ResourceScope {
        NONE,
        GLOBAL, //*
        VERSION_ONLY,
        ALL, //RootEntity/**
        ALL_ROOT_ID_ONLY, //RootEntity/*
        ALL_ROOT_ID_CHILD_TYPE_ONLY, //RootEntity/*/ChildType/*
        ROOT_ID_CHILD_TYPE_ONLY,// /RootEntity/1234/ChildType/*
        ROOT_ID_ONLY, // /RootEntity/1234
        ROOT_ID_CHILD_ID_ONLY, // /RootEntity/1234/ChildType/34124
        ALL_ROOT_ID_CHILD_ID_ONLY, // /RootEntity/*/ChildType/34124
    }

    public enum TenantScope {
        NONE,
        ALL, //*
        TENANT_ID
    }

    private final ResourceScope resourceScope;
    private final TenantScope tenantScope;
    private final String version;
    private final String tenantId;
    private final String rootType;
    private final String rootId;
    private final String childType;
    private final String childId;

    private ResourceDescriptor(Builder builder) {
        this.version = builder.version;
        this.tenantId = builder.tenantId;
        this.rootType = builder.rootType;
        this.rootId = builder.rootId;
        this.childType = builder.childType;
        this.childId = builder.childId;
        this.resourceScope = builder.resourceScope;
        this.tenantScope = builder.tenantScope;
    }

    public boolean isResourceMatched(String resource)
    {
        switch(resourceScope)
        {
            case GLOBAL:
            {
                return true;
            }
            case ALL:
            {
                return rootType.toLowerCase().equals(resource.toLowerCase());
            }
            default:
            {
                return false;
            }
        }
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

    public String getVersion() {
        return version;
    }

    public ResourceScope getResourceScope() {
        return resourceScope;
    }

    public TenantScope getTenantScope() {
        return tenantScope;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isTenantResource()
    {
        return tenantScope != TenantScope.NONE;
    }
    
    @Override
    public String toString() {
        return "ResourceDescriptor{" + "resourceScope=" + resourceScope + ", tenantScope=" + tenantScope + ", version=" + version + ", tenantId=" + tenantId + ", rootType=" + rootType + ", rootId=" + rootId + ", childType=" + childType + ", childId=" + childId + '}';
    }

    
//    public static void main(String[] args) {
//        String line = "_1231";
//        //Pattern r = RESOURCE_NAME_PATTERN.compile("[a-zA-Z]+[_[a-zA-Z0-9]]+");
//        // Matcher m = 
//        boolean ret = RESOURCE_NAME_PATTERN.matcher(line).matches();
////        if (m.find()) {
////            System.out.println("Found: " + m.group(0));
////        } else {
////            System.out.println("Not found");
////        }
//        System.out.println("r1: " + ret);
//        
//        ret = RESOURCE_ID_PATTERN.matcher(line).matches();
//        System.out.println("r2: " + ret);
//    }

    public static final class Builder {

        private String version;
        private String tenantId;
        private String rootType;
        private String rootId;
        private String childType;
        private String childId;
        private ResourceScope resourceScope;
        private TenantScope tenantScope;

        public Builder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withRootType(String rootType) {
            this.rootType = rootType;
            return this;
        }

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withChildType(String childType) {
            this.childType = childType;
            return this;
        }

        public Builder withChildId(String childId) {
            this.childId = childId;
            return this;
        }

        public Builder withResourceScope(ResourceScope scope) {
            this.resourceScope = scope;
            return this;
        }

        public Builder withTenantScope(TenantScope scope){
            this.tenantScope = scope;
            return this;
        }
        
        public ResourceDescriptor build() {
            return new ResourceDescriptor(this);
        }
    }
}
