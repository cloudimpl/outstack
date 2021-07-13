/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.ServiceModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * @author nuwansa
 */
public class ResourceCache<T> {
    private final Cache<String, Object> map;

    public ResourceCache(int maxSize, Duration evictionDuration) {
        map = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(evictionDuration)
                .build();
    }

    public void put(String id, T resource) {
        map.put(id, resource);
    }

    public <T> Optional<T> get(String id) {
        return Optional.ofNullable((T) map.getIfPresent(id));
    }

    public <T> T get(String id, T given) {
        return (T) map.get(id, i -> {
            return given;
        });
    }

    public <T> Optional<T> remove(String id) {
        Optional<T> optional = Optional.ofNullable((T) map.getIfPresent(id));
        map.invalidate(id);
        return optional;
    }

    public static void main(String[] args) {
//        System.out.println(UUID.randomUUID().toString());
//        System.out.println(UUID.randomUUID().toString());
//        System.out.println(UUID.randomUUID().toString());
//        System.out.println(UUID.randomUUID().toString());
//        System.out.println(UUID.randomUUID().toString());
//        System.out.println(UUID.randomUUID().toString());
//        String s = "trn:restrata:sso:tenant/00fdd02a-c082-421b-b3b8-357c9cbdf4dc/User/ee0495fc-3be5-40a8-a7e5-0a9479835b06/Trip/7662d4bd-ac9e-493e-9794-50d469f4ae28";
//        System.out.println(s.length());
        int i = 0;

        long start = System.currentTimeMillis();
        while (i < 100000000) {
            EntityMetaDetailCache.instance().getEntityMeta(ServiceModule.class);
            i++;
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) / i);
    }
}
