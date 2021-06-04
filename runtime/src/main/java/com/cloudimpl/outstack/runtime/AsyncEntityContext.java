/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class AsyncEntityContext<T extends RootEntity> extends RootEntityContext<T> {

    private final BiFunction<String, Object, Mono> requestHandler;

    public AsyncEntityContext(Class<T> entityType, String tid, String tenantId, Optional<EntityProvider<? extends RootEntity>> entitySupplier, Supplier<String> idGenerator,
            Optional<CRUDOperations> crudOperations, QueryOperations queryOperation, Optional<Consumer<Event>> eventPublisher,
            Consumer<Object> validator, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector, String version, BiFunction<String, Object, Mono> requestHandler) {
        super(entityType, tid, tenantId, entitySupplier, idGenerator, crudOperations, queryOperation, eventPublisher, validator, queryOperationSelector, version);
        this.requestHandler = requestHandler;
    }

    public <C extends ChildEntity<T>> C create(Class<C> type, String rootId, String id, Event<C> event) {
        ChildEntityContext childContext = (ChildEntityContext) getTx().getContext(type);
        return (C) childContext.asChildContext().create(id, event);
    }

    @Override
    public AsyncEntityContext<T> asAsyncEntityContext() {
        return this;
    }

    public <T> Mono<T> sendRequest(String domainOwner, String domainContext, String version, String serviceName, Object req) {
        return requestHandler.apply(MessageFormat.format("{0}/{1}/{2}/{3}", domainOwner, domainContext, version, serviceName), req);
    }
}
