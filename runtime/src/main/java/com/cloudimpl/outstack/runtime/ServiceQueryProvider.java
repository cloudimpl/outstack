/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.IQuery;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.handler.DefaultGetEventsQueryHandler;
import com.cloudimpl.outstack.runtime.handler.DefaultGetQueryHandler;
import com.cloudimpl.outstack.runtime.handler.DefaultListQueryHandler;
import com.cloudimpl.outstack.runtime.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.LinkedTreeMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
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
    private final static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);;
    private Supplier<Consumer> injector;
    public ServiceQueryProvider(Class<T> rootType, EventRepositoy<T> eventRepository, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector,Supplier<BiFunction<String, Object, Mono>> requestHandler,Supplier<Consumer> injector) {
        this.rootType = rootType;
        this.injector = injector;
        this.eventRepository = eventRepository;
        contextProvider = new EntityContextProvider<>(rootType, this.eventRepository::loadEntityWithClone, eventRepository::generateTid, eventRepository, queryOperationSelector,requestHandler);
    }

    public void registerQueryHandler(Class<? extends EntityQueryHandler> handlerType) {
        validateHandler(handlerType.getSimpleName().toLowerCase(), rootType, Util.extractGenericParameter(handlerType, EntityQueryHandler.class, 0));
        EntityQueryHandler handler = Util.createObject(handlerType, new Util.VarArg<>(), new Util.VarArg<>());
        injector.get().accept(handler);
        EntityQueryHandler exist = mapQueryHandlers.putIfAbsent(handlerType.getSimpleName().toLowerCase(), handler);
        if (exist != null) {
            throw new ServiceProviderException("query handler {0} already exist ", handlerType.getSimpleName());
        }
    }

    public void registerDefaultQueryHandlersForEntity(Class<? extends Entity> entityType) {
        validateHandler("defaultQueryHandlers", rootType, entityType);
        
        EntityQueryHandler get = Util.createObject(DefaultGetQueryHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType));
        injector.get().accept(get);
        mapQueryHandlers.computeIfAbsent(("Get" + entityType.getSimpleName()).toLowerCase(), s -> get);
        EntityQueryHandler list =  Util.createObject(DefaultListQueryHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType));
        injector.get().accept(list);
        mapQueryHandlers.computeIfAbsent(("List" + entityType.getSimpleName()).toLowerCase(), s -> list);
        EntityQueryHandler events = Util.createObject(DefaultGetEventsQueryHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType));
        injector.get().accept(events);
        mapQueryHandlers.computeIfAbsent(("Get" + entityType.getSimpleName() + "Events").toLowerCase(), s -> events);
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
            } else if (LinkedTreeMap.class.isInstance(input)) {
                return applyQuery(GsonCodecRuntime.decodeTree(QueryWrapper.class, (LinkedTreeMap) input));
            } else {
                return Mono.error(() -> new CommandException("invalid input received. {0}", input));
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
            return Mono.error(thr);
        }

    }

    private Publisher applyQuery(IQuery query) {
        EntityQueryHandler queryHandler = getQueryHandler(query.queryName()).orElseThrow(() -> new QueryException("query {0} not found", query.queryName().toLowerCase()));
        if (AsyncEntityQueryHandler.class.isInstance(queryHandler)) {
            Publisher ret =  AsyncEntityQueryHandler.class.cast(queryHandler).emitAsync(contextProvider, query);
            if(ret instanceof Mono)
            {
                return Mono.from(ret).map(e->encode(query, e));
            }else
            {
                return Flux.from(ret).map(e->encode(query, e));
            }
        } else {
            return Mono.just(queryHandler.emit(contextProvider, query)).map(e->encode(query, e));
        }
    }

     protected static  Object encode(IQuery query, Object reply) {
        if (QueryWrapper.class.isInstance(query)) {
            //return objectMapper.writeValueAsString(reply);
            return objectMapper.convertValue(reply, LinkedHashMap.class);

        } else {
            return reply;
        }
    }
    public void validate(Predicate<String> pred, String name, String error) {
        if (pred.test(name)) {
            throw new ServiceProviderException(error);
        }
    }
}
