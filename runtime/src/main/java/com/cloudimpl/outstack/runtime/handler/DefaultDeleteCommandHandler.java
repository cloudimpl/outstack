/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.handler;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.domainspec.DeleteCommand;
import com.cloudimpl.outstack.runtime.domainspec.Entity;

/**
 *
 * @author nuwan
 */
public class DefaultDeleteCommandHandler<T extends Entity> extends EntityCommandHandler<T, DeleteCommand,T>{

    public DefaultDeleteCommandHandler(Class<T> type) {
        super(type);
    }

    
    @Override
    protected T execute(EntityContext<T> context, DeleteCommand command) {
         return context.delete(command.id());
    }
 
}
