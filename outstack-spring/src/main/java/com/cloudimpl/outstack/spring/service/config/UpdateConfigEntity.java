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
package com.cloudimpl.outstack.spring.service.config;

import com.cloudimpl.outstack.runtime.AsyncEntityCommandHandler;
import com.cloudimpl.outstack.runtime.AsyncEntityContext;
import com.cloudimpl.outstack.runtime.ChildEntityContext;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.configs.ConfigEntity;
import com.cloudimpl.outstack.runtime.configs.ConfigGroupCreated;
import com.cloudimpl.outstack.runtime.configs.ConfigGroupEntity;
import com.cloudimpl.outstack.runtime.configs.ConfigUpdated;
import com.cloudimpl.outstack.runtime.configs.CreateConfigRequest;
import com.cloudimpl.outstack.runtime.configs.UpdateConfigRequest;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class UpdateConfigEntity extends EntityCommandHandler<ConfigEntity, UpdateConfigRequest, ConfigEntity> {

    @Override
    protected ConfigEntity execute(EntityContext<ConfigEntity> context, UpdateConfigRequest command) {
        ChildEntityContext<ConfigGroupEntity,ConfigEntity> childContext = context.asChildContext();

        ConfigEntity entity = childContext.getEntityById(command.id()).get();
        if (!entity.entityId().equals(command.getConfigName())) {
            entity = childContext.rename(entity.entityId(), command.getConfigName());
            if(command.getValue() != null && !command.getValue().equals(entity.getConfigValue()))
            {
                entity = childContext.update(command.getConfigName(), new ConfigUpdated(command.getGroupName(), command.getConfigName(), command.getValue(), command.getConfigType()));
            }
        } else {
            entity = childContext.update(command.getConfigName(), new ConfigUpdated(command.getGroupName(), command.getConfigName(), command.getValue(), command.getConfigType()));
        }
        return entity;
    }

}
