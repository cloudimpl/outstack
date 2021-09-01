/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nuwan
 */
public interface EventRepositoryFactory {
    public static  final Map<Class<? extends RootEntity>, EventRepositoy<? extends RootEntity>> mapRepos = new ConcurrentHashMap<>();
    
    <T extends RootEntity> EventRepositoy<T> createOrGetRepository(Class<T> rootType);
}
