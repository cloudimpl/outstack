/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.spring.component.SpringService;

/**
 *
 * @author nuwan
 */
public class OrganizationService extends SpringService<Organization>{
    static{
        $(CreateOrganization.class);
    }
    public OrganizationService(EventRepositoy<Organization> eventRepository, ResourceHelper resourceHelper) {
        super(eventRepository, resourceHelper);
    }
    
}
