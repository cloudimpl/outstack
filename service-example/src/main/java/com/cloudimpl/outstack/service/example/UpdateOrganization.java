/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.xventure.projectA.OrganizationCreated;
import com.xventure.projectA.UserCreated;
import com.xventure.projectA.org.Organization;

/**
 *
 * @author nuwan
 */
public class UpdateOrganization extends EntityEventHandler<Organization, UserCreated>{

    @Override
    protected void execute(EntityContext<Organization> context, UserCreated evt) {
         context.create("testorg", new OrganizationCreated("xxx", "testorg", "test"));
    }
    
}
