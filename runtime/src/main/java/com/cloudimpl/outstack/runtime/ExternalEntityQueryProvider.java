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
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author nuwan
 * @param <R>
 */
public class ExternalEntityQueryProvider<R extends RootEntity> {

    private final Class<R> type;
    private final String tenantId;
    private final QueryOperations<R> queryOperations;

    public ExternalEntityQueryProvider(QueryOperations<R> queryOperations, Class<R> type,String tenantId) {
        this.queryOperations = queryOperations;
        this.tenantId = tenantId;
        this.type = type;
    }

    public ResultSet<R> getAllRootByType(Query.PagingRequest pageRequest)
    {
        return this.queryOperations.getAllByRootType(type, tenantId, pageRequest);
    }
    
    public Optional<R> getRoot(String id) {
        return this.queryOperations.getRootById(type, id, tenantId);
    }

    public <T extends ChildEntity<R>> Optional<T> getChild(String rootId,Class<T> childType, String childId) {
        String tid = rootId;
        if (!tid.startsWith(EventRepositoy.TID_PREFIX)) {
            tid = getRoot(rootId).get().id();
        }
        return this.queryOperations.getChildById(type, tid, childType, childId, tenantId);
    }

    public <T extends ChildEntity<R>> ResultSet<T> getChildsByType(String rootId,Class<T> childType, Query.PagingRequest pageRequest) {
        String tid = rootId;
        if (!rootId.startsWith(EventRepositoy.TID_PREFIX)) {
            tid = getRoot(rootId).get().id();
        }
        return this.queryOperations.getAllChildByType(type, tid, childType, tenantId, pageRequest);
    }

    public <T extends ChildEntity<R>> Collection<T> getChildsByType(String rootId,Class<T> childType) {
        return getChildsByType(rootId,childType, Query.PagingRequest.EMPTY).getItems();
    }
}
