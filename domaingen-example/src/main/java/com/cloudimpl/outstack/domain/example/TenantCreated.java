package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.domain.example.Tenant;

public class TenantCreated extends Event<Tenant> {
    private final String endpoint ;
    private final String tenantName ;
    private final String orgName ;

    public TenantCreated(String endpoint, String tenantName, String orgName) {
        this.endpoint = endpoint ;
        this.tenantName = tenantName ;
        this.orgName = orgName ;
    }

    public String getEndpoint() {
        return this.endpoint ;
    }

    public String getTenantName() {
        return this.tenantName ;
    }

    public String getOrgName() {
        return this.orgName ;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return Tenant.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return Organization.class;
    }

    @Override
    public String rootEntityId() {
        return orgName;
    }

    @Override
    public String entityId() {
        return tenantName;
    }
}
