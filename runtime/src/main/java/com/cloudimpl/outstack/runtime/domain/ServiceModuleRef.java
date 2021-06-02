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

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "ServiceModuleRefs",version = "v1")
public class ServiceModuleRef extends ChildEntity<DomainContext> {

    private final String serviceRef;

    public ServiceModuleRef(String serviceRef) {
        this.serviceRef = serviceRef;
    }

    @Override
    public Class<DomainContext> rootType() {
        return DomainContext.class;
    }

    @Override
    public String entityId() {
        return serviceRef;
    }

    public String getServiceRef() {
        return serviceRef;
    }

    private void applyEvent(ServiceModuleRefCreated refCreated) {

    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "ServiceModuleRefCreated": {
                applyEvent((ServiceModuleRefCreated)event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "serviceRef";
    }

}
