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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class WorkContext {

    private final Map<String, List<String>> contexts;
    private transient BiFunction<String,Object,Mono> rrHandler;
    public WorkContext() {
        contexts = new ConcurrentHashMap<>();
    }

    private  WorkContext(Map<String, List<String>> contexts) {
        this.contexts = contexts;
    }
    
    public void put(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, String value) {
        List<String> list = this.contexts.computeIfAbsent(key, k -> new LinkedList<>());
        synchronized (list) {
            list.add(value);
        }
    }

    protected void setRRHandler(BiFunction<String,Object,Mono> rrHandler)
    {
        this.rrHandler = rrHandler;
    }
    
    public BiFunction<String,Object,Mono> getRRHandler()
    {
        return this.rrHandler;
    }
    
    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, float value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(this.contexts.getOrDefault(key, Collections.singletonList("false")).get(0));
    }

    public void merge(WorkContext context)
    {
        if( context == null || context == this)
        {
            throw new WorkflowException("invalid argument");
        }
        context.contexts.entrySet().stream().forEach(e->e.getValue().stream().forEach(v->this.put(e.getKey(),v)));
    }
    
    @Override
    public WorkContext clone()
    {
        return new WorkContext(new ConcurrentHashMap<>(contexts));
    }
    
    public String getString(String key) {
        List<String> list = this.contexts.getOrDefault(key, null);
        if (list == null) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public int getInt(String key) {
        return Integer.valueOf(this.contexts.getOrDefault(key, Collections.singletonList("0")).get(0));
    }

    public long getLong(String key) {
        return Long.valueOf(this.contexts.getOrDefault(key, Collections.singletonList("0L")).get(0));
    }

    public float getFloat(String key) {
        return Float.valueOf(this.contexts.getOrDefault(key, Collections.singletonList("0.0f")).get(0));
    }

    public double getDouble(String key) {
        return Double.valueOf(this.contexts.getOrDefault(key, Collections.singletonList("0.0d")).get(0));
    }
}
