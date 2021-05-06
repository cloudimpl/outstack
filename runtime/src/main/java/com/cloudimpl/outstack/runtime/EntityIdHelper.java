/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import java.util.Objects;

/**
 *
 * @author nuwan
 */
public class EntityIdHelper {

    public static void validateEntityId(String id)
    {
        Objects.requireNonNull(id);
        if(id.startsWith(EventRepositoy.TID_PREFIX))
        {
            throw new DomainEventException("invalid entity format.{0}", id);
        }
    }
    
    public static boolean isTechnicalId(String id)
    {
        Objects.requireNonNull(id);
        return id.startsWith(EventRepositoy.TID_PREFIX);
    }
    
    public static String toTechnicalId(String id)
    {
        int index = id.indexOf(EventRepositoy.TID_PREFIX);
        if(index == -1)
        {
            throw new DomainEventException("invalid technical id {0} ,must starts with 'id-'", id);
        }
        return id.substring(index + EventRepositoy.TID_PREFIX.length());
    }
    
    public static void validateId(String id,Entity entity)
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(entity);
        if(isTechnicalId(id))
        {
            if(!id.endsWith(entity.id()))
            {
                throw new DomainEventException("invalid technical id {0} in entity. {1}", id,entity.getTRN());
            }
        }else
        {
            if(!id.equals(entity.entityId()))
            {
                throw new DomainEventException("invalid entity id {0} in entity. {1}", id,entity.getBRN());
            }
        }
    }
}
