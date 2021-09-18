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

/**
 *
 * @author nuwan
 */
public class PolicyCreated extends Event<Policy> {

    private final String policyName;
    private final String policyContext;
    private final String domainOwner;
    private final String domainContext;
    private final String apiContext;
    private final String rootType;
    public PolicyCreated(String policyName,String policyContext,String domainOwner,String domainContext,String apiContext,String rootType) {
        this.policyName = policyName;
        this.policyContext = policyContext;    
        this.domainOwner = domainOwner;
        this.domainContext = domainContext;
        this.apiContext = apiContext;
        this.rootType = rootType;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return Policy.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return Policy.class;
    }

    public String getPolicyContext() {
        return policyContext;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getApiContext() {
        return apiContext;
    }

    public String getRootType() {
        return rootType;
    }

    @Override
    public String entityId() {
        return policyName;
    }

    @Override
    public String rootEntityId() {
        return policyName;
    }

}
