/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.HashSet;
import java.util.Set;
import org.reactivestreams.Publisher;
import com.cloudimpl.outstack.runtime.Handler;
import com.cloudimpl.outstack.runtime.ServiceQueryProvider;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class SpringQueryService<T extends RootEntity> implements Function<CloudMessage, Publisher> {

    private static final Set<Class<? extends EntityQueryHandler<?, ?, ?>>> HANDLERS = new HashSet<>();
    private static final Set<Class<? extends Entity>> QUERY_ENTITIES = new HashSet<>();
    private final ServiceQueryProvider<T, CloudMessage> serviceProvider;

    public SpringQueryService(EventRepositoryFactory factory) {
        Class<T> root = Util.extractGenericParameter(this.getClass(), SpringQueryService.class, 0);
        serviceProvider = new ServiceQueryProvider<>(root, factory.createOrGetRepository(root),factory::createOrGetRepository);

        HANDLERS.stream()
                .filter(h -> SpringQueryService.filter(root, h))
                .filter(h -> EntityQueryHandler.class.isAssignableFrom(h))
                .forEach(e -> serviceProvider.registerQueryHandler((Class<? extends EntityQueryHandler>) e));

        QUERY_ENTITIES.stream().filter(e -> SpringQueryService.filterEntity(root, e))
                .forEach(e -> serviceProvider.registerDefaultQueryHandlersForEntity(e));
    }

    public static void $(Class<? extends EntityQueryHandler<?, ?, ?>> handler) {
        HANDLERS.add(handler);
    }

    public static void $$(Class<? extends Entity> entityType) {
        QUERY_ENTITIES.add(entityType);
    }

    @Override
    public Publisher apply(CloudMessage msg) {
        
        return serviceProvider.apply(msg.data());
    }

    public static boolean filter(Class<? extends RootEntity> rootType, Class<? extends Handler<?>> handlerType) {
        Class<? extends Entity> entityType = Util.extractGenericParameter(handlerType, handlerType.getSuperclass(), 0);
        Class<? extends Entity> root = RootEntity.isMyType(entityType) ? entityType : Util.extractGenericParameter(entityType, ChildEntity.class, 0);
        return rootType == root;
    }

    public static boolean filterEntity(Class<? extends RootEntity> rootType, Class<? extends Entity> entityType) {
        if (RootEntity.isMyType(entityType)) {
            return rootType == entityType;
        }
        Class<? extends RootEntity> root = Util.extractGenericParameter(entityType, ChildEntity.class, 0);
        return root == rootType;

    }

    public static Collection<Class<? extends Handler<?>>> handlers(Class<? extends RootEntity> rootType) {
        return HANDLERS.stream().filter(h -> filter(rootType, h)).collect(Collectors.toList());
    }

    public static Collection<Class<? extends Entity>> queryEntities(Class<? extends RootEntity> rootType) {
        return QUERY_ENTITIES.stream().filter(h -> filterEntity(rootType, h)).collect(Collectors.toList());
    }

}
