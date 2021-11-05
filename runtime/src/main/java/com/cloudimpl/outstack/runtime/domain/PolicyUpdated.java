package com.cloudimpl.outstack.runtime.domain;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

import java.util.Collections;
import java.util.List;

public class PolicyUpdated extends Event<Policy> {

    private final String policyName;
    private final String policyContext;
    private final String domainOwner;
    private final String domainContext;
    private final String apiContext;
    private final String boundary;
    private final List<String> dependentPolicies;
    public PolicyUpdated(String policyName,String policyContext,String domainOwner,String domainContext,String apiContext,String boundary,List<String> dependentPolicies) {
        this.policyName = policyName;
        this.policyContext = policyContext;
        this.domainOwner = domainOwner;
        this.domainContext = domainContext;
        this.apiContext = apiContext;
        this.boundary = boundary;
        this.dependentPolicies = dependentPolicies == null? Collections.EMPTY_LIST : dependentPolicies;
    }

    @Override
    public Class<? extends Entity> getOwner() {
        return Policy.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return Policy.class;
    }

    public String getPolicyContext() {
        return policyContext;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getApiContext() {
        return apiContext;
    }

    public String getBoundary() {
        return boundary;
    }

    public List<String> getDependentPolicies() {
        return dependentPolicies;
    }

    @Override
    public String entityId() {
        return policyName;
    }

    @Override
    public String rootEntityId() {
        return policyName;
    }

}
