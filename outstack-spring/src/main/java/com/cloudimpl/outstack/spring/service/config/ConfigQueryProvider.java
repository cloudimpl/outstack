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

import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.configs.ConfigEntity;
import com.cloudimpl.outstack.runtime.configs.ConfigGroupEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author nuwan
 */
@Component
public class ConfigQueryProvider {                         

    private static final ConfigQueryProvider inst = new ConfigQueryProvider();
    
    private EventRepositoy<ConfigGroupEntity> eventRepo;
    
    
    public static ConfigQueryProvider getInstance()
    {
        return inst;
    }
    
    public String getConfig(String configGroup,String configName,String defaultValue)
    {
       return inst.getRawConfig(null, configGroup, configName, defaultValue);
    }
    
    public String getConfig(String tenantId,String configGroup,String configName,String defaultValue)
    {
       return inst.getRawConfig(tenantId, configGroup, configName, defaultValue);
    }
    
    public int getConfig(String tenantId,String configGroup,String configName,int defaultValue)
    {
       String val = inst.getRawConfig(tenantId, configGroup, configName, null);
       if(val == null)
           return defaultValue;
       return Integer.valueOf(val);
    }
    
    public int getConfig(String configGroup,String configName,int defaultValue)
    {
        return getConfig(null, configGroup, configName, defaultValue);
    }
    
    public boolean getConfig(String tenantId,String configGroup,String configName,boolean defaultValue)
    {
       String val = inst.getRawConfig(tenantId, configGroup, configName, null);
       if(val == null)
           return defaultValue;
       return Boolean.valueOf(val);
    }
    
    public boolean getConfig(String configGroup,String configName,boolean defaultValue)
    {
        return getConfig(null, configGroup, configName, defaultValue);
    }
    
    public long getConfig(String tenantId,String configGroup,String configName,long defaultValue)
    {
       String val = inst.getRawConfig(tenantId, configGroup, configName, null);
       if(val == null)
           return defaultValue;
       return Long.valueOf(val);
    }
    
    public long getConfig(String configGroup,String configName,long defaultValue)
    {
        return getConfig(null, configGroup, configName, defaultValue);
    }
    
    private String getRawConfig(String tenantId,String configGroup,String configName,String defaultValue)
    {
        Optional<ConfigGroupEntity> cg = eventRepo.getRootById(ConfigGroupEntity.class,configGroup,tenantId);
        if(cg.isEmpty())
            return defaultValue;
        Optional<ConfigEntity> cf =  eventRepo.getChildById(ConfigGroupEntity.class, cg.get().id(),ConfigEntity.class,configName, tenantId);
        if(cf.isEmpty())
            return defaultValue;
        return cf.get().getConfigValue();
    }
    
    public void setEventRepositroy(EventRepositoryFactory factory)
    {
        eventRepo = factory.createOrGetRepository(ConfigGroupEntity.class);
    }
            
}
