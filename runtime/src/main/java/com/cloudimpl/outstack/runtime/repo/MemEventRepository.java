/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.EntityCheckpoint;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ResultSet;
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
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class MemEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final TreeMap<String, Entity> mapEntites = new TreeMap<>();
    private final List<Event> events = new CopyOnWriteArrayList<>();
    private final Map<String, EntityCheckpoint> checkpoints = new HashMap<>();

    public MemEventRepository(Class<T> rootType, ResourceHelper resourceHelper, EventStream eventStream) {
        super(rootType, resourceHelper, eventStream);
    }

    @Override
    public synchronized void saveTx(EntityContextProvider.Transaction transaction) {
        List<Event> events = transaction.getEventList();

        for (Event event : events) {
            System.out.println("tx: " + event);
            Entity e = applyEvent(event);
            System.out.println("entity: " + e + " event : " + event);
        }
    }

    @Override
    public synchronized <T extends Entity> T applyEvent(Event event) {
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
                e = renamEntity(renamedEvent);
                break;
            }
        }
        System.out.println("entity: " + e);
        System.out.println("event: " + event);
        long nextSeq = getCheckpoint(event.getRootEntityTRN()).getSeq() + 1;
        event.setSeqNum(nextSeq);
        getCheckpoint(event.getRootEntityTRN()).setSeq(nextSeq);
        events.add(event);
        return (T) e;
    }

    private EntityCheckpoint getCheckpoint(String rootTrn) {
        return checkpoints.computeIfAbsent(rootTrn, trn -> new EntityCheckpoint(trn));
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
            rn = RootEntity.makeTRN(event.getOwner(), event.getMeta().getVersion(), event.id(), event.tenantId());
        } else {
            rn = ChildEntity.makeTRN(event.getRootOwner(), event.getMeta().getVersion(), event.rootId(), event.getOwner(), event.id(), event.tenantId());
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
    public synchronized <K extends ChildEntity<T>> ResultSet<K> getAllChildByType(Class<T> rootType, String id, Class<K> childType, String tenantId, Query.PagingRequest paging) {
        EntityIdHelper.validateTechnicalId(id);
        String prefix = resourcePrefix("brn") + ":" + RootEntity.makeTRN(rootType, version, id, tenantId);
        SortedMap<String, Entity> map = mapEntites.subMap(prefix, prefix + Character.MAX_VALUE);
        Collection<K> result = map.values().stream().filter(e -> e.getClass() == childType)
                .map(e -> (K) e)
                .filter(e -> onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        ResultSet<K> col = onPageable(result, paging);
        return col;
    }

    @Override
    public synchronized Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {
        if (id.startsWith(TID_PREFIX)) {
            return Optional.ofNullable((T) mapEntites.get(resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, id, tenantId))));
        } else {
            return Optional.ofNullable((T) mapEntites.get(resourceHelper.getFQBrn(RootEntity.makeRN(rootType, version, id, tenantId))));
        }
    }

    @Override
    public synchronized <C extends ChildEntity<T>> Optional<C> getChildById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        if (childId.startsWith(TID_PREFIX)) {
            return Optional.ofNullable((C) mapEntites.get(resourceHelper.getFQTrn(ChildEntity.makeTRN(rootType, version, id, childType, childId, tenantId))));
        } else {
            return Optional.ofNullable((C) mapEntites.get(resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId))));
        }
    }

    @Override
    public synchronized ResultSet<T> getAllByRootType(Class<T> rootType, String tenantId, Query.PagingRequest paging) {
        String trn = null;
        if (Entity.hasTenant(rootType)) {
            trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/";
        } else {
            trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/";
        }
        String fqtrn = trn;
        Collection<T> filterCollection = mapEntites.entrySet().stream()
                .filter(e -> e.getKey().startsWith(fqtrn))
                .filter(e -> e.getValue().getClass() == rootType)
                .map(e -> (T) e.getValue())
                .filter(e -> onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        return onPageable(filterCollection, paging);
    }

    private int compare(String name, Object left, Object right) {
        JsonObject leftJson = GsonCodec.encodeToJson(left).getAsJsonObject();
        JsonObject rightJson = GsonCodec.encodeToJson(right).getAsJsonObject();
        JsonElement leftEl = leftJson.get(name);
        JsonElement rightEl = rightJson.get(name);
        if (leftEl == null || rightEl == null) {
            throw new RepositoryException("null not supported for sorting: " + name + " field");
        }
        if (!leftEl.isJsonPrimitive() || !rightEl.isJsonPrimitive()) {
            throw new RepositoryException("only primitive types supported for sorting");
        }
        JsonPrimitive leftPrim = leftEl.getAsJsonPrimitive();
        JsonPrimitive rightPrim = rightEl.getAsJsonPrimitive();
        if (leftPrim.isNumber() && rightPrim.isNumber()) {
            return leftPrim.getAsBigDecimal().compareTo(rightPrim.getAsBigDecimal());
        } else if (leftPrim.isString() && rightPrim.isString()) {
            return leftPrim.getAsString().compareTo(rightPrim.getAsString());
        } else if (leftPrim.isBoolean() && rightPrim.isBoolean()) {
            return Boolean.compare(leftPrim.getAsBoolean(), rightPrim.getAsBoolean());
        }
        throw new RepositoryException("unsupported data type for sorting . {0} : {1} ", leftJson, rightJson);
    }

    private <T> boolean onFilter(T item, Map<String, String> params) {
        if (params.isEmpty()) {
            return true;
        }
        JsonObject json = GsonCodec.encodeToJson(item).getAsJsonObject();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            JsonElement el = json.get(entry.getKey());
            if (el == null || !el.isJsonPrimitive()) {
                System.out.println("el " + entry.getKey() + " not found or not an primitive data type");
                return false;
            } else {
                JsonPrimitive jsonPrim = (JsonPrimitive) el;
                if (jsonPrim.isNumber()) {
                    BigDecimal target = new BigDecimal(entry.getValue());
                    if (jsonPrim.getAsBigDecimal().compareTo(target) != 0) {
                        return false;
                    }
                } else if (jsonPrim.isString()) {
                    if (entry.getValue().startsWith("%") && entry.getValue().length() > 1) {
                        if (!jsonPrim.getAsString().toLowerCase().endsWith(entry.getValue().substring(1).toLowerCase())) {
                            return false;
                        }
                    } else if (entry.getValue().endsWith("%") && entry.getValue().length() > 1) {
                        if (!jsonPrim.getAsString().toLowerCase().startsWith(entry.getValue().substring(0, entry.getValue().length() - 1).toLowerCase())) {
                            return false;
                        }
                    } else if (!jsonPrim.getAsString().equals(entry.getValue())) {
                        return false;
                    }
                } else if (jsonPrim.isBoolean()) {
                    if (jsonPrim.getAsBoolean() != Boolean.valueOf(entry.getValue())) {
                        return false;
                    }
                } else {
                    System.out.println("unhandle primitive data type:" + jsonPrim);
                    return false;
                }
            }
        }
        return true;
    }

    private <T> ResultSet<T> onPageable(Collection<T> result, Query.PagingRequest paging) {
        if (paging == null) {
            return new ResultSet<>(result.size(), 1, 0, result);
        }

        Comparator<T> comparator = null;
        for (Query.Order order : paging.orders()) {
            Comparator<T> comp = (left, right) -> compare(order.getName(), left, right);
            if (order.getDirection() == Query.Direction.DESC) {
                comp = comp.reversed();
            }
            comparator = (comparator == null) ? comp : comparator.thenComparing(comp);
        }
        int offset = paging.pageNum() * paging.pageSize();
        int min = Math.min(result.size() - offset, paging.pageSize());
        Collection<T> out;
        if (comparator != null) {
            out = result.stream().sorted(comparator).skip(offset).limit(min).collect(Collectors.toList());
        } else {
            out = result.stream().skip(offset).limit(min < 0 ? 0 : min).collect(Collectors.toList());
        }
        return new ResultSet<>(result.size(), (int) Math.ceil(((double) result.size()) / paging.pageSize()), paging.pageNum(), out);
    }

    @Override
    public ResultSet<Event<T>> getEventsByRootId(Class<T> rootType, String rootId, String tenantId, Query.PagingRequest paging) {

        String rn;
        boolean technicalId = false;
        if (rootId.startsWith(TID_PREFIX)) {
            rn = RootEntity.makeTRN(rootType, version, rootId, tenantId);
            technicalId = true;
        } else {
            rn = RootEntity.makeRN(rootType, version, rootId, tenantId);
        }
        String rootTrn = RootEntity.makeTRN(rootType, version, rootId, tenantId);
        int size = events.size();

        Stream<Event<T>> stream = IntStream.range(0, size).mapToObj(i -> events.get(size - i - 1));
        if (technicalId) {
            stream = stream.filter(e -> e.getRootEntityTRN().equals(rn));
        } else {
            stream = stream.filter(e -> e.getRootEntityRN().equals(rn));
        }
        Collection<Event<T>> cols = stream.map(e -> (Event<T>) e)
                .filter(e -> onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        return onPageable(cols, paging);
    }

    @Override
    public <K extends ChildEntity<T>> ResultSet<Event<K>> getEventsByChildId(Class<T> rootType, String id, Class<K> childType, String childId, String tenantId, Query.PagingRequest paging) {
        EntityIdHelper.validateTechnicalId(id);
        String rn;
        boolean technicald = false;
        if (childId.startsWith(TID_PREFIX)) {
            rn = ChildEntity.makeTRN(rootType, version, id, childType, childId, tenantId);
            technicald = true;
        } else {
            rn = ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId);
        }

        int size = events.size();

        Stream<Event<K>> stream = IntStream.range(0, size).mapToObj(i -> events.get(size - i - 1));
        if (technicald) {
            stream = stream.filter(e -> e.getEntityTRN().equals(rn));
        } else {
            stream = stream.filter(e -> e.getEntityRN().equals(rn));
        }
        Collection<Event<K>> cols = stream.map(e -> (Event<K>) e)
                .filter(e -> onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        return onPageable(cols, paging);
    }

}
