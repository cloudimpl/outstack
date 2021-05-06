/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.runtime.EntityQueryContext;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdReuuest;

/**
 *
 * @author nuwan
 */
public class GetTenant extends EntityQueryHandler<Tenant,QueryByIdReuuest, Tenant>{

    @Override
    protected Tenant execute(EntityQueryContext<Tenant> context, QueryByIdReuuest query) {
        return context.getById(query.id()).orElseThrow();
    }
    
}
