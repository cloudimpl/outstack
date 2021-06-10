package com.cloudimpl.outstack.domain.example;

import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.ITenantOptional;
import com.cloudimpl.outstack.runtime.domainspec.IgnoreCase;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import javax.validation.constraints.NotBlank;
import com.cloudimpl.outstack.domain.example.UserCreated;
import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.domain.example.UserUpdated;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import javax.validation.constraints.NotEmpty;
import com.cloudimpl.outstack.domain.example.UserLoggedIn;

@EntityMeta(plural="users",version="v1")
public class User extends RootEntity implements ITenantOptional {
    private final String tenantId ;
    @NotEmpty(message = "password field cannot be empty or null in User entity")
    @NotBlank(message = "password field cannot be blank in User entity")
    private String password ;
    @NotEmpty(message = "username field cannot be empty or null in User entity")
    @IgnoreCase
    @NotBlank(message = "username field cannot be blank in User entity")
    @Id
    private final String username ;

    public User(String username, String tenantId) {
        this.username = username ;
        this.tenantId = tenantId ;
    }

    @Override
    public String getTenantId() {
        return this.tenantId ;
    }

    public String getPassword() {
        return this.password ;
    }

    public String getUsername() {
        return this.username ;
    }

    @Override
    public String entityId() {
        return username;
    }

    @Override
    public String idField() {
        return "username";
    }

    private void applyEvent(UserCreated evt) {
        this.password = evt.getPassword() ;
    }

    private void applyEvent(UserUpdated evt) {
        this.password = evt.getPassword() ;
    }

    private void applyEvent(UserLoggedIn evt) {
    }

    @Override
    public void apply(Event event) {

        switch (event.getClass().getSimpleName() ) {
            case "UserCreated" : {
                applyEvent((UserCreated) event) ;
                break ;
            }
            case "UserUpdated" : {
                applyEvent((UserUpdated) event) ;
                break ;
            }
            case "UserLoggedIn" : {
                applyEvent((UserLoggedIn) event) ;
                break ;
            }
            default : {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT,"unhandled event:"+event.getClass().getName()) ;
            }
        }
    }
}
