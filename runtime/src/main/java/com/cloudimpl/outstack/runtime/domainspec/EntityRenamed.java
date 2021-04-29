/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwan
 */
public class EntityRenamed extends Event{

    private final String entityType;
    private final String rootType;
    private final String entityId;
    private final String rootId;
    private final String oldEntityId;

    public EntityRenamed(Class<? extends Entity> entityType, Class<? extends RootEntity> rootType, String entityId,String oldEntityId, String rootId) {
        this.entityType = entityType.getName();
        this.rootType = rootType.getName();
        this.entityId = entityId;
        this.rootId = rootId;
        this.oldEntityId = oldEntityId;
    }
    
    @Override
    public Class getOwner() {
        return Util.classForName(this.entityType);
    }

    @Override
    public Class getRootOwner() {
        return Util.classForName(this.rootType);
    }

    
    @Override
    public String entityId() {
        return this.entityId;
    }

    @Override
    public String rootEntityId() {
        return this.rootId;
    }

    public String getOldEntityId() {
        return oldEntityId;
    }
    
}
