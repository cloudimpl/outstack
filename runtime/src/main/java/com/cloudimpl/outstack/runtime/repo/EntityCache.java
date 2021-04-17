/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.function.Function;

/**
 *
 * @author nuwansa
 */
public class EntityCache {
    private final Cache<String,Entity> map;

    public EntityCache(int maxSize,Duration evictionDuration) {
        map = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(evictionDuration)
                .build();
    }
    
    public void put(String id,Entity entity)
    {
        map.put(id, entity);
    }
    
    public <T extends Entity> T get(String id,Function<? super String,? extends Entity> mapper)
    {
        return (T) map.get(id, mapper);
    }
}
