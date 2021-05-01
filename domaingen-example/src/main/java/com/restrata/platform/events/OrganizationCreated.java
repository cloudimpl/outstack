package com.restrata.platform.events;

import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.restrata.platform.Organization;

public class OrganizationCreated extends Event<Organization> {
    private final String website ;
    private final String orgName ;

    public OrganizationCreated(String website, String orgName) {
        this.website = website ;
        this.orgName = orgName ;
    }

    public String getWebsite() {
        return this.website ;
    }

    public String getOrgName() {
        return this.orgName ;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return Organization.class;
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
        return orgName;
    }
}
