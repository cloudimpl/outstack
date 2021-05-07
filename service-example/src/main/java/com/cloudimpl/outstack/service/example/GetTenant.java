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
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;

/**
 *
 * @author nuwan
 */
public class GetTenant extends EntityQueryHandler<Tenant,QueryByIdRequest, Tenant>{

    @Override
    protected Tenant execute(EntityQueryContext<Tenant> context, QueryByIdRequest query) {
        return context.getEntityById(query.id()).orElseThrow();
    }
    
}
