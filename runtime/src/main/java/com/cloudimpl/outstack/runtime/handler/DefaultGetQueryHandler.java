/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.handler;

import com.cloudimpl.outstack.runtime.EntityQueryContext;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class DefaultGetQueryHandler<T extends Entity> extends EntityQueryHandler<T, QueryByIdRequest, T>{
    public DefaultGetQueryHandler(Class<?> type) {
        super((Class<T>)type);
    }

    
    @Override
    protected T execute(EntityQueryContext<T> context, QueryByIdRequest query) {
        return context.getEntityById(query.id())
                .orElseThrow(()->new DomainEventException("entity {0}:{1} not found", entityType.getSimpleName(),query.id()));
    }
    
}
