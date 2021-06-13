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
import com.amazonaws.services.dynamodbv2.model.Put;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValuesOnConditionCheckFailure;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.EntityCheckpoint;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.EventStream;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.RepositoryException;
import com.cloudimpl.outstack.spring.component.SpringApplicationConfigManager.Provider;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
    protected void saveRootEntityBrnIfNotExist(Entity e) {
        String rn = resourceHelper.getFQBrn(e.getBRN());
        String prefix = rn.substring(rn.lastIndexOf("/"));
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(prefix));
        item.put(this.rangeKey, new AttributeValue(rn));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));
        item.put(this.lastSeq, new AttributeValue().withN(""+e.getMeta().getLastSeq()));

        Put createProduct = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + this.rangeKey + ")");
        txContext.get().add(new TransactWriteItem().withPut(createProduct));
    }

    @Override
    protected void saveRootEntityTrnIfNotExist(Entity e) {
        String trn = resourceHelper.getFQBrn(e.getTRN());
        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put(this.partitionKey, new AttributeValue(trn));
        item.put(this.rangeKey, new AttributeValue(trn));
        item.put(this.json, new AttributeValue(GsonCodec.encode(e)));

        Put createProduct = new Put()
                .withTableName(this.tableName)
                .withItem(item)
                .withReturnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .withConditionExpression("attribute_not_exists(" + this.partitionKey + ")");
        
        txContext.get().add(new TransactWriteItem().withPut(createProduct));
    }

    @Override
    protected void saveRootEntityBrnIfExist(Entity e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveRootEntityTrnIfExist(Entity e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveChildEntityBrnIfNotExist(Entity e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveChildEntityTrnIfNotExist(Entity e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveChildEntityBrnIfExist(Entity e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void saveChildEntityTrnIfExist(Entity e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void deleteRootEntityBrnById(Class<T> rootType, String id, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void deleteRootEntityTrnById(Class<T> rootType, String id, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityBrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected <C extends ChildEntity<T>> void deleteChildEntityTrnById(Class<T> rootType, String id, Class<C> childType, String childId, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected EntityCheckpoint getCheckpoint(String rootTrn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void addEvent(Event event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet<T> getAllByRootType(Class<T> rootType, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<T> getRootById(Class<T> rootType, String id, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends ChildEntity<T>> Optional<T> getChildById(Class<T> rootType, String id, Class<T> childType, String childId, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends ChildEntity<T>> ResultSet<T> getAllChildByType(Class<T> rootType, String id, Class<T> childType, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet<Event<T>> getEventsByRootId(Class<T> rootType, String rootId, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends ChildEntity<T>> ResultSet<Event<T>> getEventsByChildId(Class<T> rootType, String id, Class<T> childType, String childId, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
