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

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "CommandHandlers",version = "v1")
public class CommandHandlerEntity extends ChildEntity<MicroService>{
    private String handlerName;
    private String entityName;

    public CommandHandlerEntity(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getEntityName() {
        return entityName;
    }
    
    @Override
    public Class<MicroService> rootType() {
        return MicroService.class;
    }

    @Override
    public String entityId() {
        return handlerName;
    }

    private void applyEvent(CommandHandlerRegistered cmdHandlerRegistered)
    {
        this.handlerName = cmdHandlerRegistered.getHandlerName();
        this.entityName = cmdHandlerRegistered.getEntityName();
    }
    
    @Override
    protected void apply(Event event) {
        switch(event.getClass().getSimpleName())
        {
            case "CommandHandlerRegistered":
            {
                applyEvent((CommandHandlerRegistered)event);
                break;
            }
        }
    }

    @Override
    public String idField() {
        return "handlerName";
    }
    
}
