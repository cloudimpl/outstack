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

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.Put;
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
import com.cloudimpl.outstack.spring.component.SpringApplicationConfigManager.Provider;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class DynamodbEventRepository<T extends RootEntity> extends EventRepositoy<T> {

    private final Provider.ProviderConfigs configs;
    private final DynamoDB dynamodb;
    private  String entityTable;
    private  String eventTable;
    public static final String partitionKey = "partitionKey";
    public static final String rangeKey = "rangeKey";
    private final int partitionCount;
    public DynamodbEventRepository(DynamoDB dynamodb, Class<T> rootType, ResourceHelper resourceHelper, EventStream eventStream,Provider.ProviderConfigs configs) {
        super(rootType, resourceHelper, eventStream);
        this.dynamodb = dynamodb;
        //this.loadTables();
        this.configs = configs;
        this.partitionCount = Integer.valueOf(configs.getOption("partitionCount").get());
    }

    @Override
    public void saveTx(EntityContextProvider.Transaction transaction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends Entity> T applyEvent(Event event) {
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
    public <K extends ChildEntity<T>> Optional<K> getChildById(Class<T> rootType, String id, Class<K> childType, String childId, String tenantId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <K extends ChildEntity<T>> ResultSet<K> getAllChildByType(Class<T> rootType, String id, Class<K> childType, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResultSet<Event<T>> getEventsByRootId(Class<T> rootType, String rootId, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <K extends ChildEntity<T>> ResultSet<Event<K>> getEventsByChildId(Class<T> rootType, String id, Class<K> childType, String childId, String tenantId, Query.PagingRequest paging) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    private void loadTables()
//    {
//        Optional<String> entityTable  = configs.getOption(rootType.getSimpleName()+"EntityTable",configs.getOption("defaultEntityTable").get());
//        System.out.println("dynamodb repository : "+rootType.getSimpleName() + " pick "+entityTable.get()+" dynamodb Table for ");
//        return table.get();
//    }
//    
//    private Put putEvent(Event event)
//    {
//        Put putEvent = new Put().withTableName(table.getTableName())
//                .
//    }
}
