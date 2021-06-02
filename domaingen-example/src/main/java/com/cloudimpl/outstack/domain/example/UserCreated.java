package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.domain.example.User;
import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import javax.validation.constraints.NotEmpty;

public class UserCreated extends Event<User> {
    @NotBlank(message = "username field cannot be blank in UserCreated event")
    @NotEmpty(message = "username field cannot be empty or null in UserCreated event")
    private final String username ;
    private final String password ;

    public UserCreated(String username, String password) {
        this.username = username ;
        this.password = password ;
    }

    public String getUsername() {
        return this.username ;
    }

    public String getPassword() {
        return this.password ;
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
