/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.restrata.platform.Organization;
import com.restrata.platform.commands.OrganizationCreateRequest;
import com.restrata.platform.events.OrganizationCreated;

/**
 *
 * @author nuwan
 */
public class CreateOrganization extends EntityCommandHandler<Organization, OrganizationCreateRequest,Organization>{

    @Override
    protected Organization execute(EntityContext<Organization> context, OrganizationCreateRequest command) {
        return context.create(command.getOrgName(), new OrganizationCreated("xxx", command.getOrgName()));
    }
    
}
