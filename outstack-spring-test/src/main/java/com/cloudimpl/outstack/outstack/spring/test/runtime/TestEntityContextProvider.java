package com.cloudimpl.outstack.outstack.spring.test.runtime;

import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EntityProvider;
import com.cloudimpl.outstack.runtime.QueryOperations;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

import java.util.function.Function;
import java.util.function.Supplier;

public class TestEntityContextProvider<T extends RootEntity> extends EntityContextProvider<T> {

    private EntityContext<?> context;
    private Transaction<T> transaction;

    public TestEntityContextProvider(Class<T> type, EntityProvider entityProvider, Supplier<String> idGenerator,
                                     QueryOperations<T> queryOperation, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector) {
        super(type, entityProvider, idGenerator, queryOperation, queryOperationSelector);
    }

    @Override
    public Transaction<T> createWritableTransaction(String rootTid, String tenantId) {
        this.transaction = super.createWritableTransaction(rootTid, tenantId);
        this.context = transaction.getContext(this.type);
        return transaction;
    }

    public EntityContext<?> getContext() {
        return context;
    }

    public Transaction<T> getTransaction() {
        return transaction;
    }
}
