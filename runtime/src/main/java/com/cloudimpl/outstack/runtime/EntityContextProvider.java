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
public class EntityContextProvider<T extends RootEntity> {

    private final EntityProvider entityProvider;
    private final QueryOperations<T> queryOperation;
    private final Supplier<String> idGenerator;
    private final ValidatorFactory factory;
    private final Validator validator;
    public EntityContextProvider(EntityProvider entityProvider, Supplier<String> idGenerator, QueryOperations<T> queryOperation) {
        this.entityProvider = entityProvider;
        this.idGenerator = idGenerator;
        this.queryOperation = queryOperation;
        this.factory = Validation.buildDefaultValidatorFactory();
        this.validator = this.factory.getValidator();
    }

    public Transaction createTransaction(String rootTid, String tenantId) {
        return new Transaction(entityProvider, idGenerator, rootTid, tenantId, queryOperation,this::validateObject);
    }

    private <T> void validateObject(T target)
    {
        Set<ConstraintViolation<T>> violations = this.validator.validate(target);
        if(!violations.isEmpty())
        {
            //TODO throw error here
        }          
    }
    public static final class Transaction< R extends RootEntity> implements CRUDOperations, QueryOperations<R> {

        private final TreeMap<String, Entity> mapEntities;
        private final EntityProvider entityProvider;
        private final QueryOperations<R> queryOperation;
        private final String tenantId;
        private final Supplier<String> idGenerator;
        private String rootTid;
        private Object reply;
        private final List<Event> eventList;
        private final Consumer<Object> validator;
        public Transaction(EntityProvider entityProvider, Supplier<String> idGenerator, String rootTid, String tenantId, QueryOperations<R> queryOperation,Consumer<Object> validator) {
            this.mapEntities = new TreeMap<>();
            this.entityProvider = entityProvider;
            this.idGenerator = idGenerator;
            this.rootTid = rootTid;
            this.tenantId = tenantId;
            this.queryOperation = queryOperation;
            this.eventList = new LinkedList<>();
            this.validator = validator;
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

        public <C extends ChildEntity<R>, K extends Entity> EntityContext<?> getContext(Class<K> entityType) {
            if (RootEntity.isMyType(entityType)) {
                Class<R> rootType = (Class<R>) entityType;
                return new RootEntityContext<>(rootType, rootTid, tenantId, this::loadEntity, idGenerator, this, this, this::publishEvent);
            } else {
                validateRootTid();
                Class<R> rootType = Util.extractGenericParameter(entityType, ChildEntity.class, 0);
                Class<C> childType = (Class<C>) entityType;
                return new ChildEntityContext<>(rootType, rootTid, childType, tenantId, this::loadEntity, idGenerator, this, this, this::publishEvent);
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
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeTRN(rootType, id, tenantId)));
            } else {
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeRN(rootType, id, tenantId)));
            }
        }

        protected <C extends ChildEntity<R>> Optional<C> loadChildEntity(Class<R> rootType, String id, Class<C> childType, String childId, String tenantId) {
            EntityIdHelper.validateTechnicalId(id);
            if (childId.startsWith(TID_PREFIX)) {
                return Optional.ofNullable((C) mapEntities.get(ChildEntity.makeTRN(rootType, id, childType, childId, tenantId)));
            } else {
                return Optional.ofNullable((C) mapEntities.get(ChildEntity.makeRN(rootType, id, childType, childId, tenantId)));
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
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeTRN(rootType, id, tenantId))).or(() -> queryOperation.getRootById(rootType, id, tenantId));
            } else {
                return Optional.ofNullable((R) mapEntities.get(RootEntity.makeRN(rootType, id, tenantId))).or(() -> queryOperation.getRootById(rootType, id, tenantId));
            }
        }

        @Override
        public <T extends ChildEntity<R>> Optional<T> getChildById(Class<R> rootType, String id, Class<T> childType, String childId, String tenantId) {
            EntityIdHelper.validateTechnicalId(id);
            if (childId.startsWith(TID_PREFIX)) {
                return Optional.ofNullable((T) mapEntities.get(ChildEntity.makeTRN(rootType, id, childType, id, tenantId)))
                        .or(() -> queryOperation.getChildById(rootType, id, childType, childId, tenantId));
            } else {
                return Optional.ofNullable((T) mapEntities.get(ChildEntity.makeRN(rootType, id, childType, childId, tenantId)))
                        .or(() -> queryOperation.getChildById(rootType, id, childType, childId, tenantId));
            }

        }

        @Override
        public <T extends ChildEntity<R>> Collection<T> getAllChildByType(Class<R> rootType, String id, Class<T> childType, String tenantId) {

            Map<String, T> map = (Map<String, T>) new HashMap<>(queryOperation.getAllChildByType(rootType, id, childType, tenantId).stream().collect(Collectors.toMap(c -> c.getTRN(), c -> (T) c)));
            mapEntities.headMap(RootEntity.makeTRN(rootType, id, tenantId)).entrySet().forEach(p -> map.put(p.getKey(), (T) p.getValue()));
            return map.values();
        }
    }
}
