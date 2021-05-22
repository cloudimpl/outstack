/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import static com.cloudimpl.outstack.runtime.EventRepositoy.TID_PREFIX;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class EntityContextProvider<T extends RootEntity> extends EntityQueryContextProvider<T> {

    private final EntityProvider entityProvider;

    public EntityContextProvider(Class<T> type, EntityProvider entityProvider, Supplier<String> idGenerator, QueryOperations<T> queryOperation, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector) {
        super(type, idGenerator, queryOperation, queryOperationSelector);
        this.entityProvider = entityProvider;
    }

    public Transaction<T> createWritableTransaction(String rootTid, String tenantId) {
        return new Transaction(entityProvider, idGenerator, rootTid, tenantId, queryOperation, this::validateObject, this.queryOperationSelector, version);
    }

    private <T> void validateObject(T target) {
        Set<ConstraintViolation<T>> violations = this.validator.validate(target);
        if (!violations.isEmpty()) {
            ValidationErrorException error = new ValidationErrorException(violations.stream().findFirst().get().getMessage());
            throw error;
        }
    }

    public static final class Transaction< R extends RootEntity> extends ReadOnlyTransaction<R> implements CRUDOperations {

        private final TreeMap<String, Entity> mapEntities;
        private final EntityProvider entityProvider;
        private Object reply;
        private final List<Event> eventList;

        public Transaction(EntityProvider entityProvider, Supplier<String> idGenerator, String rootTid,
                String tenantId, QueryOperations<R> queryOperation, Consumer<Object> validator, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector, String version) {
            super(idGenerator, rootTid, tenantId, queryOperation, validator, queryOperationSelector, version);
            this.mapEntities = new TreeMap<>();
            this.entityProvider = entityProvider;
            this.eventList = new LinkedList<>();
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

        @Override
        public <C extends ChildEntity<R>, K extends Entity> EntityContext<?> getContext(Class<K> entityType) {
            if (RootEntity.isMyType(entityType)) {
                Class<R> rootType = (Class<R>) entityType;
                return new RootEntityContext(rootType,
                        rootTid, tenantId,
                        Optional.of((EntityProvider) this::loadEntity),
                        idGenerator, Optional.of((CRUDOperations) this),
                        this,
                        Optional.of((Consumer<Event>) this::publishEvent),
                        validator, this.queryOperationSelector, version);
            } else {
                validateRootTid();
                Class<R> rootType = Util.extractGenericParameter(entityType, ChildEntity.class, 0);
                Class<C> childType = (Class<C>) entityType;
                return new ChildEntityContext<R, C>(
                        rootType,
                        rootTid, childType, tenantId,
                        Optional.of((EntityProvider) this::loadEntity), idGenerator, Optional.of((CRUDOperations) this),
                        this, Optional.of((Consumer<Event>) this::publishEvent), validator, this.queryOperationSelector, version);
            }
        }

        private void validateRootTid() {
            if (rootTid == null) {
                throw new ServiceProviderException("rootId not available");
            }
        }

        public <K extends Entity, C extends ChildEntity<R>> Optional<K> loadEntity(Class<R> rootType, String id, Class<C> childType, String childId, String tenantId) {
            if (childType == null) {
                return (Optional<K>) loadRootEntity(rootType, id, tenantId)
                        .or(() -> entityProvider.loadEntity(rootType, id, childType, childId, tenantId));
            } else {
                return (Optional<K>) loadChildEntity(rootType, id, childType, childId, tenantId).or(() -> entityProvider.loadEntity(rootType, id, childType, childId, tenantId));
            }
        }

        protected Optional<R> loadRootEntity(Class<R> rootType, String id, String tenantId) {
            if (id.startsWith(TID_PREFIX)) {
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeTRN(rootType, version, id, tenantId)));
            } else {
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeRN(rootType, version, id, tenantId)));
            }
        }

        protected <C extends ChildEntity<R>> Optional<C> loadChildEntity(Class<R> rootType, String id, Class<C> childType, String childId, String tenantId) {
            EntityIdHelper.validateTechnicalId(id);
            if (childId.startsWith(TID_PREFIX)) {
                return Optional.ofNullable((C) mapEntities.get(ChildEntity.makeTRN(rootType, version, id, childType, childId, tenantId)));
            } else {
                return Optional.ofNullable((C) mapEntities.get(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId)));
            }
        }

        public void putEntity(Entity entity) {
            mapEntities.put(entity.getBRN(), entity);
            mapEntities.put(entity.getTRN(), entity);
            if (entity.isRoot()) {
                if (this.rootTid == null) {
                    this.rootTid = entity.id();
                } else if (!this.rootTid.equals(entity.id())) {
                    throw new ServiceProviderException("multiple root id modification on same transaction not supported. expecting {0} , found {1}", this.rootTid, entity.entityId());
                }
            }
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
            mapEntities.get(entity.getBRN());
        }

        @Override
        public void rename(Entity oldEntity, Entity newEntity) {
            mapEntities.remove(oldEntity.getBRN());
            mapEntities.put(newEntity.getBRN(), newEntity);
            mapEntities.put(newEntity.getTRN(), newEntity);
        }

        @Override
        public Optional<R> getRootById(Class<R> rootType, String id, String tenantId) {
            if (id.startsWith(TID_PREFIX)) {
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeTRN(rootType, version, id, tenantId))).or(() -> queryOperation.getRootById(rootType, id, tenantId));
            } else {
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeRN(rootType, version, id, tenantId))).or(() -> queryOperation.getRootById(rootType, id, tenantId));
            }
        }

        @Override
        public <T extends ChildEntity<R>> Optional<T> getChildById(Class<R> rootType, String id, Class<T> childType, String childId, String tenantId) {
            EntityIdHelper.validateTechnicalId(id);
            if (childId.startsWith(TID_PREFIX)) {
                return Optional.ofNullable((T) mapEntities.get(ChildEntity.makeTRN(rootType, version, id, childType, id, tenantId)))
                        .or(() -> queryOperation.getChildById(rootType, id, childType, childId, tenantId));
            } else {
                return Optional.ofNullable((T) mapEntities.get(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId)))
                        .or(() -> queryOperation.getChildById(rootType, id, childType, childId, tenantId));
            }

        }

        @Override
        public <T extends ChildEntity<R>> ResultSet<T> getAllChildByType(Class<R> rootType, String id, Class<T> childType, String tenantId, Query.PagingRequest pageable) {

            Map<String, T> map = (Map<String, T>) new HashMap<>(queryOperation.getAllChildByType(rootType, id, childType, tenantId, pageable).getItems().stream().collect(Collectors.toMap(c -> c.getTRN(), c -> (T) c)));
            mapEntities.headMap(RootEntity.makeTRN(rootType, version, id, tenantId)).entrySet().forEach(p -> map.put(p.getKey(), (T) p.getValue()));
            Collection<T> out = map.values();
            return new ResultSet<>(out.size(), (int) Math.ceil(((double) out.size()) / pageable.pageSize()), pageable.pageNum(), out);
        }

        @Override
        public ResultSet<R> getAllByRootType(Class<R> rootType, String tenantId, Query.PagingRequest paging) {
            return queryOperation.getAllByRootType(rootType, tenantId, paging);
        }
    }
}
