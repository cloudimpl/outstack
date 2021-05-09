/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.OrganizationCreated;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;


/**
 *
 * @author nuwan
 */
public class UpdateOrganization extends EntityCommandHandler<Organization, OrganizationCreateRequest,Organization>{

    @Override
    protected Organization execute(EntityContext<Organization> context, OrganizationCreateRequest command) {
        //Optional<Tenant> tenant = context.<Organization>asRootContext().getChildEntityById(Tenant.class, "xxx");
        return context.update(command.getOrgName(), new OrganizationCreated(command.getWebsite(), command.getOrgName()));
    }
 
}
