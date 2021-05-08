/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

/**
 *
 * @author nuwan
 */
public abstract class Query implements IQuery{
    private  String _rootId;
    private  String _tenantId;
    private  String _id;
    public Query(Builder builder)
    {
        this._rootId = builder.rootId;
        this._tenantId = builder.tenantId;
        this._id = builder.id;
    }
    
    public final String tenantId()
    {
        return _tenantId;
    }
    
    
    protected void setRootId(String rootId)
    {
        this._rootId = rootId;
    }
    
    protected void setTenantId(String tenantId)
    {
        this._tenantId = tenantId;
    }
    
    protected void setId(String id)
    {
        this._id = id;
    }
    
     @Override
    public <T extends Query> T unwrap(Class<T> type)
    {
        return (T) this;
    }
    
    @Override
    public String queryName()
    {
        return this.getClass().getSimpleName();
    }
    
    public final String rootId()
    {
        return this._rootId;
    }
    
    public final String id()
    {
        return this._id;
    }
    
    public abstract static  class Builder
    {
        private String rootId;
        private String tenantId;
        private String id;
        
        public Builder withRootId(String rootId)
        {
            this.rootId = rootId;
            return this;
        }
        
        public Builder withTenantId(String tenatId)
        {
            this.tenantId = tenatId;
            return this;
        }
        
        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }
        
        public abstract <T extends Query> T build();
        
    }
}
