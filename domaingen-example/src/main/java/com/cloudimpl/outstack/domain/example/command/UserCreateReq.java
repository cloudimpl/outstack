package com.cloudimpl.outstack.domain.example.command;

import com.cloudimpl.outstack.runtime.domainspec.Command;

public class UserCreateReq extends Command {
    private final String password ;
    private final String username ;

    private UserCreateReq(Builder builder) {
        super(builder) ;
        this.password = builder.password ;
        this.username = builder.username ;
    }

    public String getPassword() {
        return this.password ;
    }

    public String getUsername() {
        return this.username ;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {
        private String password ;
        private String username ;

        public Builder withPassword(String password) {
            this.password = password ;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username ;
            return this;
        }

        @Override
        public UserCreateReq build() {
            return new UserCreateReq(this);
        }
    }
}
