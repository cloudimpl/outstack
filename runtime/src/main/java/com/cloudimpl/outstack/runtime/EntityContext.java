/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Event;

/**
 *
 * @author nuwansa
 * @param <T>
 */
public class EntityContext<T extends Entity>{
    private String tenantId;

    protected void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
    
    
    public <E extends Event> T apply(String id,E event)
    {
        event.setTenantId(tenantId);
        return null;
    }
    
    public T getEntity(String id)
    {
        return null;
    }
    
}
