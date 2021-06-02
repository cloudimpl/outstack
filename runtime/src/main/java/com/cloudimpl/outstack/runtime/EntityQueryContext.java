/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <T>
 */
public interface EntityQueryContext<T extends Entity>{
    
    
    Optional<T> getEntityById(String id);
    
    <R extends RootEntity> RootEntityQueryContext<R> asRootQueryContext();

    
    <R extends RootEntity,K extends ChildEntity<R>> ChildEntityQueryContext<R,K> asChildQueryContext() ;
    
    <R extends RootEntity> AsyncRootEntityQueryContext<R> asAsyncQueryContext();
    
    <R extends RootEntity> ExternalEntityQueryProvider<R> getEntityQueryProvider(Class<R> rootType,String id);
    
    ResultSet<Event<T>> getEntityEventsById(String id,Query.PagingRequest pageRequest);
    
}
