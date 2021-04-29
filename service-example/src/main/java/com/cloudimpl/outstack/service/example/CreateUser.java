/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.xventure.projectA.UserCreated;
import com.xventure.projectA.enums.OrderType;
import com.xventure.projectA.user.User;

/**
 *
 * @author nuwan
 */
public class CreateUser extends EntityCommandHandler<User, UserCreateReq,User>{

    @Override
    protected User execute(EntityContext<User> context, UserCreateReq command) {
        return context.create("test",new UserCreated("xx","awq", "dad", OrderType.MARKET, "test"));
    }
    
}
