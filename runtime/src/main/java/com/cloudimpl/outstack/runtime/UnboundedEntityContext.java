package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.*;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UnboundedEntityContext<T extends RootEntity> extends EntityContext<T> implements EntityQueryContext {
private EntityContextProvider entityContextProvider;
    private final BiFunction<String, Object, Mono> requestHandler;
    public static final CommandResponse OK = new CommandResponse("OK");

    public UnboundedEntityContext(EntityContextProvider entityContextProvider, Class<T> entityType, String tenantId, Supplier<String> idGenerator, Optional<CRUDOperations> crudOperations, QueryOperations<?> queryOperation, Optional<Consumer<Event>> eventPublisher,
                                  Consumer<Object> validator, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector, String version, BiFunction<String, Object, Mono> requestHandler) {
        super(entityType, tenantId, Optional.empty(), idGenerator, crudOperations, queryOperation, eventPublisher, validator, queryOperationSelector, version);
        this.entityContextProvider = entityContextProvider;
        this.requestHandler = requestHandler;
    }

    @Override
    public T create(String id, Event<T> event) {
        EntityIdHelper.validateEntityId(id);
        EntityContext entityContext = (EntityContext) ((EntityContextProvider.UnboundedTransaction)getTx()).getTransaction(id).getContext(getEntityMeta().getType());
        return (T) entityContext.asRootContext().create(id, event);
    }

    @Override
    public T update(String id, Event<T> event) {

        EntityContext entityContext = (EntityContext) ((EntityContextProvider.UnboundedTransaction)getTx()).getTransaction(id).getContext(getEntityMeta().getType());
        return (T) entityContext.asRootContext().update(id, event);
    }

    @Override
    public T delete(String id) {

        EntityContext entityContext = (EntityContext) ((EntityContextProvider.UnboundedTransaction)getTx()).getTransaction(id).getContext(getEntityMeta().getType());
        return (T) entityContext.asRootContext().delete(id);
    }

    @Override
    public T rename(String id, String newId) {

        EntityContext entityContext = (EntityContext) ((EntityContextProvider.UnboundedTransaction)getTx()).getTransaction(id).getContext(getEntityMeta().getType());
        return (T) entityContext.asRootContext().rename(id, newId);
    }

    public UnboundedEntityContext<T> asUnboundedEntityContext() {
        return this;
    }

    @Override
    public Optional<T> getEntityById(String id) {
        return this.<T>getQueryOperations().getRootById(entityType, id, getTenantId());
    }

    @Override
    public ResultSet<Event> getEntityEventsById(String id, Query.PagingRequest pageRequest) {
        return null;
    }

    @Override
    public ExternalEntityQueryProvider getEntityQueryProvider(Class rootType, String tenantId) {
        return null;
    }

    @Override
    public ExternalEntityQueryProvider getEntityQueryProvider(Class rootType) {
        return null;
    }

    @Override
    public AsyncRootEntityQueryContext asAsyncQueryContext() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ChildEntityQueryContext asChildQueryContext() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public RootEntityQueryContext asRootQueryContext() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <R extends RootEntity> RootEntityContext<R> asRootContext() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <R extends RootEntity> AsyncEntityContext<R> asAsyncEntityContext() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <R extends RootEntity, K extends ChildEntity<R>> ChildEntityContext<R, K> asChildContext() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public <T> Mono<T> sendRequest(String domainOwner, String domainContext, String version, String serviceName, Object req) {
        return requestHandler.apply(MessageFormat.format("{0}/{1}/{2}/{3}", domainOwner, domainContext, version, serviceName), req);
    }

    public <R extends  RootEntity> RootEntityContext<R> asNonTenantContext(String rootId) {
        EntityContext entityContext = (EntityContext) ((EntityContextProvider.UnboundedTransaction)getTx()).getNonTenantTransaction(rootId).getContext(getEntityMeta().getType());
        return entityContext.asRootContext();
    }

}
