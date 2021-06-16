/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.spring.component.SpringService;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "OrganizationService")
@Router(routerType = RouterType.ROUND_ROBIN)
public class OrganizationService extends SpringService<Organization>{
    static{
        $(CreateOrganization.class);
        $(UploadOrganizationLogo.class);
        $(UpdateOrganization.class);
        $(CreateTenant.class);
        $(UploadTenantLogo.class);
        $(ServiceAsyncHandler.class);
        $$(Organization.class);
        $$(Tenant.class); // auto bind for DeleteTenant and RenameTenant command handlers
    }
    
    public OrganizationService(EventRepositoryFactory eventRepoFactory) {
        super(eventRepoFactory);
    }
    
}
