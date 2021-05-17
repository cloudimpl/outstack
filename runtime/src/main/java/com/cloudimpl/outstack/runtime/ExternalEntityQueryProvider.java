/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <R>
 */
public class ExternalEntityQueryProvider<R extends RootEntity> {

    private final Class<R> type;
    private final String id;
    private final String tenantId;
    private final QueryOperations<R> queryOperations;

    public ExternalEntityQueryProvider(QueryOperations<R> queryOperations, Class<R> type, String id, String tenantId) {
        this.queryOperations = queryOperations;
        this.tenantId = tenantId;
        this.type = type;
        this.id = id;
    }

    public Optional<R> getRoot() {
        return this.queryOperations.getRootById(type, id, tenantId);
    }

    public <T extends ChildEntity<R>> Optional<T> getChild(Class<T> childType, String childId) {
        String tid = id;
        if (!id.startsWith(EventRepository.TID_PREFIX)) {
            tid = getRoot().get().id();
        }
        return this.queryOperations.getChildById(type, tid, childType, childId, tenantId);
    }

    public <T extends ChildEntity<R>> ResultSet<T> getChildsByType(Class<T> childType, Query.PagingRequest pageRequest) {
        String tid = id;
        if (!id.startsWith(EventRepository.TID_PREFIX)) {
            tid = getRoot().get().id();
        }
        return this.queryOperations.getAllChildByType(type, tid, childType, tenantId, pageRequest);
    }

    public <T extends ChildEntity<R>> Collection<T> getChildsByType(Class<T> childType) {
        return getChildsByType(childType, null).getItems();
    }
}
