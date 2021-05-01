/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.xventure.projectA.OrganizationCreated;
import com.xventure.projectA.user.User;

/**
 *
 * @author nuwan
 */
public class Event2 extends EntityEventHandler<User, OrganizationCreated>{

    @Override
    protected void execute(EntityContext<User> context, OrganizationCreated event) {
        context.update(event.entityId(), new OrganizationCreated("yyy", "event.en", userId))
    }
    
}
