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
import java.util.List;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <R>
 */
public interface QueryOperations<R extends RootEntity>{
    ResultSet<R> getAllByRootType(Class<R> rootType,String tenantId,Query.PagingRequest paging);
    ResultSet<R> getAllByRootType(Class<R> rootType, Collection<String> tenantId, Query.PagingRequest paging);
    <K> K executeRawQuery(String rawQuery);
    boolean isIdExist(String id,String tenantId);
    Optional<R> getRootById(Class<R> rootType,String id,String tenantId);
    <T extends ChildEntity<R>> Optional<T> getChildById(Class<R> rootType,String id,Class<T> childType, String childId,String tenantId);
    <T extends ChildEntity<R>> ResultSet<T> getAllChildByType(Class<R> rootType,String id,Class<T> childType,String tenantId,Query.PagingRequest paging);
    <T extends ChildEntity<R>> ResultSet<T> getAllChildByType(Class<R> rootType,String id,Class<T> childType,Collection<String> tenantId,Query.PagingRequest paging);
   ResultSet<Event<R>>  getEventsByRootId(Class<R> rootType,String rootId,String tenantId,Query.PagingRequest paging);
    <T extends ChildEntity<R>> ResultSet<Event<T>> getEventsByChildId(Class<R> rootType,String id,Class<T> childType, String childId,String tenantId,Query.PagingRequest paging);

}
