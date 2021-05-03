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
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.util.Util;
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
    }
    public OrganizationService(EventRepositoryFactory eventRepoFactory, ResourceHelper resourceHelper) {
        super(eventRepoFactory, resourceHelper);
    }
    
}
