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
package com.cloudimpl.outstack.spring.domain;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
public class MicroServiceProvisioned extends Event<MicroService>{
    private final String  serviceName;
    private final String rootEntity;
    private final String  version;
    private final String  apiContext;
    private final boolean tenantService;

    public MicroServiceProvisioned(String serviceName,String rootEntity, String version,String apiContext, boolean tenantService) {
        this.serviceName = serviceName;
        this.rootEntity = rootEntity;
        this.version = version;
        this.apiContext = apiContext;
        this.tenantService = tenantService;
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

    public boolean isTenantService() {
        return tenantService;
    }
    
    
    @Override
    public Class<? extends Entity> getOwner() {
        return MicroService.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return MicroService.class;
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
