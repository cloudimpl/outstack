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

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ITenantOptional;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "Roles" , version = "v1")
public class Role extends RootEntity implements ITenantOptional {

    private final String roleName;
    private final String tenantId;

    public Role(String roleName,String tenantId) {
        this.roleName = roleName;
        this.tenantId = tenantId;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public String entityId() {
        return roleName;
    }

    private void applyEvent(RoleCreated created) {

    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "RoleCreated": {
                applyEvent((RoleCreated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "roleName";
    }

}
