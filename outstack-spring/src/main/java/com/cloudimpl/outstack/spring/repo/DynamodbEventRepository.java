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
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
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
import java.text.MessageFormat;
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
 * @param <T>
 */
public class DynamodbEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final Provider.ProviderConfigs configs;
    private final DynamoDB dynamodb;
    private final AmazonDynamoDB client;
    private String entityTable;
    private String eventTable;
    public static final String partitionKey = "partitionKey";
    public static final String rangeKey = "rangeKey";
    public static final String json = "json";
    public static final String lastSeq = "lastSeq";
    public static final String eventType = "eventType";
    //private final int partitionCount;
    private final String tableName;
    ThreadLocal<List<TransactWriteItem>> txContext = ThreadLocal.withInitial(() -> new LinkedList<>());

    public DynamodbEventRepository(AmazonDynamoDB client, DynamoDB dynamodb, Class<T> rootType, ResourceHelper resourceHelper, EventStream eventStream, Provider.ProviderConfigs configs) {
        super(rootType, resourceHelper, eventStream);
        this.dynamodb = dynamodb;
        this.client = client;
        //this.loadTables();
        this.configs = configs;
        this.tableName = this.configs.getOption(rootType.getSimpleName() + "Table", "defaultTable").get();
        System.out.println("table name " + this.tableName + " pick for root type: " + rootType.getSimpleName());
        //this.partitionCount = Integer.valueOf(configs.getOption("partitionCount").get());
    }

    @Override
    protected void startTransaction() {
        List<TransactWriteItem> list = txContext.get();
        list.clear();

    }

    @Override
    protected void endTransaction() {
        TransactWriteItemsRequest placeOrderTransaction = new TransactWriteItemsRequest()
                .withTransactItems(txContext.get())
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        // Execute the transaction and process the result.
        try {
            client.transactWriteItems(placeOrderTransaction);
            System.out.println("Transaction Successful");

        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    protected <C extends ChildEntity<T>> Collection<C> getAllChildByType(Class<T> rootType, String id, Class<C> childType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveRootEntityBrnIfNotExist(RootEntity e) {
        String rn = resourceHelper.getFQBrn(e.getBRN());
        String prefix = rn.substring(rn.lastIndexOf("/"));
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(prefix));
        item.put(this.rangeKey, new AttributeValue(rn));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));

        Put createEntity = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + this.rangeKey + ")");
        txContext.get().add(new TransactWriteItem().withPut(createEntity));
    }

    @Override
    protected void saveRootEntityTrnIfNotExist(RootEntity e) {
        String trn = resourceHelper.getFQBrn(e.getTRN());
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(trn));
        item.put(this.rangeKey, new AttributeValue(trn));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));

        Put createEntity = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + this.partitionKey + ")");

        txContext.get().add(new TransactWriteItem().withPut(createEntity));
    }

    @Override
    protected void saveRootEntityBrnIfExist(long lastSeq, RootEntity e) {
        String rn = resourceHelper.getFQBrn(e.getBRN());
        String prefix = rn.substring(rn.lastIndexOf("/"));
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(prefix));
        item.put(this.rangeKey, new AttributeValue(rn));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":lastSeq", new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));
        expressionAttributeValues.put(":checkLastSeq", new AttributeValue().withN(String.valueOf(lastSeq)));
        expressionAttributeValues.put(":json", new AttributeValue(GsonCodec.encode(e)));

        Update updateEntity = new Update()
                .withTableName(this.tableName)
                .withUpdateExpression("SET json = :json , lastSeq = :lastSeq")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("lastSeq = :checkLastSeq and attribute_exists(" + this.rangeKey + ")")
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);
        txContext.get().add(new TransactWriteItem().withUpdate(updateEntity));
    }

    @Override
    protected void saveRootEntityTrnIfExist(long lastSeq, RootEntity e) {
        String trn = resourceHelper.getFQBrn(e.getTRN());
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(trn));
        item.put(this.rangeKey, new AttributeValue(trn));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":lastSeq", new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));
        expressionAttributeValues.put(":checkLastSeq", new AttributeValue().withN(String.valueOf(lastSeq)));
        expressionAttributeValues.put(":json", new AttributeValue(GsonCodec.encode(e)));

        Update updateEntity = new Update()
                .withTableName(this.tableName)
                .withUpdateExpression("SET json = :json , lastSeq = :lastSeq")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withConditionExpression("lastSeq = :checkLastSeq and attribute_exists(" + this.rangeKey + ")")
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);
        txContext.get().add(new TransactWriteItem().withUpdate(updateEntity));
    }

    @Override
    protected void saveChildEntityBrnIfNotExist(String rootTrn, ChildEntity e) {
        String partitionKey = resourceHelper.getFQTrn(rootTrn);
        String rangeKey = e.getBRN();
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(partitionKey));
        item.put(this.rangeKey, new AttributeValue(rangeKey));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));

        Put createEntity = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + this.rangeKey + ")");
        txContext.get().add(new TransactWriteItem().withPut(createEntity));
    }

    @Override
    protected void saveChildEntityTrnIfNotExist(String rootTrn, ChildEntity e) {
        String partitionKey = resourceHelper.getFQTrn(rootTrn);
        String rangeKey = e.getTRN();
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(partitionKey));
        item.put(this.rangeKey, new AttributeValue(rangeKey));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));

        Put createEntity = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + this.rangeKey + ")");
        txContext.get().add(new TransactWriteItem().withPut(createEntity));
    }

    @Override
    protected void saveChildEntityBrnIfExist(long lastSeq, String rootTrn, ChildEntity e) {
        String partitionKey = resourceHelper.getFQTrn(rootTrn);
        String rangeKey = e.getBRN();
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(partitionKey));
        item.put(this.rangeKey, new AttributeValue(rangeKey));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":lastSeq", new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));
        expressionAttributeValues.put(":checkLastSeq", new AttributeValue().withN(String.valueOf(lastSeq)));
        expressionAttributeValues.put(":json", new AttributeValue(GsonCodec.encode(e)));

        Update updateEntity = new Update()
                .withTableName(this.tableName)
                .withUpdateExpression("SET json = :json , lastSeq = :lastSeq")
                .withConditionExpression("lastSeq = :checkLastSeq and attribute_exists(" + this.rangeKey + ")")
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);

        txContext.get().add(new TransactWriteItem().withUpdate(updateEntity));
    }

    @Override
    protected void saveChildEntityTrnIfExist(long lastSeq, String rootTrn, ChildEntity e) {
        String partitionKey = resourceHelper.getFQTrn(rootTrn);
        String rangeKey = e.getTRN();
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(partitionKey));
        item.put(this.rangeKey, new AttributeValue(rangeKey));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":lastSeq", new AttributeValue().withN(String.valueOf(e.getMeta().getLastSeq())));
        expressionAttributeValues.put(":checkLastSeq", new AttributeValue().withN(String.valueOf(lastSeq)));
        expressionAttributeValues.put(":json", new AttributeValue(GsonCodec.encode(e)));

        Update updateEntity = new Update()
                .withTableName(this.tableName)
                .withUpdateExpression("SET json = :json , lastSeq = :lastSeq")
                .withConditionExpression("lastSeq = :checkLastSeq and attribute_exists(" + this.rangeKey + ")")
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);

        txContext.get().add(new TransactWriteItem().withUpdate(updateEntity));
    }

    @Override
    protected void deleteRootEntityBrnById(Class<T> rootType, String id, String tenantId) {
        String rn = resourceHelper.getFQBrn(RootEntity.makeRN(rootType, version, id, tenantId));
        String prefix = rn.substring(rn.lastIndexOf("/"));
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(prefix));
        item.put(this.rangeKey, new AttributeValue(rn));
        Delete delete = new Delete()
                .withTableName(tableName)
                .withKey(item);
        txContext.get().add(new TransactWriteItem().withDelete(delete));
    }

    @Override
    protected void deleteRootEntityTrnById(Class<T> rootType, String id, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityBrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        String partitionKey = resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, id, tenantId));
        String rangeKey = resourceHelper.getFQBrn(ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId));
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(partitionKey));
        item.put(this.rangeKey, new AttributeValue(rangeKey));
        Delete delete = new Delete()
                .withTableName(tableName)
                .withKey(item);
        txContext.get().add(new TransactWriteItem().withDelete(delete));
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityTrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected EntityCheckpoint getCheckpoint(String rootTrn) {

        String partitionKey = resourceHelper.getFQTrn(rootTrn);
        Map<String, String> expressionAttributesNames = new HashMap<>();
        expressionAttributesNames.put("#partitionKey", "partitionKey");
        expressionAttributesNames.put("#rangeKey", "rangeKey");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":partitionKey", new AttributeValue().withS(partitionKey));
        expressionAttributeValues.put(":rangeKey", new AttributeValue().withS(EntityCheckpoint.class.getSimpleName()));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(this.tableName)
                .withAttributesToGet("json")
                .withKeyConditionExpression("#partitionKey = :partitionKey AND #rangeKey = :rangeKey")
                .withExpressionAttributeNames(expressionAttributesNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        QueryResult queryResult = client.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items.size() > 0) {
            return GsonCodec.decode(EntityCheckpoint.class, items.get(0).get("json").getS());
        } else {
            return new EntityCheckpoint(rootTrn).setSeq(0);
        }
    }

    @Override
    protected void addEvent(Event event) {
        String trn = resourceHelper.getFQBrn(event.getRootEntityTRN() + "/events");
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(trn));
        item.put(this.rangeKey, new AttributeValue(String.format("%019d", event.getSeqNum())));
        item.put(this.json, new AttributeValue(GsonCodec.encode(event)));
        item.put(this.lastSeq, new AttributeValue().withN(String.valueOf(event.getSeqNum())));
        item.put(this.eventType, new AttributeValue(event.getClass().getSimpleName()));

        Put createEvent = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withConditionExpression("attribute_not_exists(" + this.rangeKey + ")")
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD);
        txContext.get().add(new TransactWriteItem().withPut(createEvent));
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

    @Override
    public ResultSet<T> getAllByRootType(Class<T> rootType, String tenantId, Query.PagingRequest paging) {
        String partitionKey = getRootBrn(rootType, tenantId);
        Map<String, String> expressionAttributesNames = new HashMap<>();
        expressionAttributesNames.put("#partitionKey", "partitionKey");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":partitionKey", new AttributeValue().withS(partitionKey));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(this.tableName)
                .withAttributesToGet("json")
                .withKeyConditionExpression("#partitionKey = :partitionKey")
                .withExpressionAttributeNames(expressionAttributesNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        QueryResult queryResult = client.query(queryRequest);
        List items = queryResult.getItems().stream().map(attr -> GsonCodec.decode(rootType, attr.get("json").getS())).collect(Collectors.toList());
        return EventRepoUtil.onPageable(items, paging);
    }

    @Override
    public Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {
        String pKey;
        String rKey;
        if (EntityIdHelper.isTechnicalId(id)) {
            pKey = resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, id, tenantId));
            rKey = pKey;
        } else {
            pKey = getRootBrn(rootType, tenantId);
            rKey = resourceHelper.getFQBrn(RootEntity.makeRN(rootType, version, id, tenantId));
        }

        Map<String, String> expressionAttributesNames = new HashMap<>();
        expressionAttributesNames.put("#partitionKey", "partitionKey");
        expressionAttributesNames.put("#rangeKey", "rangeKey");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":partitionKey", new AttributeValue().withS(pKey));
        expressionAttributeValues.put(":rangeKey", new AttributeValue().withS(rKey));

        QueryRequest queryRequest = new QueryRequest()
                .withTableName(this.tableName)
                .withAttributesToGet("json")
                .withKeyConditionExpression("#partitionKey = :partitionKey AND #rangeKey = :rangeKey")
                .withExpressionAttributeNames(expressionAttributesNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        QueryResult queryResult = client.query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items.size() > 0) {
            return Optional.of(GsonCodec.decode(rootType, items.get(0).get("json").getS()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <C extends ChildEntity<T>> Optional<C> getChildById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        String pKey = resourceHelper.getFQTrn(RootEntity.makeTRN(rootType, version, id, tenantId));
        String rKey;
        if(EntityIdHelper.isTechnicalId(childId))
        {
            rKey = ChildEntity.makeTRN(rootType, version, id, childType, childId, tenantId);
        }
        else
        {
            rKey = ChildEntity.makeRN(rootType, version, id, childType, childId, tenantId);
        }
    }

    @Override
    public <C extends ChildEntity<T>> ResultSet<C> getAllChildByType(Class<T> rootType, String id, Class<C> childType, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    protected void updateCheckpoint(long lastSeq, EntityCheckpoint checkpoint) {
        String partitionKey = resourceHelper.getFQTrn(rootTrn);
        String rangeKey = resourceHelper.getFQBrn(rootTrn);
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(partitionKey));
        item.put(this.rangeKey, new AttributeValue(rangeKey));
        item.put(this.json, new AttributeValue(GsonCodec.encode(rootTrn)));
        Put createEntity = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
    }

}
