package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.domain.example.User;
import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import javax.validation.constraints.NotEmpty;

public class UserUpdated extends Event<User> {
    private final String password ;
    @NotBlank(message = "username field cannot be blank in UserUpdated event")
    @NotEmpty(message = "username field cannot be empty or null in UserUpdated event")
    private final String username ;

    public UserUpdated(String password, String username) {
        this.password = password ;
        this.username = username ;
    }

    public String getPassword() {
        return this.password ;
    }

    public String getUsername() {
        return this.username ;
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
        return username;
    }

    @Override
    public String entityId() {
        return username;
    }
}
