package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.TenantCreated;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;

@EntityMeta(plural="tenants",version="V1")
public class Tenant extends ChildEntity<Organization> {
    private String endpoint ;
    private final String tenantName ;

    public Tenant(String tenantName) {
        this.tenantName = tenantName ;
    }

    public String getEndpoint() {
        return this.endpoint ;
    }

    public String getTenantName() {
        return this.tenantName ;
    }

    @Override
    public String entityId() {
        return tenantName;
    }

    @Override
    public Class<Organization> rootType() {
        return Organization.class;
    }

    @Override
    public String idField() {
        return "tenantName";
    }

    private void applyEvent(TenantCreated evt) {
        this.endpoint = evt.getEndpoint() ;
    }

    @Override
    public void apply(Event event) {

        switch (event.getClass().getSimpleName() ) {
            case "TenantCreated" : {
                applyEvent((TenantCreated) event) ;
                break ;
            }
            default : {
                throw new DomainEventException("unhandled event:"+event.getClass().getName()) ;
            }
        }
    }
}
