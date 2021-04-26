/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domain.v1;

import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwan
 */
public class EntityDeleted extends Event{

    private final String entityType;
    private final String rootType;
    private final String entityId;
    private final String rootEntityId;

    public EntityDeleted(Class<? extends Entity> entityType, Class<? extends RootEntity> rootType,String entityId, String rootEntityId) {
        this.entityType = entityType.getName();
        this.rootType = rootType.getName();
        this.entityId = entityId;
        this.rootEntityId = rootEntityId;
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        return Util.classForName(entityType);
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        return  Util.classForName(rootType);
    }

    @Override
    public String entityId() {
       return entityId;
    }

    @Override
    public String rootEntityId() {
        return rootEntityId;
    }
    
}
