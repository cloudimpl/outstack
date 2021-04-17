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
public interface Entity extends IResource {

    String id();

    default boolean hasTenant() {
        return this instanceof ITenant;
    }

    default boolean isRoot() {
        return this instanceof RootEntity;
    }

    public static <T extends Entity> T createEntity(Class<?> type, Event event) {
        if (event.isRootEvent()) {
            if (event.getTenantId() != null) {
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(event.entityId(), event.getTenantId()));
            } else {
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class), new Util.VarArg<>(event.entityId()));
            }
        }else
        {
            if(event.getTenantId() != null){
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class,String.class, String.class), new Util.VarArg<>(event.rootEntityId(),event.entityId()
                                , event.getTenantId()));
            }else{
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(event.rootEntityId(),event.entityId()));
           
            }
        }
    }
    
    <T extends Entity> T apply(Event event);
}
