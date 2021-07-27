/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <T>
 */
public interface RootEntityQueryContext<T extends RootEntity> extends EntityQueryContext<T> {

    <C extends ChildEntity<T>> Optional<C> getChildEntityById(Class<C> childType, String id);
    
    <C extends ChildEntity<T>> ResultSet<Event<C>> getChildEntityEventsById(Class<C> childType, String id,Query.PagingRequest pageRequest);

    <C extends ChildEntity<T>> ResultSet<C> getAllChildEntitiesByType(Class<C> childType,Query.PagingRequest pageRequest);
    
    default <C extends ChildEntity<T>> Collection<C> getAllChildEntitiesByType(Class<C> childType)
    {
        return getAllChildEntitiesByType(childType, null).getItems();
    }
    
    Optional<T> getEntity();
    
    <T extends RootEntity> ResultSet<T> getAll(Query.PagingRequest pagingRequest);
   
    RootEntityQueryContext<T> asNonTenantContext(String id);

    // <E extends RootEntity> getQueryProvider(Class<E> rootType);
}
