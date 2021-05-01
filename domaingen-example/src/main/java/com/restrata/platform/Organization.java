package com.restrata.platform;

import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.restrata.platform.events.OrganizationCreated;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;

@EntityMeta(plural="organizations")
public class Organization extends RootEntity {
    private String website ;
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
                throw new DomainEventException("unhandled event:"+event.getClass().getName()) ;
            }
        }
    }
}
