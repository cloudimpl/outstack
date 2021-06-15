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

        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getOwner());
        EntityMetaDetail rootMeta = EntityMetaDetailCache.instance().getEntityMeta(this.getRootOwner());
        _meta.setIdIgnoreCase(meta.isIdIgnoreCase());
        _meta.setRootIdIgnoreCase(rootMeta.isIdIgnoreCase());
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        if (entityType != null) {
            return Util.classForName(entityType);
        } else {
            return null;
        }
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
        if (rootType != null) {
            return Util.classForName(rootType);
        } else {
            return null;
        }
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
