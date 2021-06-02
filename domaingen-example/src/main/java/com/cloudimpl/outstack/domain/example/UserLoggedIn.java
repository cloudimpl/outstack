package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.domain.example.User;
import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import javax.validation.constraints.NotEmpty;

public class UserLoggedIn extends Event<User> {
    private final String remoteIp ;
    private final String browserDetail ;
    @NotBlank(message = "username field cannot be blank in UserLoggedIn event")
    @NotEmpty(message = "username field cannot be empty or null in UserLoggedIn event")
    private final String username ;

    public UserLoggedIn(String remoteIp, String browserDetail, String username) {
        this.remoteIp = remoteIp ;
        this.browserDetail = browserDetail ;
        this.username = username ;
    }

    public String getRemoteIp() {
        return this.remoteIp ;
    }

    public String getBrowserDetail() {
        return this.browserDetail ;
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
