/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import reactor.core.publisher.Mono;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 *
 * @author nuwan
 */
public class AsyncRootEntityQueryContext<T extends RootEntity> implements RootEntityQueryContext<T>{
    private final RootEntityContext<T> inst;
    private final BiFunction<String, Object, Mono> requestHandler;

    public AsyncRootEntityQueryContext(RootEntityContext<T> inst,  BiFunction<String, Object, Mono> requestHandler) {
        this.inst = inst;
        this.requestHandler = requestHandler;
    }

    @Override
    public String getTenantId() {
        return inst.getTenantId();
    }
    
    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildEntityById(Class<C> childType, String id) {
        return inst.getChildEntityById(childType, id);
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<Event<C>> getChildEntityEventsById(Class<C> childType, String id, Query.PagingRequest pageRequest) {
        return inst.getChildEntityEventsById(childType, id, pageRequest);
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<C> getAllChildEntitiesByType(Class<C> childType, Query.PagingRequest pageRequest) {
        return inst.getAllChildEntitiesByType(childType, pageRequest);
    }

    @Override
    public Optional<T> getEntity() {
        return inst.getEntity();
    }

    @Override
    public  ResultSet<T> getAll(Query.PagingRequest pagingRequest) {
        return inst.getAll(pagingRequest);
    }

    @Override
    public Optional<T> getEntityById(String id) {
        return inst.getEntityById(id);
    }

    @Override
    public RootEntityQueryContext<T> asRootQueryContext() {
        return inst.asRootQueryContext();
    }

    @Override
    public <R extends RootEntity, K extends ChildEntity<R>> ChildEntityQueryContext<R, K> asChildQueryContext() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <R extends RootEntity> ExternalEntityQueryProvider<R> getEntityQueryProvider(Class<R> rootType) {
        return inst.getEntityQueryProvider(rootType);
    }
    
    @Override
    public ResultSet<Event<T>> getEntityEventsById(String id, Query.PagingRequest pageRequest) {
        return inst.getEntityEventsById(id, pageRequest);
    }

    @Override
    public  AsyncRootEntityQueryContext<T> asAsyncQueryContext() {
        return this;
    }

    @Override
    public RootEntityContext<T> asNonTenantContext(String id) {
        return inst.asNonTenantContext(id);
    }

    public <T> Mono<T> sendRequest(String domainOwner, String domainContext, String version, String serviceName, Object req) {
        return requestHandler.apply(MessageFormat.format("{0}/{1}/{2}/{3}", domainOwner, domainContext, version, serviceName), req);
    }

    @Override
    public boolean isIdExist(String id, String tenantId) {
        return inst.isIdExist(id, tenantId);
    }

    @Override
    public <R extends RootEntity> ExternalEntityQueryProvider<R> getEntityQueryProviderFromTenantList(Class<R> rootType, Collection<String> tenantId) {
        return inst.getEntityQueryProviderFromTenantList(rootType, tenantId);
    }


}
