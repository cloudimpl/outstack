/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.spring.component.SpringService;
import com.xventure.projectA.user.User;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "UserService")
@Router(routerType = RouterType.ROUND_ROBIN)
public class UserService extends SpringService<User>{
    
    public UserService(EventRepositoy<User> eventRepository, ResourceHelper resourceHelper) {
        super(eventRepository, resourceHelper);
        registerCommandHandler(CreateUser.class);
        registerCommandHandler(CreateOrganization.class);
        registerEventHandler(UpdateOrganization.class);
    }
    
}
