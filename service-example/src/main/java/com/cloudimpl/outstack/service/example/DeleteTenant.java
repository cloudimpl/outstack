/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.domainspec.DeleteCommand;


/**
 *
 * @author nuwan
 */
public class DeleteTenant extends EntityCommandHandler<Tenant, DeleteCommand,Tenant>{

    @Override
    protected Tenant execute(EntityContext<Tenant> context, DeleteCommand command) {
         return context.delete(command.id());
    }
 
}
