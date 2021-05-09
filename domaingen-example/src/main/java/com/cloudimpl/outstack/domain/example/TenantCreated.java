package com.cloudimpl.outstack.domain.example;

import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.domain.example.Tenant;
import javax.validation.constraints.NotEmpty;

public class TenantCreated extends Event<Tenant> {
    private final String endpoint ;
    @NotEmpty(message = "tenantName field cannot be empty or null in TenantCreated event")
    @NotBlank(message = "tenantName field cannot be blank in TenantCreated event")
    private final String tenantName ;
    @NotBlank(message = "orgName field cannot be blank in TenantCreated event")
    @NotEmpty(message = "orgName field cannot be empty or null in TenantCreated event")
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
