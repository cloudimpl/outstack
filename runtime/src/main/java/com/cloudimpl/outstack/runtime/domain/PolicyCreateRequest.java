/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.runtime.domain;

import com.cloudimpl.outstack.runtime.domainspec.Command;

import java.util.List;

/**
 *
 * @author nuwan
 */
 public class PolicyCreateRequest extends Command {

    private final String domainOwner;
    private final String domainContext;
    private final String policyName;
    private final String policyContext;
    private final String boundary;
    private final List<String> dependentPolicies;

    public PolicyCreateRequest(Builder builder) {
        super(builder);
        this.domainOwner = builder.domainOwner;
        this.domainContext = builder.domainContext;
        this.policyName = builder.policyName;
        this.policyContext = builder.policyContext;
        this.boundary = builder.boundary;
        this.dependentPolicies = builder.dependentPolicies;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getPolicyContext() {
        return policyContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getBoundary() {
        return boundary;
    }

    public List<String> getDependentPolicies() {
        return dependentPolicies;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {

        private String policyName;
        private String policyContext;
        private String domainOwner;
        private String domainContext;
        private String boundary;
        private List<String> dependentPolicies;
        
        public Builder withPolicyContext(String policyContext) {
            this.policyContext = policyContext;
            return this;
        }

        public Builder withPolicyName(String policyName) {
            this.policyName = policyName;
            return this;
        }

        public Builder withDomainOwner(String domainOwner){
            this.domainOwner = domainOwner;
            return this;
        }
        
        public Builder withDomainContext(String domainContext){
            this.domainContext = domainContext;
            return this;
        }
        
        public Builder withBoundary(String boundary){
            this.boundary = boundary;
            return this;
        }

        public Builder WithDependentPolicies(List<String> dependentPolicies) {
            this.dependentPolicies = dependentPolicies;
            return this;
        }

        @Override
        public PolicyCreateRequest build() {
            return new PolicyCreateRequest(this);
        }
    }
}
