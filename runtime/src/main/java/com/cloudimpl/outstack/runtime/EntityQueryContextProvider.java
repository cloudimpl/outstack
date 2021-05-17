/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import static com.cloudimpl.outstack.runtime.EventRepository.TID_PREFIX;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class EntityQueryContextProvider<T extends RootEntity> {

    protected final QueryOperations<T> queryOperation;
    protected final Supplier<String> idGenerator;
    protected final ValidatorFactory factory;
    protected final Validator validator;
    protected final Function<Class<? extends RootEntity>,QueryOperations<?>> queryOperationSelector;
    protected final Class<T> type;
    protected final String version;
    public EntityQueryContextProvider(Class<T> type,Supplier<String> idGenerator, QueryOperations<T> queryOperation,Function<Class<? extends RootEntity>,QueryOperations<?>> queryOperationSelector) {
        this.type = type;
        this.version = Entity.getVersion(type);
        this.idGenerator = idGenerator;
        this.queryOperation = queryOperation;
        this.factory = Validation.buildDefaultValidatorFactory();
        this.validator = this.factory.getValidator();
        this.queryOperationSelector = queryOperationSelector;
    }

    public ReadOnlyTransaction<T> createTransaction(String rootTid, String tenantId) {
        return new ReadOnlyTransaction(idGenerator, rootTid, tenantId, queryOperation,this::validateObject,this.queryOperationSelector,version);
    }

    private <T> void validateObject(T target)
    {
        Set<ConstraintViolation<T>> violations = this.validator.validate(target);
        if(!violations.isEmpty())
        {
            ValidationErrorException error = new ValidationErrorException(violations.stream().findFirst().get().getMessage());
            throw error;
        }          
    }

    public String getVersion() {
        return version;
    }
    
    
    public static  class ReadOnlyTransaction< R extends RootEntity> implements QueryOperations<R> {

        protected final QueryOperations<R> queryOperation;
        protected final String tenantId;
        protected final Supplier<String> idGenerator;
        protected String rootTid;
        protected Object reply;
        protected final Consumer<Object> validator;
        protected final Function<Class<? extends RootEntity>,QueryOperations<?>> queryOperationSelector;
        protected final String version;
        public ReadOnlyTransaction(Supplier<String> idGenerator, String rootTid,
                String tenantId, QueryOperations<R> queryOperation,Consumer<Object> validator,
                Function<Class<? extends RootEntity>,QueryOperations<?>> queryOperationSelector,String version) {
            this.idGenerator = idGenerator;
            this.rootTid = rootTid;
            this.tenantId = tenantId;
            this.queryOperation = queryOperation;
            this.validator = validator;
            this.queryOperationSelector = queryOperationSelector;
            this.version = version;
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

        public <C extends ChildEntity<R>, K extends Entity> EntityContext<?> getContext(Class<K> entityType) {
           
            if (RootEntity.isMyType(entityType)) {
                Class<R> rootType = (Class<R>) entityType;
                return new RootEntityContext<>(rootType, rootTid, tenantId, Optional.empty(), idGenerator, Optional.empty(), this, Optional.empty(),validator,this.queryOperationSelector,version);
            } else {
                validateRootTid();
                Class<R> rootType = Util.extractGenericParameter(entityType, ChildEntity.class, 0);
                Class<C> childType = (Class<C>) entityType;
                return new ChildEntityContext<>(rootType, rootTid, childType, tenantId, Optional.empty(), idGenerator, Optional.empty(), this, Optional.empty(),validator,this.queryOperationSelector,version);
            }
        }

        private void validateRootTid() {
            if (rootTid == null) {
                throw new ServiceProviderException("rootId not available");
            }
        }
        
        @Override
        public Optional<R> getRootById(Class<R> rootType, String id, String tenantId) {
            if (id.startsWith(TID_PREFIX)) {
                return queryOperation.getRootById(rootType, id, tenantId);
            } else {
                return queryOperation.getRootById(rootType, id, tenantId);
            }
        }

        @Override
        public <T extends ChildEntity<R>> Optional<T> getChildById(Class<R> rootType, String id, Class<T> childType, String childId, String tenantId) {
            EntityIdHelper.validateTechnicalId(id);
            if (childId.startsWith(TID_PREFIX)) {
                return  queryOperation.getChildById(rootType, id, childType, childId, tenantId);
            } else {
                return  queryOperation.getChildById(rootType, id, childType, childId, tenantId);
            }

        }

        @Override
        public <T extends ChildEntity<R>> ResultSet<T> getAllChildByType(Class<R> rootType, String id, Class<T> childType, String tenantId,Query.PagingRequest pageable) {
            return queryOperation.getAllChildByType(rootType, id, childType, tenantId,pageable);
            
        }

        @Override
        public ResultSet<R> getAllByRootType(Class<R> rootType,String tenantId,Query.PagingRequest paging) {
            return queryOperation.getAllByRootType(rootType, tenantId, paging);
        }

        @Override
        public ResultSet<Event<R>> getEventsByRootId(Class<R> rootType, String rootId, String tenantId, Query.PagingRequest paging) {
            return queryOperation.getEventsByRootId(rootType, rootId, tenantId, paging);
        }

        @Override
        public <T extends ChildEntity<R>> ResultSet<Event<T>> getEventsByChildId(Class<R> rootType, String id, Class<T> childType, String childId, String tenantId, Query.PagingRequest paging) {
            return queryOperation.getEventsByChildId(rootType, id, childType, childId, tenantId, paging);
        }

        
    }
}
