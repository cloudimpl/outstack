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

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
public class ConfigUpdated extends Event<ConfigEntity> {

    private final String groupName;
    private final String configName;
    private final String configValue;
    private final String configType;
    
    public ConfigUpdated(String groupName,String configName,String configValue, String configType) {
        this.groupName = groupName;
        this.configName = configName;
        this.configValue = configValue;
        this.configType = configType;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getConfigName() {
        return configName;
    }

    public String getConfigValue() {
        return configValue;
    }
    public String getConfigType() {
        return configType;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return ConfigEntity.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return ConfigGroupEntity.class;
    }

    @Override
    public String entityId() {
        return configName;
    }

    @Override
    public String rootEntityId() {
        return groupName;
    }

}
