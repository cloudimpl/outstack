package com.xventure.projectA.user;

import com.xventure.projectA.value.Address;
import com.xventure.projectA.enums.OrderType;
import com.cloudimpl.outstack.runtime.domainspec.Command;

public class UserCreateRequest extends Command {
    private final OrderType orderType ;
    private final String firstname ;
    private final Address address ;
    private final String name ;
    private final String id ;
    private final String userId ;

    private UserCreateRequest(Builder builder) {
        super(builder) ;
        this.orderType = builder.orderType ;
        this.firstname = builder.firstname ;
        this.address = builder.address ;
        this.name = builder.name ;
        this.id = builder.id ;
        this.userId = builder.userId ;
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

    public static final class Builder extends Command.Builder {
        private OrderType orderType ;
        private String firstname ;
        private Address address ;
        private String name ;
        private String id ;
        private String userId ;

        public Builder withOrderType(OrderType orderType) {
            this.orderType = orderType ;
            return this;
        }

        public Builder withFirstname(String firstname) {
            this.firstname = firstname ;
            return this;
        }

        public Builder withAddress(Address address) {
            this.address = address ;
            return this;
        }

        public Builder withName(String name) {
            this.name = name ;
            return this;
        }

        public Builder withId(String id) {
            this.id = id ;
            return this;
        }

        public Builder withUserId(String userId) {
            this.userId = userId ;
            return this;
        }

        public UserCreateRequest build() {
            return new UserCreateRequest(this);
        }
    }
}
