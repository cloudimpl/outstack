/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.*;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.EntityRenamed;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class SimpleTransaction<T extends RootEntity> implements ITransaction<T> {

    private final Map<String, Entity> mapTrnEntities;
    private final Map<String, Entity> mapBrnEntities;
    private final Map<String, Entity> mapDeleteEntities;
    private final Map<String, Entity> mapRenameEntities;

    private final List<Event> events;
    private final Class<? extends RootEntity> rootType;
    private final String version;
    private final QueryOperations<T> queryOperation;

    public SimpleTransaction(Class<? extends RootEntity> rootType, QueryOperations<T> queryOperation) {
        this.events = new LinkedList<>();
        this.mapTrnEntities = new HashMap<>();
        this.mapBrnEntities = new HashMap<>();
        this.mapRenameEntities = new HashMap<>();
        this.mapDeleteEntities = new HashMap<>();
        this.rootType = rootType;
        this.queryOperation = queryOperation;
        this.version = EntityMetaDetailCache.instance().getEntityMeta(rootType).getVersion();
    }

    @Override
    public List<Event> getEventList() {
        return events;
    }

    public void apply(Event e) {
        events.add(e);
        switch (e.getAction()) {
            case CREATE: {
                if (e.isRootEvent()) {
                    RootEntity root = RootEntity.create(e.getOwner(), e.entityId(), e.tenantId(), e.id());
                    EntityHelper.applyEvent(root,e);
                    mapBrnEntities.put(root.getBRN(), root);
                    mapTrnEntities.put(root.getTRN(), root);
                } else {
                    RootEntity root = (RootEntity) getRootById(e.getRootOwner(),e.rootId(), e.tenantId()).get();
                    ChildEntity child = root.createChildEntity(e.getOwner(), e.entityId(),e.id());
                    EntityHelper.applyEvent(child,e);
                    mapBrnEntities.put(child.getBRN(), child);
                    mapTrnEntities.put(child.getTRN(), child);
                }
                break;
            }
            case UPDATE: {
                if (e.isRootEvent()) {
                    T root = (T) getRootById(e.getOwner(), e.id(), e.tenantId()).get();
                    EntityHelper.applyEvent(root,e);
                    mapBrnEntities.put(root.getBRN(), root);
                    mapTrnEntities.put(root.getTRN(), root);
                } else {
                    ChildEntity child = (ChildEntity) getChildById(e.getRootOwner(), e.rootId(), e.getOwner(), e.entityId(), e.tenantId()).get();
                    EntityHelper.applyEvent(child,e);
                    mapBrnEntities.put(child.getBRN(), child);
                    mapTrnEntities.put(child.getTRN(), child);
                }
                break;
            }
            case DELETE: {
                if (e.isRootEvent()) {
                    RootEntity root = (RootEntity) getRootById(e.getRootOwner(), e.entityId(), e.tenantId()).get();
                    mapDeleteEntities.put(root.getBRN(), root);

                } else {
                    ChildEntity child = (ChildEntity) getChildById(e.getRootOwner(), e.rootId(), e.getOwner(), e.entityId(), e.tenantId()).get();
                    mapDeleteEntities.put(child.getBRN(), child);
                }
                break;
            }
            case RENAME: {
                EntityRenamed renameEvent = (EntityRenamed) e;
                if (e.isRootEvent()) {
                    RootEntity root = (RootEntity) getRootById(e.getRootOwner(), renameEvent.id(), e.tenantId()).get();
                    RootEntity newRoot = root.rename(renameEvent.entityId());
                    mapRenameEntities.put(root.getTRN(), root);
                    mapBrnEntities.put(newRoot.getBRN(), newRoot);
                    mapTrnEntities.put(newRoot.getTRN(), newRoot);
                } else {
                    ChildEntity child = (ChildEntity) getChildById(e.getRootOwner(), e.rootId(), e.getOwner(), e.id(), e.tenantId()).get();
                    ChildEntity newChild = child.rename(renameEvent.entityId());
                    mapRenameEntities.put(child.getTRN(), child);
                    mapBrnEntities.put(newChild.getBRN(), newChild);
                    mapTrnEntities.put(newChild.getTRN(), newChild);
                }
                break;
            }
        }
    }

    @Override
    public Collection<Entity> getEntityList() {
        return mapBrnEntities.values();
    }

    @Override
    public Map<String, Entity> getDeletedEntities() {
        return mapDeleteEntities;
    }

    @Override
    public Map<String, Entity> getRenameEntities() {
        return mapRenameEntities;
    }

    @Override
    public void setAttachment(Object attachment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <K> K getAttachment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <K> K getReply() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <C extends ChildEntity<T>, K extends Entity, Z extends EntityQueryContext> Z getContext(Class<K> entityType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputMetaProvider getInputMetaProvider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResultSet<T> getAllByRootType(Class<T> rootType, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet<T> getAllByRootType(Class<T> rootType, Collection<String> tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <K> K executeRawQuery(String rawQuery) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {
        if (EntityIdHelper.isTechnicalId(id)) {
            return Optional.ofNullable((T) mapTrnEntities.get(RootEntity.makeTRN(rootType, version, id, tenantId))).or(() -> queryOperation.getRootById(rootType, id, tenantId));
        } else {
            return Optional.ofNullable((T) mapBrnEntities.get(RootEntity.makeRN(rootType, version, id, tenantId))).or(() -> queryOperation.getRootById(rootType, id, tenantId));
        }

    }

    @Override
    public Optional<T> getRootById(Class<T> rootType, String id, String tenantId, boolean isIgnoreCase) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        if (EntityIdHelper.isTechnicalId(childId)) {
            return Optional.ofNullable((C) mapTrnEntities.get(ChildEntity.makeTRN(rootType, version, id, childType, childId, tenantId))).or(() -> queryOperation.getChildById(rootType, id, childType, childId, tenantId));
        } else {
            return Optional.ofNullable((C) mapBrnEntities.get(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId))).or(() -> queryOperation.getChildById(rootType, id, childType, childId, tenantId));
        }
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<C> getAllChildByType(Class<T> rootType, String id, Class<C> childType, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T1 extends ChildEntity<T>> ResultSet<T1> getAllChildByType(Class<T> rootType, String id, Class<T1> childType, Collection<String> tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ResultSet<Event<T>> getEventsByRootId(Class<T> rootType, String rootId, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<Event<C>> getEventsByChildId(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isIdExist(String id, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<T> getRootType() {
        return (Class<T>) this.rootType;
    }
}
