package com.cloudimpl.outstack.domain.example.commands;

import com.cloudimpl.outstack.runtime.domainspec.Command;

public class TenantCreateRequest extends Command {
    private final String endpoint ;
    private final String tenantName ;

    private TenantCreateRequest(Builder builder) {
        super(builder) ;
        this.endpoint = builder.endpoint ;
        this.tenantName = builder.tenantName ;
    }

    public String getEndpoint() {
        return this.endpoint ;
    }

    public String getTenantName() {
        return this.tenantName ;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {
        private String endpoint ;
        private String tenantName ;

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint ;
            return this;
        }

        public Builder withTenantName(String tenantName) {
            this.tenantName = tenantName ;
            return this;
        }

        @Override
        public TenantCreateRequest build() {
            return new TenantCreateRequest(this);
        }
    }
}
