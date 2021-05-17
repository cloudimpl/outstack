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

/**
 *
 * @author nuwan
 * @param <R>
 * @param <T>
 */
public interface ChildEntityQueryContext<R extends RootEntity,T extends ChildEntity<R>> extends EntityQueryContext<T>{
     <R extends RootEntity> R getRoot();
     ResultSet<T> getAllByEntityType(Class<T> type,Query.PagingRequest pageReq);
     default Collection<T> getAllByEntityType(Class<T> type)
     {
         return getAllByEntityType(type, null).getItems();
     }
}
