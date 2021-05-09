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
import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

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
            EntityHelper.setCreatedDate(e, event.getMeta().createdDate());
            EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
        } else {
            RootEntity root = (RootEntity) mapEntites.get(resourceHelper.getFQTrn(event.getRootEntityTRN()));
            e = root.createChildEntity(event.getOwner(), event.entityId(), event.id());
            e.applyEvent(event);
            EntityHelper.setCreatedDate(e, event.getMeta().createdDate());
            EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
        }
        mapEntites.put(resourceHelper.getFQTrn(e), e);
        mapEntites.put(resourceHelper.getFQBrn(e), e);
        return e;
    }

    private String resourcePrefix(String prefix) {
        return MessageFormat.format("{0}:{1}", prefix, resourceHelper);
    }

    private Entity updateEntity(Event event) {

        Entity e = mapEntites.get(resourceHelper.getFQTrn(event.getEntityTRN()));
        e.applyEvent(event);
        EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
        return e;
    }

    private Entity deleteEntity(Event event) {
        return mapEntites.remove(resourceHelper.getFQBrn(event.getEntityRN()));
    }

    private Entity renamEntity(EntityRenamed event) {
        String rn;
        if (event.isRootEvent()) {
            rn = RootEntity.makeTRN(event.getOwner(), event.id(), event.tenantId());
        } else {
            rn = ChildEntity.makeTRN(event.getRootOwner(), event.rootId(), event.getOwner(), event.id(), event.tenantId());
        }
        Entity e = mapEntites.get(resourceHelper.getFQTrn(rn));
        mapEntites.remove(resourceHelper.getFQBrn(e));
        e = e.rename(event.entityId());
        EntityHelper.setUpdatedDate(e, event.getMeta().createdDate());
        mapEntites.put(resourceHelper.getFQBrn(e), e);
        mapEntites.put(resourceHelper.getFQTrn(e), e);
        return e;
    }

    @Override
    public <K extends ChildEntity<T>> Collection<K> getAllChildByType(Class<T> rootType, String id, Class<K> childType, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        String prefix = resourcePrefix("brn") + ":" + RootEntity.makeTRN(rootType, id, tenantId);
        SortedMap<String, Entity> map = mapEntites.subMap(prefix, prefix + Character.MAX_VALUE);
        return map.values().stream().map(e -> (K) e).collect(Collectors.toList());
    }

    @Override
    public Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {
        if (id.startsWith(TID_PREFIX)) {
            return Optional.ofNullable((T) mapEntites.get(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, id, tenantId))));
        } else {
            return Optional.ofNullable((T) mapEntites.get(resourceHelper.getFQBrn(RootEntity.makeRN(rootType, id, tenantId))));
        }
    }

    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        if (childId.startsWith(TID_PREFIX)) {
            return Optional.ofNullable((C) mapEntites.get(resourceHelper.getFQTrn(ChildEntity.makeTRN(rootType, id, childType, childId, tenantId))));
        } else {
            return Optional.ofNullable((C) mapEntites.get(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, id, childType, childId, tenantId))));
        }
    }

    @Override
    public Collection<T> getAllByRootType(Class<T> rootType, String tenantId, Query.PagingRequest paging) {
        Collection<T> filterCollection = mapEntites.entrySet().stream()
                .filter(e -> e.getKey().startsWith(resourcePrefix("trn") + ":tenant/" + tenantId + "/" + rootType.getSimpleName()))
                .map(e -> (T) e.getValue()).collect(Collectors.toList());
        Comparator<T> comparator = null;
        for (Query.Order order : paging.orders()) {
            Comparator<T> comp = (left, right) -> compare(order.getName(), left, right);
            if (order.getDirection() == Query.Direction.DESC) {
                comp = comp.reversed();
            }
            comparator = (comparator == null) ? comp : comparator.thenComparing(comp);
        }
        int offset = paging.pageNum() * paging.pageSize();
        int min = Math.min(filterCollection.size() - offset, paging.pageSize());
        filterCollection.stream().sorted(comparator).skip(offset).limit(min).collect(Collectors.toList());
        return null;
    }

    private int compare(String name, T left, T right) {
        JsonObject leftJson = GsonCodec.encodeToJson(left).getAsJsonObject();
        JsonObject rightJson = GsonCodec.encodeToJson(right).getAsJsonObject();
        JsonElement leftEl = leftJson.get(name);
        JsonElement rightEl = rightJson.get(name);
        if (leftEl == null || rightEl == null) {
            throw new RepositoryException("null not supported for sorting: " + name + " field");
        }
        if (!leftEl.isJsonPrimitive() || !rightEl.isJsonPrimitive()) {
            throw new RepositoryException("only primitive types supported for sorting: ");
        }
        JsonPrimitive leftPrim = leftEl.getAsJsonPrimitive();
        JsonPrimitive rightPrim = rightEl.getAsJsonPrimitive();
        if (leftPrim.isNumber() && rightPrim.isNumber()) {
            return leftPrim.getAsBigDecimal().compareTo(rightPrim.getAsBigDecimal());
        } else if (leftPrim.isString() && rightPrim.isString()) {
            return leftPrim.getAsString().compareTo(rightPrim.getAsString());
        }
        throw new RepositoryException("unsupported data type for sorting . {0} : {1} ", leftJson, rightJson);
    }
}
