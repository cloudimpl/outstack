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
package com.cloudimpl.outstack.runtime;

/**
 *
 * @author nuwan
 */
public interface IConfigProvider {
    
    default String getConfig(String group,String configName,String defaultValue)
    {
        return defaultValue;
    }
    
    default int getConfig(String group,String configName,int defaultValue)
    {
        return Integer.valueOf(getConfig(group, configName,String.valueOf(defaultValue)));
    }
    
    default long getConfig(String group,String configName,long defaultValue)
    {
        return Long.valueOf(getConfig(group, configName,String.valueOf(defaultValue)));
    }
    
    default boolean getConfig(String group,String configName,boolean defaultValue)
    {
        return Boolean.valueOf(getConfig(group, configName,String.valueOf(defaultValue)));
    }
}
