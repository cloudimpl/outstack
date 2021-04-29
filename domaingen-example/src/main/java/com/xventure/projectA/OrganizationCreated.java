package com.xventure.projectA;

import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.xventure.projectA.org.Organization;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.xventure.projectA.user.User;

public class OrganizationCreated extends Event<Organization> {
    private final String name ;
    private final String orgId ;
    private final String userId ;

    public OrganizationCreated(String name, String orgId, String userId) {
        this.name = name ;
        this.orgId = orgId ;
        this.userId = userId ;
    }

    public String getName() {
        return this.name ;
    }

    public String getOrgId() {
        return this.orgId ;
    }

    public String getUserId() {
        return this.userId ;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return Organization.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return User.class;
    }

    @Override
    public String rootEntityId() {
        return userId;
    }

    @Override
    public String entityId() {
        return orgId;
    }
}
