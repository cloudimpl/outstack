/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import com.cloudimpl.outstack.runtime.EntityMetaDetail;
import com.cloudimpl.outstack.runtime.EntityMetaDetailCache;
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

        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getOwner());
        EntityMetaDetail rootMeta = EntityMetaDetailCache.instance().getEntityMeta(this.getRootOwner());
        _meta.setIdIgnoreCase(meta.isIdIgnoreCase());
        _meta.setRootIdIgnoreCase(rootMeta.isIdIgnoreCase());
    }
    
    @Override
    public Class getOwner() {
        if (entityType != null) {
            return Util.classForName(this.entityType);
        } else {
            return null;
        }
    }

    @Override
    public Class getRootOwner() {
        if (rootType != null) {
            return Util.classForName(this.rootType);
        } else {
            return null;
        }
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
