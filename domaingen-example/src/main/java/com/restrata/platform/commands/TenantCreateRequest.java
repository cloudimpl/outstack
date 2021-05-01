package com.restrata.platform.commands;

import com.cloudimpl.outstack.runtime.domainspec.Command;

public class TenantCreateRequest extends Command {
    private final String endpoint ;

    private TenantCreateRequest(Builder builder) {
        super(builder) ;
        this.endpoint = builder.endpoint ;
    }

    public String getEndpoint() {
        return this.endpoint ;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {
        private String endpoint ;

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint ;
            return this;
        }

        @Override
        public TenantCreateRequest build() {
            return new TenantCreateRequest(this);
        }
    }
}
