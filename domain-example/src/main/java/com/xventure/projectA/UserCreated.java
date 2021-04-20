package com.xventure.projectA;

import com.xventure.projectA.enums.OrderType;
import com.cloudimpl.domainspec.v1.RootEntity;
import com.cloudimpl.domainspec.v1.Event;
import com.cloudimpl.domainspec.v1.Entity;
import com.xventure.projectA.user.User;

public class UserCreated extends Event {
    private final String id ;
    private final String name ;
    private final String firstname ;
    private final OrderType orderType ;
    private final String userId ;

    public UserCreated(String id, String name, String firstname, OrderType orderType, String userId) {
        this.id = id ;
        this.name = name ;
        this.firstname = firstname ;
        this.orderType = orderType ;
        this.userId = userId ;
    }

    public String getId() {
        return this.id ;
    }

    public String getName() {
        return this.name ;
    }

    public String getFirstname() {
        return this.firstname ;
    }

    public OrderType getOrderType() {
        return this.orderType ;
    }

    public String getUserId() {
        return this.userId ;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return User.class;
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
        return userId;
    }
}
