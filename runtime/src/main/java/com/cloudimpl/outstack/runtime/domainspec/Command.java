/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Objects;

/**
 *
 * @author nuwansa
 */
public abstract class Command implements Input {

    private final String rootTid;
    private final String tenantId;
    private final String command;
    private final String targetEntity;

    public Command(Builder builder) {
        Objects.requireNonNull(builder.targetEntity);
        Objects.requireNonNull(builder.command);
        this.rootTid = builder.rootTid;
        this.tenantId = builder.tenantId;
        this.command = builder.command;
        this.targetEntity = builder.targetEntity;
    }

    public String rootTid() {
        return rootTid;
    }
    
    public String commandName() {
        return command;
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    public static  Builder builder()
    {
        return new Builder();
    }
    
    public static class Builder {
        protected String rootTid;
        protected String tenantId;
        protected String command;
        protected String targetEntity;
        
        public Builder withRootTid(String rootTid)
        {
            this.rootTid = rootTid;
            return this;
        }
        
        public Builder withTenantId(String tenantId)
        {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder withCommand(String command)
        {
            this.command = command;
            return this;
        }
        
        public Builder withTargetEntity(String targetEntity)
        {
            this.targetEntity = targetEntity;
            return this;
        }
        
    }
}
