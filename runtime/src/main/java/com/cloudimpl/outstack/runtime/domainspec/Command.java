/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

/**
 *
 * @author nuwansa
 */
public abstract class Command implements Input, ICommand {

    private  String _rootId;
    private String _id;
    private  String _tenantId;

    public Command(Builder builder) {
        this._rootId = builder.rootId;
        this._tenantId = builder.tenantId;
        this._id = builder.id;
    }

    public final String rootId() {
        return _rootId;
    }

    protected void setRootId(String rootId)
    {
        this._rootId = rootId;
    }
    
    protected void setId(String id)
    {
        this._id = id;
    }
    
    public final String id()
    {
        return this._id;
    }
    
    protected void setTenantId(String tenantId)
    {
        this._tenantId = tenantId;
    }
    
    @Override
    public final String commandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public final <T extends Command> T unwrap(Class<T> type)
    {
        return (T) this;
    }
    
    @Override
    public final String tenantId() {
        return _tenantId;
    }

    public static  class Builder {

        protected String rootId;
        protected String id;
        protected String tenantId;

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withId(String id)
        {
            this.id = id;
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
