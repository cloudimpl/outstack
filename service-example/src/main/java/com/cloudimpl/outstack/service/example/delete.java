/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.xventure.projectA.user.User;

/**
 *
 * @author nuwan
 */
public class delete extends EntityCommandHandler<User,DeleteCommand,String>{

    @Override
    protected String execute(EntityContext<User> context, DeleteCommand command) {
        context.delete(command.tid());
        return "done";
    }
    
}
