/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.IQuery;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.handler.DefaultGetEventsQueryHandler;
import com.cloudimpl.outstack.runtime.handler.DefaultGetQueryHandler;
import com.cloudimpl.outstack.runtime.handler.DefaultListQueryHandler;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class ServiceQueryProvider<T extends RootEntity, R> implements Function<Object, Publisher<?>> {

    private final Map<String, EntityQueryHandler> mapQueryHandlers = new HashMap<>();

    private final Class<T> rootType;
    private final EventRepositoy<T> eventRepository;
    private final EntityContextProvider<T> contextProvider;

    public ServiceQueryProvider(Class<T> rootType, EventRepositoy<T> eventRepository, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector) {
        this.rootType = rootType;
        this.eventRepository = eventRepository;
        contextProvider = new EntityContextProvider<>(rootType,this.eventRepository::loadEntityWithClone, eventRepository::generateTid, eventRepository, queryOperationSelector);
    }

    public void registerQueryHandler(Class<? extends EntityQueryHandler> handlerType) {
        validateHandler(handlerType.getSimpleName().toLowerCase(), rootType, Util.extractGenericParameter(handlerType, EntityQueryHandler.class, 0));
        EntityQueryHandler exist = mapQueryHandlers.putIfAbsent(handlerType.getSimpleName().toLowerCase(), Util.createObject(handlerType, new Util.VarArg<>(), new Util.VarArg<>()));
        if (exist != null) {
            throw new ServiceProviderException("query handler {0} already exist ", handlerType.getSimpleName());
        }
    }

    public void registerDefaultQueryHandlersForEntity(Class<? extends Entity> entityType) {
        validateHandler("defaultQueryHandlers", rootType, entityType);
        mapQueryHandlers.computeIfAbsent(("Get" + entityType.getSimpleName()).toLowerCase(), s -> Util.createObject(DefaultGetQueryHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType)));
        mapQueryHandlers.computeIfAbsent(("List" + entityType.getSimpleName()).toLowerCase(), s -> Util.createObject(DefaultListQueryHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType)));
        mapQueryHandlers.computeIfAbsent(("Get" + entityType.getSimpleName()+"Events").toLowerCase(), s -> Util.createObject(DefaultGetEventsQueryHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType)));
    }

    public Optional<EntityQueryHandler> getQueryHandler(String name) {
        return Optional.ofNullable(mapQueryHandlers.get(name.toLowerCase()));
    }

    public static void validateHandler(String name, Class<? extends RootEntity> rootType, Class<? extends Entity> type) {
        if (RootEntity.isMyType(type)) {
            if (type != rootType) {
                throw new ServiceProviderException("handler {0} root entity type {1} not matched with service provider type {2}", name, type.getName(), rootType.getName());
            }
        } else {
            Class<? extends RootEntity> root = Util.extractGenericParameter(type, ChildEntity.class, 0);
            if (root != rootType) {
                throw new ServiceProviderException("handler {0} root entity type {1} not matched with service provider type {2}", name, root.getName(), rootType.getName());
            }
        }

    }

    @Override
    public Publisher apply(Object input) {
        try {
            if (IQuery.class.isInstance(input)) {
                return applyQuery((IQuery) input);
            } else {
                return Mono.error(() -> new CommandException("invalid input received. {0}", input));
            }
        }catch(Throwable thr)
        {
            thr.printStackTrace();
            return Mono.error(thr);
        }

    }

    private Publisher applyQuery(IQuery query) {
        return Mono.just(getQueryHandler(query.queryName()).orElseThrow(() -> new QueryException("query {0} not found", query.queryName().toLowerCase())).emit(contextProvider, query))
                .map(ct -> ct.getTx().getReply());
    }

    public void validate(Predicate<String> pred, String name, String error) {
        if (pred.test(name)) {
            throw new ServiceProviderException(error);
        }
    }
}
