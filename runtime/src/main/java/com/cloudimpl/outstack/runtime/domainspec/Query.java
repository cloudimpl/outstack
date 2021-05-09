/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Collections;
import java.util.List;

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
    
    public static class PagingRequest
    {
        private final int pageNum;
        private final int pageSize;
        private final List<Order> orders;

        public PagingRequest(int pageNum, int pageSize,List<Order> orders) {
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.orders = Collections.unmodifiableList(orders);
        }
        
        public List<Order> orders()
        {
            return this.orders;
        }
        
        public int pageNum()
        {
            return this.pageNum;
        }
        
        public int pageSize()
        {
            return this.pageSize;
        }
    }
    
    public static class Order
    {
        private final String name;
        private final Direction direction;

        public Order(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        public String getName() {
            return name;
        }

        public Direction getDirection() {
            return direction;
        }
        
        
    }
    
    public static enum Direction
    {
        ASC,
        DESC;
    }
}
