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
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Id;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "EventHandlers",version = "v1")
public class EventHandlerEntity extends ChildEntity<ServiceModule>{
    @Id
    private String handlerName;
    private String entityName;

    public EventHandlerEntity(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getEntityName() {
        return entityName;
    }
    
    @Override
    public Class<ServiceModule> rootType() {
        return ServiceModule.class;
    }

    @Override
    public String entityId() {
        return handlerName;
    }

    private void applyEvent(EventHandlerRegistered evtHandlerRegistered)
    {
        this.handlerName = evtHandlerRegistered.getHandlerName();
        this.entityName = evtHandlerRegistered.getEntityName();
    }
    
    @Override
    protected void apply(Event event) {
        switch(event.getClass().getSimpleName())
        {
            case "EventHandlerRegistered":
            {
                applyEvent((EventHandlerRegistered)event);
                break;
            }
        }
    }

    @Override
    public String idField() {
        return "handlerName";
    }
    
}
