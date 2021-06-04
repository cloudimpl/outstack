/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException.ErrorCode;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwan
 */
public class EntityHelper {

    public static void updateId(Entity entity, String id) {
        entity.setTid(id);
    }

    public static void updateRootId(ChildEntity entity, String rootId) {
        entity.setRootId(rootId);
    }
    
    public static <T extends Entity> boolean hasTenant(Class<T> entityType) {
        return ITenant.class.isAssignableFrom(entityType);
    }

    public static <T extends Entity> boolean hasOptionalTenant(Class<T> entityType) {
        return ITenantOptional.class.isAssignableFrom(entityType);
    }
    public static void setCreatedDate(Entity e,long millis)
    {
        e.getMeta().setCreatedDate(millis);
    }
    
    public static void setUpdatedDate(Entity e ,long millis)
    {
        e.getMeta().setUpdatedDate(millis);
    }
    
    public static void setCreatedDate(Event e,long millis)
    {
        e.getMeta().setCreatedDate(millis);
    }
    
    public  static  void validateEvent(Class<? extends RootEntity> rootType, Event event){
        if(event.getRootOwner()!= rootType){
            throw new DomainEventException(ErrorCode.BASIC_VIOLATION,"root entity type {0} mismatched with event root owner {1}",rootType,event.getRootOwner());
        }
    }
    
    public static void setVersion(Event e,String version)
    {
        e.getMeta().setVersion(version);
    }
    
    public static <T extends Entity> boolean isRootEntity(Class<T> entityType) {
        return RootEntity.isMyType(entityType);
    }

    public static <T extends Entity> T createEntity(Class<T> type, Event event) {
        if (event.isRootEvent()) {
            if (event.tenantId() != null) {
                return Util.createObject(type,
                        new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(event.entityId(), event.tenantId()));
            } else {
                return Util.createObject(type,
                        new Util.VarArg<>(String.class), new Util.VarArg<>(event.entityId()));
            }
        } else {
            if (event.tenantId() != null) {
                return Util.createObject(type,
                        new Util.VarArg<>(String.class, String.class, String.class), new Util.VarArg<>(event.rootEntityId(), event.entityId(),
                                event.tenantId()));
            } else {
                return Util.createObject(type,
                        new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(event.rootEntityId(), event.entityId()));

            }
        }
    }

    public static <T extends RootEntity> T createRootEntity(Class<T> type, String entityId,String tenantId) {
        if (!EntityHelper.isRootEntity(type)) {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_ENTITY_TYPE,"type {0} not a root entity", type.getClass().getName());
        }

        if (tenantId != null) {
            return Util.createObject(type,
                    new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(entityId, tenantId));
        } else {
            return Util.createObject(type,
                    new Util.VarArg<>(String.class), new Util.VarArg<>(entityId));
        }

    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> T createChildEntity(Class<? extends RootEntity> rootType,String rootId,Class<T> childType,String entityId, String tenantId) {
        if (tenantId != null) {
            return Util.createObject(childType,
                    new Util.VarArg<>(String.class,String.class, String.class), new Util.VarArg<>(rootId,entityId, tenantId));
        } else {
            return Util.createObject(childType,
                    new Util.VarArg<>(String.class,String.class), new Util.VarArg<>(rootId,entityId));
        }

    }
}
