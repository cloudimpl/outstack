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
import com.cloudimpl.outstack.runtime.domainspec.TenantRequirement;

/**
 *
 * @author nuwan
 */
public class ServiceModuleProvisioned extends Event<ServiceModule>{
    private final String  serviceName;
    private final String rootEntity;
    private final String  version;
    private final String  apiContext;
    private final TenantRequirement tenancy;

    public ServiceModuleProvisioned(String serviceName,String rootEntity, String version,String apiContext, TenantRequirement tenancy) {
        this.serviceName = serviceName;
        this.rootEntity = rootEntity;
        this.version = version;
        this.apiContext = apiContext;
        this.tenancy = tenancy;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    public String getVersion() {
        return version;
    }

    public String getApiContext() {
        return apiContext;
    }

    public TenantRequirement getTenancy() {
        return tenancy;
    }
    
    
    @Override
    public Class<? extends Entity> getOwner() {
        return ServiceModule.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return ServiceModule.class;
    }

    @Override
    public String entityId() {
        return this.rootEntity;
    }

    @Override
    public String rootEntityId() {
        return this.rootEntity;
    }
    
}
