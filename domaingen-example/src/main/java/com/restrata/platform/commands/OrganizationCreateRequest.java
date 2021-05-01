package com.restrata.platform.commands;

import com.cloudimpl.outstack.runtime.domainspec.Command;

public class OrganizationCreateRequest extends Command {
    private final String website ;

    private OrganizationCreateRequest(Builder builder) {
        super(builder) ;
        this.website = builder.website ;
    }

    public String getWebsite() {
        return this.website ;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {
        private String website ;

        public Builder withWebsite(String website) {
            this.website = website ;
            return this;
        }

        @Override
        public OrganizationCreateRequest build() {
            return new OrganizationCreateRequest(this);
        }
    }
}
