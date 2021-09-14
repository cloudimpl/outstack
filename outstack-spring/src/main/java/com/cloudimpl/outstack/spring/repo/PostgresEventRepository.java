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
package com.cloudimpl.outstack.spring.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Delete;
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.amazonaws.services.dynamodbv2.model.Update;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.EntityCheckpoint;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.domainspec.TenantRequirement;
import com.cloudimpl.outstack.runtime.repo.EventRepoUtil;
import com.cloudimpl.outstack.runtime.repo.RepositoryException;
import com.cloudimpl.outstack.spring.component.SpringApplicationConfigManager.Provider;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class PostgresEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final Provider.ProviderConfigs configs;
    private String entityTable;
    private String eventTable;

    private PostgresRepositoryFactory factory;
    //private final int partitionCount;
    private final String tableName;
    ThreadLocal<List<Function<Connection, Integer>>> txContext = ThreadLocal.withInitial(() -> new LinkedList<>());

    public PostgresEventRepository(PostgresRepositoryFactory factory, Class<T> rootType, ResourceHelper resourceHelper, Provider.ProviderConfigs configs) {
        super(rootType, resourceHelper);
        this.factory = factory;
        //this.loadTables();
        this.configs = configs;
        this.tableName = this.configs.getOption(rootType.getSimpleName() + "Table").or(() -> this.configs.getOption("defaultTable")).get();
        System.out.println("table name " + this.tableName + " pick for root type: " + rootType.getSimpleName());
        //this.partitionCount = Integer.valueOf(configs.getOption("partitionCount").get());
    }

    @Override
    protected void startTransaction() {

        List<Function<Connection, Integer>> list = txContext.get();
        list.clear();
    }

    @Override
    protected void endTransaction() {
        factory.execute(txContext.get());
    }

    @Override
    protected void saveRootEntityBrnIfNotExist(RootEntity e) {

        String tenantId = e.getTenantId() != null ? e.getTenantId() : "nonTenant";
        String rn = resourceHelper.getFQBrn(e.getBRN());
        Function<Connection, Integer> func = conn -> factory.insertEntity(conn, tableName, tenantId, rn, e.getClass().getSimpleName(), e.id(), GsonCodec.encode(e), e.getMeta().getLastSeq());
        txContext.get().add(func);

    }

    @Override
    protected void saveRootEntityTrnIfNotExist(RootEntity e) {

    }

    @Override
    protected void saveRootEntityBrnIfExist(long lastSeq, RootEntity e) {

        String tenantId = e.getTenantId() != null ? e.getTenantId() : "nonTenant";
        String rn = resourceHelper.getFQBrn(e.getBRN());
        Function<Connection, Integer> func = conn -> factory.updateEntity(conn, tableName, tenantId, rn, e.getClass().getSimpleName(), e.id(), GsonCodec.encode(e), lastSeq, e.getMeta().getLastSeq());
        txContext.get().add(func);

    }

    @Override
    protected void saveRootEntityTrnIfExist(long lastSeq, RootEntity e) {

    }

    @Override
    protected void saveChildEntityBrnIfNotExist(ChildEntity e) {

        String tenantId = e.getTenantId() != null ? e.getTenantId() : "nonTenant";
        String rn = resourceHelper.getFQBrn(e.getBRN());
        Function<Connection, Integer> func = conn -> factory.insertEntity(conn, tableName, tenantId, rn, e.getClass().getSimpleName(), e.id(), GsonCodec.encode(e), e.getMeta().getLastSeq());
        txContext.get().add(func);

    }

    @Override
    protected void saveChildEntityTrnIfNotExist(ChildEntity e) {

    }

    @Override
    protected void saveChildEntityBrnIfExist(long lastSeq, ChildEntity e) {

        String tenantId = e.getTenantId() != null ? e.getTenantId() : "nonTenant";
        String rn = resourceHelper.getFQBrn(e.getBRN());
        Function<Connection, Integer> func = conn -> factory.updateEntity(conn, tableName, tenantId, rn, e.getClass().getSimpleName(), e.id(), GsonCodec.encode(e), lastSeq, e.getMeta().getLastSeq());
        txContext.get().add(func);

    }

    @Override
    protected void saveChildEntityTrnIfExist(long lastSeq, ChildEntity e) {

    }

    @Override
    protected void deleteRootEntityBrnById(RootEntity e) {

        String tenantId = e.getTenantId() != null ? e.getTenantId() : "nonTenant";
        String rn = resourceHelper.getFQBrn(RootEntity.makeRN(rootType, version, e.entityId(), e.getTenantId()));
        Function<Connection, Integer> func = conn -> factory.deleteEntity(conn, tableName, tenantId, rn, e.id());
        txContext.get().add(func);

    }

    @Override
    protected void deleteRootEntityTrnById(Class<T> rootType, String id, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityBrnById(ChildEntity e) {

        String tenantId = e.getTenantId() != null ? e.getTenantId() : "nonTenant";
        String rn = resourceHelper.getFQBrn(ChildEntity.makeRN(e.rootType(), version, e.rootId(), e.getClass(), e.entityId(), e.getTenantId()));
        Function<Connection, Integer> func = conn -> factory.deleteEntity(conn, tableName, tenantId, rn, e.id());
        txContext.get().add(func);

    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityTrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Optional<EntityCheckpoint> _getCheckpoint(String rootTrn) {

        String partitionKey = resourceHelper.getFQTrn(rootTrn)+":checkpoint";

        Function<Connection, Optional<String>> fn = conn -> factory.getEntityByBrn(conn, tableName, partitionKey, "nonTenant");
        Optional<String> out = factory.executeQuery(fn);
        return out.map(s -> GsonCodec.decode(EntityCheckpoint.class, s)).or(() -> Optional.of(new EntityCheckpoint(rootTrn).setSeq(0)));
    }

    @Override
    protected void addEvent(Event event) {

        String trn = resourceHelper.getFQTrn(event.getRootEntityTRN());
        Function<Connection, Integer> func = conn -> factory.insertEvent(conn, tableName+"Events", event.tenantId() == null?"nonTenant": event.tenantId(), trn, event.getOwner().getSimpleName(),event.id(), event.getClass().getSimpleName(), event.getSeqNum(), GsonCodec.encodeWithType(event));
        txContext.get().add(func);
    }

    private String getRootBrn(Class<T> rootType, String tenantId) {
        String trn = null;
        TenantRequirement tenantReq = Entity.checkTenantRequirement(rootType);
        switch (tenantReq) {
            case REQUIRED:
                trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName();
                break;
            case OPTIONAL:
                if (tenantId != null) {
                    trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName();
                } else {
                    trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName();
                }
                break;
            default:
                trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName();
                break;
        }
        return trn;
    }

    private <C extends ChildEntity<T>> String getChildBrn(Class<T> rootType, String id, Class<C> childType, String tenantId) {
        EntityIdHelper.validateTechnicalId(id);
        String trn = null;
        TenantRequirement tenantReq = Entity.checkTenantRequirement(rootType);
        switch (tenantReq) {
            case REQUIRED:
                trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/" + id + "/" + childType.getSimpleName() + "/";
                break;
            case OPTIONAL:
                if (tenantId != null) {
                    trn = resourcePrefix("brn") + ":tenant/" + tenantId + "/" + version + "/" + rootType.getSimpleName() + "/" + id + "/" + childType.getSimpleName() + "/";
                } else {
                    trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/" + id + "/" + childType.getSimpleName() + "/";
                }
                break;
            default:
                trn = resourcePrefix("brn") + ":" + version + "/" + rootType.getSimpleName() + "/" + id + "/" + childType.getSimpleName() + "/";
                break;
        }
        return trn;
    }

    @Override
    public ResultSet<T> getAllByRootType(Class<T> rootType, String tenantId, Query.PagingRequest paging) {
        String t = tenantId != null ? tenantId : "nonTenant";
        Function<Connection, Collection<String>> fn = conn -> factory.getEntityByType(conn, tableName, rootType.getSimpleName(), t);

        List items = factory.executeQuery(fn).stream().map(s -> GsonCodec.decode(rootType, s)).filter(i -> EventRepoUtil.onFilter(i, paging.getParams())).collect(Collectors.toList());
        return EventRepoUtil.onPageable(items, paging);

    }

    @Override
    public Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {

        String t = tenantId != null ? tenantId : "nonTenant";
        Function<Connection, Optional<String>> fn;

        if (EntityIdHelper.isTechnicalId(id)) {
            fn = conn -> factory.getEntityByTrn(conn, tableName, rootType.getSimpleName(), id, t);
        } else {
            String brn = resourceHelper.getFQBrn(RootEntity.makeRN(rootType, version, id, tenantId));
            fn = conn -> factory.getEntityByBrn(conn, tableName, brn, t);
        }

        Optional<String> out = factory.executeQuery(fn);
        return out.map(s -> GsonCodec.decode(rootType, s));
    }

    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {

        String t = tenantId != null ? tenantId : "nonTenant";
        EntityIdHelper.validateTechnicalId(id);

        Function<Connection, Optional<String>> fn;
        if (EntityIdHelper.isTechnicalId(childId)) {
            fn = conn -> factory.getEntityByTrn(conn, tableName, childType.getSimpleName(), childId, t);
        } else {
            String brn = resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId));
            fn = conn -> factory.getEntityByBrn(conn, tableName, brn, t);
        }
        Optional<String> out = factory.executeQuery(fn);
        return out.map(s -> GsonCodec.decode(childType, s));
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<C> getAllChildByType(Class<T> rootType, String id, Class<C> childType, String tenantId, Query.PagingRequest paging) {
        String t = tenantId != null ? tenantId : "nonTenant";
        Function<Connection, Collection<String>> fn = conn -> factory.getEntityByType(conn, tableName, childType.getSimpleName(), t);

        List items = factory.executeQuery(fn).stream().map(s -> GsonCodec.decode(childType,s)).filter(i -> EventRepoUtil.onFilter(i, paging.getParams())).collect(Collectors.toList());
        return EventRepoUtil.onPageable(items, paging);
    }

    @Override
    public ResultSet<Event<T>> getEventsByRootId(Class<T> rootType, String rootId, String tenantId, Query.PagingRequest paging) {
        String t = tenantId != null ? tenantId : "nonTenant";
        String trn = resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, rootId, tenantId));

        Function<Connection, Collection<String>> fn = conn -> factory.getEvents(conn, tableName+"Events", t, trn, this.rootType.getSimpleName(), rootId, Long.MAX_VALUE);

        List items = factory.executeQuery(fn).stream().map(s -> GsonCodec.decode(s)).filter(i -> EventRepoUtil.onFilter(i, paging.getParams())).collect(Collectors.toList());
        return EventRepoUtil.onPageable(items, paging);
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<Event<C>> getEventsByChildId(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId, Query.PagingRequest paging) {
        EntityIdHelper.validateTechnicalId(id);
        String t = tenantId != null ? tenantId : "nonTenant";
        if (!EntityIdHelper.isTechnicalId(childId)) {
            childId = getChildById(rootType, id, childType, childId, tenantId).get().id();
        }

        String trn = resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, id, tenantId));

        String childTid = childId;
        Function<Connection, Collection<String>> fn = conn -> factory.getEvents(conn, tableName+"Events", t, trn, childType.getSimpleName(), childTid, Long.MAX_VALUE);

        List items = factory.executeQuery(fn).stream().map(s -> GsonCodec.decode(s)).filter(i -> EventRepoUtil.onFilter(i, paging.getParams())).collect(Collectors.toList());
        return EventRepoUtil.onPageable(items, paging);
    }

    @Override
    protected void updateCheckpoint(EntityCheckpoint checkpoint) {

        String partitionKey = resourceHelper.getFQTrn(checkpoint.getRootTrn())+":checkpoint";

        Function<Connection, Integer> func = conn -> factory.insertCheckpoint(conn, tableName, "nonTenant", partitionKey, EntityCheckpoint.class.getSimpleName(), "-", GsonCodec.encode(checkpoint), checkpoint.getSeq());
        txContext.get().add(func);

    }

}