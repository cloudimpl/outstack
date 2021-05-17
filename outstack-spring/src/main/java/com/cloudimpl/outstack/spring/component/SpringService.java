/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.runtime.CommandHandler;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;

import java.util.HashSet;
import java.util.Set;

import org.reactivestreams.Publisher;
import com.cloudimpl.outstack.runtime.Handler;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;

import static com.cloudimpl.outstack.spring.component.SpringQueryService.filterEntity;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @param <T>
 * @author nuwan
 */
public class SpringService<T extends RootEntity> implements Function<CloudMessage, Publisher> {

    private static final Set<Class<? extends CommandHandler<?>>> HANDLERS = new HashSet<>();
    private static final Set<Class<? extends Entity>> CMD_ENTITIES = new HashSet<>();
    private final ServiceProvider<T, CloudMessage> serviceProvider;

    public SpringService(EventRepositoryFactory factory) {
        Class<T> root = Util.extractGenericParameter(this.getClass(), SpringService.class, 0);
        serviceProvider = new ServiceProvider<>(root, factory.createRepository(root), factory::createRepository);
        HANDLERS.stream()
                .filter(h -> SpringService.filter(root, h))
                .filter(h -> EntityCommandHandler.class.isAssignableFrom(h))
                .forEach(e -> serviceProvider.registerCommandHandler((Class<? extends EntityCommandHandler>) e));
        HANDLERS.stream()
                .filter(h -> SpringService.filter(root, h))
                .filter(h -> EntityEventHandler.class.isAssignableFrom(h))
                .forEach(e -> serviceProvider.registerEventHandler((Class<? extends EntityEventHandler>) e));

        CMD_ENTITIES.stream().filter(e -> SpringQueryService.filterEntity(root, e))
                .forEach(e -> serviceProvider.registerDefaultCmdHandlersForEntity(e));
    }

    public static void $(Class<? extends CommandHandler<?>> handler) {
        HANDLERS.add(handler);
    }

    public static void $$(Class<? extends Entity> entityType) {
        CMD_ENTITIES.add(entityType);
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

    public static Collection<Class<? extends Entity>> queryEntities(Class<? extends RootEntity> rootType) {
        return CMD_ENTITIES.stream().filter(h -> filterEntity(rootType, h)).collect(Collectors.toList());
    }

    public static Collection<Class<? extends CommandHandler<?>>> handlers(Class<? extends RootEntity> rootType) {
        return HANDLERS.stream().filter(h -> filter(rootType, h)).collect(Collectors.toList());
    }

    public static Collection<Class<? extends Entity>> cmdEntities(Class<? extends RootEntity> rootType) {
        return CMD_ENTITIES.stream().filter(h -> filterEntity(rootType, h)).collect(Collectors.toList());
    }
}
