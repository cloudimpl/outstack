package com.restrata.platform.commands;

import com.cloudimpl.outstack.runtime.domainspec.Command;

public class OrganizationCreateRequest extends Command {
    private final String website ;
    private final String orgName ;

    private OrganizationCreateRequest(Builder builder) {
        super(builder) ;
        this.website = builder.website ;
        this.orgName = builder.orgName ;
    }

    public String getWebsite() {
        return this.website ;
    }

    public String getOrgName() {
        return this.orgName ;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {
        private String website ;
        private String orgName ;

        public Builder withWebsite(String website) {
            this.website = website ;
            return this;
        }

        public Builder withOrgName(String orgName) {
            this.orgName = orgName ;
            return this;
        }

        @Override
        public OrganizationCreateRequest build() {
            return new OrganizationCreateRequest(this);
        }
    }
}
