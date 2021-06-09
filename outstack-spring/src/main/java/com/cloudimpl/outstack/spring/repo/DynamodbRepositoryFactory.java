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

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.cloudimpl.outstack.core.ComponentProvider;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nuwan
 */
public class DynamodbRepositoryFactory implements EventRepositoryFactory {

    private final AmazonDynamoDB client;
    private final DynamoDB dynamoDB;
    private final ResourceHelper helper;
    private ComponentProvider.ProviderConfigs providerConfig;
    private final Map<Class<? extends RootEntity>, EventRepositoy<? extends RootEntity>> mapRepos = new ConcurrentHashMap<>();
    
    public DynamodbRepositoryFactory(ResourceHelper helper,ComponentProvider.ProviderConfigs providerConfig) {
        this.helper = helper;
        this.providerConfig = providerConfig;
        client = AmazonDynamoDBClientBuilder.standard()
               .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(providerConfig.getOption("endpoint").get(), providerConfig.getOption("region").get()))
            ///.withRegion(Regions.US_EAST_1)
            //.withCredentials(new ProfileCredentialsProvider("profile_test"))
            .build();
        this.dynamoDB = new DynamoDB(client);
    }

    @Override
    public <T extends RootEntity> EventRepositoy<T> createOrGetRepository(Class<T> rootType) {
        return (EventRepositoy<T>) mapRepos.computeIfAbsent(rootType, type -> new DynamodbEventRepository<>(this.dynamoDB,(Class<T>) type, this.helper, null,this.providerConfig));
    }
}
