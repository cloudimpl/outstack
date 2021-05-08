/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.handler;

import com.cloudimpl.outstack.runtime.EntityQueryContext;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class DefaultListQueryHandler<T extends Entity> extends EntityQueryHandler<T,QueryByIdRequest, Collection<T>>{
    
    public DefaultListQueryHandler(Class<?> type)
    {
        super((Class<T>)type);
    }
    
    @Override
    protected Collection<T> execute(EntityQueryContext<T> context, QueryByIdRequest query) {
        if(RootEntity.isMyType(entityType))
        {
            throw new UnsupportedOperationException("global list not supported yet");
        }
        return context.asChildQueryContext().getAllByEntityType((Class<ChildEntity<RootEntity>>)entityType).stream().map(e->(T)e).collect(Collectors.toList());
    }
    
}