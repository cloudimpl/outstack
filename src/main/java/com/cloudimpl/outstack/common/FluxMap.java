/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author nuwan
 */
public class FluxMap<K,V> {
    private final Map<K,Value<V>> map = new ConcurrentHashMap<>();
    private final AtomicLong version = new AtomicLong();
    
    public void put(K key,V value)
    {
        this.map.put(key, createValue(value));
    }
    
    private Value<V> createValue(V v)
    {
        return new Value<>(version.incrementAndGet(),v);
    }
    
    public static final class Value<V>
    {
        private final long version;
        private final V value;

        public Value(long version, V value) {
            this.version = version;
            this.value = value;
        }

        public V getValue() {
            return value;
        }

        public long getVersion() {
            return version;
        }
        
    }
}
