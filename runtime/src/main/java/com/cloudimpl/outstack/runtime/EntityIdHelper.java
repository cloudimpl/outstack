/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import java.util.Objects;

/**
 *
 * @author nuwan
 */
public class EntityIdHelper {

    public static void validateEntityId(String id) {
        Objects.requireNonNull(id);
        if (id.startsWith(EventRepositoy.TID_PREFIX)) {
            throw new DomainEventException(DomainEventException.ErrorCode.EXPECT_ENTITY_ID, "invalid entity id format.{0}", id);
        }
    }

    public static void validateTechnicalId(String id) {
        Objects.requireNonNull(id);
        if (!id.startsWith(EventRepositoy.TID_PREFIX)) {
            throw new DomainEventException(DomainEventException.ErrorCode.EXPECT_TECHNICAL_ID, "invalid entity technical id format.{0}", id);
        }
    }

    public static boolean isTechnicalId(String id) {
        Objects.requireNonNull(id);
        return id.startsWith(EventRepositoy.TID_PREFIX);
    }

    public static String toTechnicalId(String id) {
        int index = id.indexOf(EventRepositoy.TID_PREFIX);
        if (index == -1) {
            throw new DomainEventException(DomainEventException.ErrorCode.EXPECT_TECHNICAL_ID, "invalid technical id {0} ,must starts with 'id-'", id);
        }
        return id.substring(index + EventRepositoy.TID_PREFIX.length());
    }

    public static void validateId(String id, Entity entity) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(entity);
        if (isTechnicalId(id)) {
            if (!id.equals(entity.id())) {
                throw new DomainEventException(DomainEventException.ErrorCode.TECHNICAL_ID_MISMATCHED, "invalid technical id {0} in entity. {1}", id, entity.getTRN());
            }
        } else {
            EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(entity.getClass());
            if (meta.isIdIgnoreCase()) {
                if (!id.equalsIgnoreCase(entity.entityId())) {
                    throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_ID_MISMATCHED, "invalid entity id {0} in entity. {1}", id, entity.getBRN());
                }
            } else {
                if (!id.equals(entity.entityId())) {
                    throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_ID_MISMATCHED, "invalid entity id {0} in entity. {1}", id, entity.getBRN());
                }
            }

        }
    }

    public static void validateId(String id, Event event) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(event);
        if (isTechnicalId(id)) {
            if (!id.equals(event.id())) {
                throw new DomainEventException(DomainEventException.ErrorCode.TECHNICAL_ID_MISMATCHED, "invalid technical id {0} in event. {1}", id, event.getTRN());
            }
        } else {
            EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(event.getOwner());
            if (meta.isIdIgnoreCase()) {
                if (!id.equalsIgnoreCase(event.entityId())) {
                    throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_ID_MISMATCHED, "invalid entity id {0} in event. {1}", id, event.getBRN());
                }
            } else {
                if (!id.equals(event.entityId())) {
                    throw new DomainEventException(DomainEventException.ErrorCode.ENTITY_ID_MISMATCHED, "invalid entity id {0} in event. {1}", id, event.getBRN());
                }
            }

        }
    }

    public static String idToRefId(String tid) {
        validateTechnicalId(tid);
        return "ref:" + tid;
    }

    public static String refIdToId(String refId) {
        if (!refId.startsWith("ref:id-")) {
            throw new DomainEventException(DomainEventException.ErrorCode.NOT_A_REF_ID, "invalid reference id", refId);
        }
        return refId.substring(7);
    }
}
