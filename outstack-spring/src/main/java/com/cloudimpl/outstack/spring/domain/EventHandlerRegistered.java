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
public class EventHandlerRegistered extends Event<EventHandlerEntity>{
    private final String serviceName;
    private final String handlerName;
    private final String entityName;

    public EventHandlerRegistered(String serviceName,String handlerName, String entityName) {
        this.serviceName = serviceName;
        this.handlerName = handlerName;
        this.entityName = entityName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return EventHandlerEntity.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return MicroService.class;
    }

    @Override
    public String entityId() {
        return serviceName;
    }

    @Override
    public String rootEntityId() {
        return serviceName;
    }
    
}
