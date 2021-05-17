/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.handler;

import com.cloudimpl.outstack.runtime.EntityQueryContext;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class DefaultGetEventsQueryHandler<T extends Entity> extends EntityQueryHandler<T,QueryByIdRequest, ResultSet<Event<T>>>{
    
    public DefaultGetEventsQueryHandler(Class<?> type)
    {
        super((Class<T>)type);
    }
    
    @Override
    protected ResultSet<Event<T>> execute(EntityQueryContext<T> context, QueryByIdRequest query) {
        ResultSet<T> out;
        return context.getEntityEventsById(query.id(),query.getPagingReq());
       
    }
    
}