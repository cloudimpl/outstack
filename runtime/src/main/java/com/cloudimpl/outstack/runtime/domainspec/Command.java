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
public abstract class Command implements Input,ICommand {

    private  String rootId;
    private  String tenantId;

    public Command(Builder builder) {
        this.rootId = builder.rootId;
        this.tenantId = builder.tenantId;
    }

    public String rootId() {
        return rootId;
    }

    protected void setRootId(String rootId)
    {
        this.rootId = rootId;
    }
    
    protected void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }
    
    @Override
    public String commandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public <T extends Command> T unwrap(Class<T> type)
    {
        return (T) this;
    }
    
    @Override
    public String tenantId() {
        return tenantId;
    }

    public static  class Builder {

        protected String rootId;
        protected String tenantId;

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public  <T extends Command> T build()
        {
            return (T) new Command(this) {
            };
        }
    }
}
