/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.domain.example.TenantCreated;
import com.cloudimpl.outstack.domain.example.commands.TenantCreateRequest;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;

/**
 *
 * @author nuwan
 */
// @EnableFileUpload(mimetypes={})
public class CreateTenant extends EntityCommandHandler<Tenant, TenantCreateRequest,Tenant>{

    @Override
    protected Tenant execute(EntityContext<Tenant> context, TenantCreateRequest command) {
        return context.create(command.getTenantName(), new TenantCreated("xxx", command.getTenantName(),command.getOrgName()));
    }
    
}
