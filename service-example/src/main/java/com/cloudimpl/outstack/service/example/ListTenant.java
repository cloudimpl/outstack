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
import java.util.Collection;

/**
 *
 * @author nuwan
 */
public class ListTenant extends EntityQueryHandler<Tenant,QueryByIdRequest, Collection<Tenant>>{

    @Override
    protected Collection<Tenant> execute(EntityQueryContext<Tenant> context, QueryByIdRequest query) {
        return context.<Organization,Tenant>asChildQueryContext().getAllByEntityType(Tenant.class);
    }
    
}
