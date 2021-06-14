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
import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.TenantRequirement;
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
    protected void startTransaction() {

    }

    @Override
    protected void endTransaction() {

    }

    protected void saveEntity(Entity e) {
        mapEntites.put(resourceHelper.getFQTrn(e), e);
        mapEntites.put(resourceHelper.getFQBrn(e), e);
    }

    protected <C extends ChildEntity<T>> Collection<C> getAllChildByType(String rootBrn, Class<T> rootType, String id, Class<C> childType) {
        return mapEntites.entrySet().stream()
                .filter(e -> e.getValue().getClass() == childType)
                .filter(e -> e.getKey().startsWith(rootBrn))
                .map(e -> e.getValue())
                .map(c -> (C) c)
                .collect(Collectors.toList());
    }

    @Override
    protected EntityCheckpoint getCheckpoint(String rootTrn) {
        return checkpoints.computeIfAbsent(rootTrn, trn -> new EntityCheckpoint(trn));
    }

    @Override
    public synchronized <K extends ChildEntity<T>> ResultSet<K> getAllChildByType(Class<T> rootType, String id, Class<K> childType, String tenantId, Query.PagingRequest paging) {
        EntityIdHelper.validateTechnicalId(id);
        String trn = null;
        TenantRequirement tenantReq = Entity.checkTenantRequirement(rootType);
        switch (tenantReq) {
            case REQUIRED:
                trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/" + id;
                break;
            case OPTIONAL:
                if (tenantId != null) {
                    trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/" + id;
                } else {
                    trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/" + id;
                }
                break;
            default:
                trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/" + id;
                break;
        }
        String fqtrn = trn;

        Collection<K> result = getAllChildByType(trn, rootType, id, childType)
                .stream()
                //mapEntites.entrySet().stream().filter(e -> e.getValue().getClass() == childType)
                //.filter(e -> e.getKey().startsWith(fqtrn))
                .filter(e -> EventRepoUtil.onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        ResultSet<K> col = EventRepoUtil.onPageable(result, paging);
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
        TenantRequirement tenantReq = Entity.checkTenantRequirement(rootType);
        switch (tenantReq) {
            case REQUIRED:
                trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/";
                break;
            case OPTIONAL:
                if (tenantId != null) {
                    trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/";
                } else {
                    trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/";
                }
                break;
            default:
                trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/";
                break;
        }
        String fqtrn = trn;
        Collection<T> filterCollection = mapEntites.entrySet().stream()
                .filter(e -> e.getKey().startsWith(fqtrn))
                .filter(e -> e.getValue().getClass() == rootType)
                .map(e -> (T) e.getValue())
                .filter(e -> EventRepoUtil.onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        return EventRepoUtil.onPageable(filterCollection, paging);
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
            stream = stream.filter(e -> e.getEntityTRN().equals(rn));
        } else {
            stream = stream.filter(e -> e.getEntityRN().equals(rn));
        }
        Collection<Event<T>> cols = stream.map(e -> (Event<T>) e)
                .filter(e -> EventRepoUtil.onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        return EventRepoUtil.onPageable(cols, paging);
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
                .filter(e -> EventRepoUtil.onFilter(e, paging.getParams()))
                .collect(Collectors.toList());
        return EventRepoUtil.onPageable(cols, paging);
    }

    @Override
    protected <C extends ChildEntity<T>> Collection<C> getAllChildByType(Class<T> rootType, String id, Class<C> childType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveRootEntityBrnIfNotExist(RootEntity e) {
        Entity old = mapEntites.putIfAbsent(resourceHelper.getFQBrn(e), e);
        if (old != null) {
            throw new RepositoryException("{0} exist.", e.getBRN());
        }
    }

    @Override
    protected void saveRootEntityTrnIfNotExist(RootEntity e) {
        Entity old = mapEntites.putIfAbsent(resourceHelper.getFQTrn(e), e);
        if (old != null) {
            throw new RepositoryException("{0} exist.", e.getTRN());
        }
    }

    @Override
    protected void saveRootEntityBrnIfExist(long lastSeq,RootEntity e) {
        mapEntites.put(resourceHelper.getFQBrn(e), e);
    }

    @Override
    protected void saveRootEntityTrnIfExist(long lastSeq,RootEntity e) {
        mapEntites.put(resourceHelper.getFQTrn(e), e);
    }

    @Override
    protected void saveChildEntityBrnIfNotExist(String rootTrn,ChildEntity e) {
        Entity old = mapEntites.putIfAbsent(resourceHelper.getFQBrn(e), e);
        if (old != null) {
            throw new RepositoryException("{0} exist.", e.getBRN());
        }
    }

    @Override
    protected void saveChildEntityTrnIfNotExist(String rootTrn,ChildEntity e) {
        Entity old = mapEntites.putIfAbsent(resourceHelper.getFQTrn(e), e);
        if (old != null) {
            throw new RepositoryException("{0} exist.", e.getTRN());
        }
    }

    @Override
    protected void saveChildEntityBrnIfExist(long lastSeq,String rootTrn,ChildEntity e) {
        mapEntites.put(resourceHelper.getFQBrn(e), e);
    }

    @Override
    protected void saveChildEntityTrnIfExist(long lastSeq,String rootTrn,ChildEntity e) {
        mapEntites.put(resourceHelper.getFQTrn(e), e);
    }

    @Override
    protected void deleteRootEntityBrnById(Class<T> rootType, String id, String tenantId) {
        EntityIdHelper.validateEntityId(id);
        String brn = resourceHelper.getFQBrn(RootEntity.makeRN(rootType, version, id, tenantId));
        mapEntites.remove(brn);
    }

    @Override
    protected void deleteRootEntityTrnById(Class<T> rootType, String id, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        String trn = resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, id, tenantId));
        mapEntites.remove(trn);
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityBrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        EntityIdHelper.validateEntityId(childId);
        String brn = resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId));
        mapEntites.remove(brn);
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityTrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        EntityIdHelper.validateTechnicalId(childId);
        String brn = resourceHelper.getFQTrn(ChildEntity.makeTRN(rootType, version, id, childType, childId, tenantId));
        mapEntites.remove(brn);
    }

    @Override
    protected void addEvent(Event event) {
        events.add(event);
    }

    @Override
    protected void updateCheckpoint(long lastSeq, EntityCheckpoint checkpoint) {
        checkpoints.put(checkpoint.getRootTrn(), checkpoint);
    }

}
