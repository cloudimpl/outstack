/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.handler;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.RenameCommand;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class DefaultRenameCommandHandler<T extends Entity> extends EntityCommandHandler<T, RenameCommand,T>{

    public DefaultRenameCommandHandler(Class<T> type) {
        super(type);
    }

    
    @Override
    protected T execute(EntityContext<T> context, RenameCommand command) {
         EntityIdHelper.validateEntityId(command.getEntityId());
         return context.rename(command.id(),command.getEntityId());
    }
 
}
