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
package com.cloudimpl.outstack.runtime.configs;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ITenantOptional;
import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "config-group-entities",version = "v1")
public class ConfigGroupEntity extends RootEntity implements ITenantOptional{

    @Id
    private final String groupName;
    private final String tenantId;
    
    public ConfigGroupEntity(String groupName,String tenantId) {
        this.groupName = groupName;
        this.tenantId = tenantId;
    }

    public String getGroupName() {
        return groupName;
    }
    
    @Override
    public String entityId() {
        return groupName;
    }

     @Override
    public String getTenantId()
    {
        return this.tenantId;
    }
    
    private void applyEvent(ConfigGroupCreated configGroupCreatedEvent) {

    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "ConfigGroupCreated": {
                applyEvent((ConfigGroupCreated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "groupName";
    }

}
