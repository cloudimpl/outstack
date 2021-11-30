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
package com.cloudimpl.outstack.workflow;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public class WorkContext {
    private final Map<String,String> contexts = new HashMap<>();
    
    public void put(String key, boolean value)
    {
        this.contexts.put(key, String.valueOf(value));
    }

    public void put(String key, String value)
    {
        this.contexts.put(key, value);
    }

    public void put(String key, int value)
    {
        this.contexts.put(key, String.valueOf(value));
    }

    public void put(String key, long value)
    {
        this.contexts.put(key, String.valueOf(value));
    }

    public void put(String key, float value)
    {
        this.contexts.put(key, String.valueOf(value));
    }

    public void put(String key, double value)
    {
        this.contexts.put(key, String.valueOf(value));
    }

    public boolean getBoolean(String key)
    {
        return Boolean.valueOf(this.contexts.getOrDefault(key, "false"));
    }

    public String getString(String key)
    {
        return this.contexts.getOrDefault(key, null);
    }

    public int getInt(String key)
    {
        return Integer.valueOf(this.contexts.getOrDefault(key, "0"));
    }

    public  long getLong(String key)
    {
        return Long.valueOf(this.contexts.getOrDefault(key, "0L"));
    }

    public float getFloat(String key)
    {
        return Float.valueOf(this.contexts.getOrDefault(key, "0.0f"));
    }

    public double getDouble(String key)
    {
        return Double.valueOf(this.contexts.getOrDefault(key, "0.0d"));
    }
}
