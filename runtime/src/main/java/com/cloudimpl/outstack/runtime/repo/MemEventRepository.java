/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class MemEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final TreeMap<String, Entity> mapEntites = new TreeMap<>();

    public MemEventRepository(Class<T> rootType, ResourceHelper resourceHelper, EventStream eventStream) {
        super(rootType, resourceHelper, eventStream);
    }

    @Override
    public synchronized void saveTx(EntityContextProvider.Transaction transaction) {
        List<Event> events = transaction.getEventList();

        for (Event event : events) {
            System.out.println("tx: " + event);
            Entity e = null;
            switch (event.getAction()) {
                case CREATE: {
                    e = createEntity(event);
                    break;
                }
                case UPDATE: {
                    e = updateEntity(event);
                    break;
                }
                case DELETE: {
                    e = deleteEntity(event);
                    break;
                }
                case RENAME: {
                    EntityRenamed renamedEvent = (EntityRenamed) event;
                    renamEntity(renamedEvent);
                    break;
                }
            }
            System.out.println("entity: " + e);
        }
    }

    private Entity createEntity(Event event) {
        Entity e;
        if (event.isRootEvent()) {
            e = RootEntity.create(event.getOwner(), event.entityId(), event.tenantId(), event.id());
            e.applyEvent(event);
        } else {
            RootEntity root = (RootEntity) mapEntites.get(resourceHelper.getFQTrn(event.getRootEntityTRN()));
            e = root.createChildEntity(event.getOwner(), event.entityId(), event.id());
            e.applyEvent(event);
        }
        mapEntites.put(resourceHelper.getFQTrn(e), e);
        mapEntites.put(resourceHelper.getFQBrn(e), e);
        return e;
    }

    private String resourcePrefix(String prefix) {
       return MessageFormat.format("{0}:{1}",prefix, resourceHelper);
    }

    private Entity updateEntity(Event event) {

        Entity e = mapEntites.get(resourceHelper.getFQTrn(event.getEntityTRN()));
        e.applyEvent(event);
        return e;
    }

    private Entity deleteEntity(Event event) {
        return mapEntites.remove(resourceHelper.getFQBrn(event.getEntityRN()));
    }

    private Entity renamEntity(EntityRenamed event) {
        String rn;
        if(event.isRootEvent())
        {
            rn = RootEntity.makeTRN(event.getOwner(),event.id(),event.tenantId());
        }
        else
        {
            rn = ChildEntity.makeTRN(event.getRootOwner(),event.rootId(),event.getOwner(),event.id(),event.tenantId());
        }
        Entity e = mapEntites.get(resourceHelper.getFQTrn(rn));
        mapEntites.remove(resourceHelper.getFQBrn(e));
        e = e.rename(event.entityId());

        mapEntites.put(resourceHelper.getFQBrn(e), e);
        mapEntites.put(resourceHelper.getFQTrn(e), e);
        return e;
    }

    @Override
    public <K extends ChildEntity<T>> Collection<K> getAllChildByType(Class<T> rootType,String id,Class<K> childType,String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        String prefix = resourcePrefix("brn") + ":" + RootEntity.makeTRN(rootType, id, tenantId);
        SortedMap<String, Entity> map = mapEntites.subMap(prefix, prefix + Character.MAX_VALUE);
        return map.values().stream().map(e -> (K) e).collect(Collectors.toList());
    }

    @Override
    public  Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {
        if(id.startsWith(TID_PREFIX))
        {
            return Optional.ofNullable((T) mapEntites.get(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, id, tenantId))));
        }
        else
        {
            return Optional.ofNullable((T) mapEntites.get(resourceHelper.getFQBrn(RootEntity.makeRN(rootType, id, tenantId))));
        }
    }

    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        if(childId.startsWith(TID_PREFIX))
        {
            return Optional.ofNullable((C) mapEntites.get(resourceHelper.getFQTrn(ChildEntity.makeTRN(rootType, id,childType,childId, tenantId))));
        }
        else
        {
            return Optional.ofNullable((C) mapEntites.get(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, id,childType,childId, tenantId))));
        }
    }

}
