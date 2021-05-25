package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.domain.example.OrganizationCreated;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import javax.validation.constraints.NotEmpty;

@EntityMeta(plural="organizations",version="v1")
public class Organization extends RootEntity {
    private String website ;
    @NotBlank(message = "orgName field cannot be blank in Organization entity")
    @NotEmpty(message = "orgName field cannot be empty or null in Organization entity")
    private final String orgName ;

    public Organization(String orgName) {
        this.orgName = orgName ;
    }

    public String getWebsite() {
        return this.website ;
    }

    public String getOrgName() {
        return this.orgName ;
    }

    @Override
    public String entityId() {
        return orgName;
    }

    @Override
    public String idField() {
        return "orgName";
    }

    private void applyEvent(OrganizationCreated evt) {
        this.website = evt.getWebsite() ;
    }

    @Override
    public void apply(Event event) {

        switch (event.getClass().getSimpleName() ) {
            case "OrganizationCreated" : {
                applyEvent((OrganizationCreated) event) ;
                break ;
            }
            default : {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT,"unhandled event:"+event.getClass().getName()) ;
            }
        }
    }
}
