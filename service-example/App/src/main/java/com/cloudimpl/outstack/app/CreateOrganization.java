/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.OrganizationCreated;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author nuwan
 */
public class CreateOrganization extends EntityCommandHandler<Organization, OrganizationCreateRequest, Organization> {

    @Value("${outstack.apiContext}")
    private String domainOwner;

    @Override
    protected Organization execute(EntityContext<Organization> context, OrganizationCreateRequest command) {
        //Optional<Tenant> tenant = context.<Organization>asRootContext().getChildEntityById(Tenant.class, "xxx");
        return context.create(command.getOrgName(), new OrganizationCreated(command.getWebsite(), command.getOrgName()));
    }

}
