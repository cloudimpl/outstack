/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.StreamProcessor;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.StreamEvent;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nuwan
 */
public interface EventRepositoryFactory {

    public static final Map<Class<? extends RootEntity>, EventRepositoy<? extends RootEntity>> mapRepos = new ConcurrentHashMap<>();

    public static final StreamProcessor<StreamEvent> eventStream = new StreamProcessor<>();

    <T extends RootEntity> EventRepositoy<T> createOrGetRepository(Class<T> rootType);

    public static <T extends RootEntity> Optional<EventRepositoy<T>> getRepository(Class<T> rootType) {
        return Optional.ofNullable((EventRepositoy<T>) mapRepos.get(rootType));
    }

    public static StreamProcessor<StreamEvent> getEventStream() {
        return eventStream;
    }
}
