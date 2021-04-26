/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.ChildEntity;
import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class EntityContextProvider<T extends RootEntity> {

    private final Function<String, ? extends Entity> entityProvider;
    private final Supplier<String> idGenerator;
    private final ResourceHelper resourceHelper;

    public EntityContextProvider(Function<String, ? extends Entity> entityProvider, Supplier<String> idGenerator, ResourceHelper resourceHelper) {
        this.entityProvider = entityProvider;
        this.idGenerator = idGenerator;
        this.resourceHelper = resourceHelper;
    }

    public Transaction createTransaction(String tenantId) {
        return new Transaction(entityProvider, idGenerator, tenantId, resourceHelper);
    }

    public static final class Transaction< R extends RootEntity> implements CRUDOpertations {

        private final Map<String, Entity> mapBrnEntities;
        private final Map<String, Entity> mapTrnEntities;
        private final Function<String, ? extends Entity> entityProvider;
        private final String tenantId;
        private final Supplier<String> idGenerator;
        private final ResourceHelper resourceHelper;
        private String rootTid;
        private Object reply;
        private final List<Event> eventList;

        public Transaction(Function<String, ? extends Entity> entityProvider, Supplier<String> idGenerator, String tenantId, ResourceHelper resourceHelper) {
            this.mapBrnEntities = new HashMap<>();
            this.mapTrnEntities = new HashMap<>();
            this.entityProvider = entityProvider;
            this.idGenerator = idGenerator;
            this.tenantId = tenantId;
            this.resourceHelper = resourceHelper;
            this.eventList = new LinkedList<>();
        }

        public void setRootTid(String rootTid) {
            this.rootTid = rootTid;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getRootTid() {
            return rootTid;
        }

        public void setReply(Object reply) {
            this.reply = reply;
        }

        public <K> K getReply() {
            return (K) reply;
        }

        public List<Event> getEventList() {
            return eventList;
        }

        protected void publishEvent(Event event) {
            this.eventList.add(event);
        }

        public String getEntityTrn(Event event) {
            return resourceHelper.getFQTrn(event.getEntityTRN());
        }
        
        public String getEntityBrn(Event event) {
            return resourceHelper.getFQTrn(event.getEntityRN());
        }
        
         public String getEntityBrn(String resourceRN) {
            return resourceHelper.getFQTrn(resourceRN);
        }

        public <C extends ChildEntity<R>, K extends Entity> EntityContext<?> getContext(Class<K> entityType) {
            if (RootEntity.isMyType(entityType)) {
                Class<R> rootType = (Class<R>) entityType;
                return new RootEntityContext<>(rootType, tenantId, this::getEntity, idGenerator, resourceHelper, this, this::publishEvent);
            } else {
                Class<R> rootType = Util.extractGenericParameter(entityType, ChildEntity.class, 0);
                Class<C> childType = (Class<C>) entityType;
                return new ChildEntityContext<>(rootType, rootTid, childType, tenantId, this::getEntity, idGenerator, resourceHelper, this, this::publishEvent);
            }
        }

        public <E extends Entity> E getEntity(String fqId) {
            E entity;
            if (fqId.startsWith("brn:")) {
                entity = (E) Optional.ofNullable(mapBrnEntities.get(fqId)).orElse(entityProvider.apply(fqId));
            } else if (fqId.startsWith("trn:")) {
                entity = (E) Optional.ofNullable(mapTrnEntities.get(fqId)).orElse(entityProvider.apply(fqId));
            } else {
                throw new ServiceProviderException("unknown resource prefix in fqid {0} ", fqId);
            }
            if (entity == null || entity == RootEntity.DELETED || entity == ChildEntity.DELETED) {
                return null;
            }
            if (entity.isRoot()) {
                if (this.rootTid == null) {
                    this.rootTid = entity.tid();
                } else if (!this.rootTid.equals(entity.tid())) {
                    throw new ServiceProviderException("multiple root id modification on same transaction not supported. expecting {0} , found {1}", this.rootTid, entity.id());
                }

            }
            return entity;
        }

        public void putEntity(Entity entity) {
            mapBrnEntities.put(resourceHelper.getFQBrn(entity), entity);
            mapTrnEntities.put(resourceHelper.getFQTrn(entity), entity);
            if (entity.isRoot()) {
                if (this.rootTid == null) {
                    this.rootTid = entity.tid();
                } else if (!this.rootTid.equals(entity.tid())) {
                    throw new ServiceProviderException("multiple root id modification on same transaction not supported. expecting {0} , found {1}", this.rootTid, entity.id());
                }

            }
        }

        public void endTransaction() {

        }

        @Override
        public void create(Entity entity) {
            putEntity(entity);
        }

        @Override
        public void update(Entity entity) {
            putEntity(entity);
        }

        @Override
        public void delete(Entity entity) {
            if (entity.isRoot()) {
                mapBrnEntities.put(resourceHelper.getFQBrn(entity), RootEntity.DELETED);
            } else {
                mapBrnEntities.put(resourceHelper.getFQBrn(entity), ChildEntity.DELETED);
            }
        }

        @Override
        public void rename(String oldId, Entity entity) {
            if (entity.isRoot()) {
                mapBrnEntities.remove(resourceHelper.getFQBrn(RootEntity.makeRN((Class<? extends RootEntity>) entity.getClass(), oldId, getTenantId())));
            } else {
                ChildEntity child = (ChildEntity) entity;
                mapBrnEntities.remove(resourceHelper.getFQBrn(ChildEntity.makeRN(child.rootType(), child.rootTid(), child.getClass(), oldId, getTenantId())));
            }
            mapBrnEntities.put(resourceHelper.getFQBrn(entity), entity);
        }
    }
}
