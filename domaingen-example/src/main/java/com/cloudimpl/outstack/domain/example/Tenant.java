package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.TenantCreated;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import javax.validation.constraints.NotEmpty;

@EntityMeta(plural="tenants",version="v1")
public class Tenant extends ChildEntity<Organization> {
    private String endpoint ;
    @NotEmpty(message = "tenantName field cannot be empty or null in Tenant entity")
    @NotBlank(message = "tenantName field cannot be blank in Tenant entity")
    @Id
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
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT,"unhandled event:"+event.getClass().getName()) ;
            }
        }
    }
}
