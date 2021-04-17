/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Tenant;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 *
 * @author nuwan
 */
public class MemEntityRepository implements EventRepositoy{
    private final Multimap<String,Entity> map = HashMultimap.create();
    private final Entity rootEntity;
    private final String keyPrefix;
    public MemEntityRepository(Entity rootEntity) {
        this.rootEntity = rootEntity;
        this.keyPrefix = this.rootEntity.hasTenant()?"Tenant#"+Tenant.class.cast(rootEntity).getTenantId()+"#"+rootEntity.getClass().getName()+"#"+rootEntity.id():
                rootEntity.getClass().getName()+"#"+rootEntity.id();
    }
    
    
    @Override
    public <T extends Entity> T insert(T entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends Entity> T update(T entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends Entity> T deleteById(String entityType,String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends Entity> T deleteByEntityId(String entityType,String entityId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String createKey(String entityType,String entityId)
    {
        return this.keyPrefix +"#"+entityType+"#"+entityId;
    }
}
