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
public interface QueryOperations<R extends RootEntity>{
    Collection<R> getAllByRootType(Class<R> rootType,String tenantId,Query.PagingRequest paging);
    Optional<R> getRootById(Class<R> rootType,String id,String tenantId);
    <T extends ChildEntity<R>> Optional<T> getChildById(Class<R> rootType,String id,Class<T> childType, String childId,String tenantId);
    <T extends ChildEntity<R>> Collection<T> getAllChildByType(Class<R> rootType,String id,Class<T> childType,String tenantId,Query.PagingRequest paging);
    
}
