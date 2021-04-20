package com.xventure.projectA.org;

import com.xventure.projectA.value.Address;
import com.xventure.projectA.enums.OrderType;
import com.xventure.projectA.UserCreated;
import com.cloudimpl.domainspec.v1.ITenant;
import com.cloudimpl.domainspec.v1.ChildEntity;
import com.xventure.projectA.user.User;
import com.cloudimpl.domainspec.v1.Event;
import com.cloudimpl.domainspec.v1.DomainEventException;

public class Organization extends ChildEntity<User> implements ITenant {
    private final String tenantId ;
    private OrderType orderType ;
    private String firstname ;
    private Address address ;
    private String name ;
    private String id ;
    private final String userId ;
    private final String orgId ;

    public Organization(String userId, String orgId, String tenantId) {
        this.userId = userId ;
        this.orgId = orgId ;
        this.tenantId = tenantId ;
    }

    @Override
    public String getTenantId() {
        return this.tenantId ;
    }

    public OrderType getOrderType() {
        return this.orderType ;
    }

    public String getFirstname() {
        return this.firstname ;
    }

    public Address getAddress() {
        return this.address ;
    }

    public String getName() {
        return this.name ;
    }

    public String getId() {
        return this.id ;
    }

    public String getUserId() {
        return this.userId ;
    }

    public String getOrgId() {
        return this.orgId ;
    }

    @Override
    public String id() {
        return orgId;
    }

    @Override
    public String rootId() {
        return userId;
    }

    @Override
    public Class<User> rootType() {
        return User.class;
    }

    private void applyEvent(UserCreated evt) {
        this.id = evt.getId() ;
        this.name = evt.getName() ;
        this.firstname = evt.getFirstname() ;
    }

    @Override
    public void apply(Event event) {

        switch (event.getClass().getSimpleName() ) {
            case "UserCreated" : {
                applyEvent((UserCreated) event) ;
                break ;
            }
            default : {
                throw new DomainEventException("unhandled event:"+event.getClass().getName()) ;
            }
        }
    }
}
