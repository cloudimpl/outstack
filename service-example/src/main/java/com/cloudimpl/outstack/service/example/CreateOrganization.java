/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.xventure.projectA.OrganizationCreated;
import com.xventure.projectA.org.Organization;

/**
 *
 * @author nuwan
 */
public class CreateOrganization extends EntityCommandHandler<Organization, OrgCreateRequest, Organization>{

    @Override
    protected Organization execute(EntityContext<Organization> context, OrgCreateRequest command) {
        return context.create("testorg", new OrganizationCreated("xxx", "testorg", "test"));
    }
    
}
