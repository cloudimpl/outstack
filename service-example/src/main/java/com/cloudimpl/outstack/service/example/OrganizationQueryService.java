/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.spring.component.SpringQueryService;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "OrganizationQueryService")
@Router(routerType = RouterType.ROUND_ROBIN)
public class OrganizationQueryService extends SpringQueryService<Organization>{
    static{
        $(AsynQueryHandler.class);
        $$(Organization.class); //auto binding for GetOrganization and ListOrganization query handlers
        $$(Tenant.class);       //auto binding for GetTenant and ListTenant query handlers
    }
    
    public OrganizationQueryService(EventRepositoryFactory eventRepoFactory) {
        super(eventRepoFactory);
    }
    
}
