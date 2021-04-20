/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domain.v1;

import com.cloudimpl.outstack.runtime.common.GsonCodec;

/**
 *
 * @author nuwan
 */
public abstract class Entity implements IResource {
    private String _tid;
    
    void setTid(String tid)
    {
        this._tid = tid;
    }

    public String tid() {
        return _tid;
    }
    
    public abstract String id();

    public boolean hasTenant() {
        return this instanceof ITenant;
    }

    public boolean isRoot() {
        return this instanceof RootEntity;
    }

    protected abstract void apply(Event event);
    
    public void applyEvent(Event event)
    {
        if(event.getOwner() != this.getClass())
        {
            throw new DomainEventException("invalid domain event: "+event.getClass().getName());
        }
        apply(event);
    }
    
    public Entity cloneEntity()
    {
        String json = GsonCodec.encode(this);
        return GsonCodec.decode(this.getClass(), json);
    }
}
