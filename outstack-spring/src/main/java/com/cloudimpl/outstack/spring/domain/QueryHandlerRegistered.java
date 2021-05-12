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
public class QueryHandlerRegistered extends Event<CommandHandlerEntity>{
    private final String rootEntity;
    private final String handlerName;
    private final String entityName;

    public QueryHandlerRegistered(String handlerName, String entityName,String rootEntity) {
        this.rootEntity = rootEntity;
        this.handlerName = handlerName;
        this.entityName = entityName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return QueryHandlerEntity.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return MicroService.class;
    }

    @Override
    public String entityId() {
        return handlerName;
    }

    @Override
    public String rootEntityId() {
        return rootEntity;
    }
    
}
