///*
// * Copyright 2021 nuwan.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.cloudimpl.outstack.spring.repo;
//
//import com.cloudimpl.outstack.common.GsonCodec;
//import com.cloudimpl.outstack.core.ComponentProvider;
//import com.cloudimpl.outstack.runtime.EntityContextProvider;
//import com.cloudimpl.outstack.runtime.EventRepositoy;
//import com.cloudimpl.outstack.runtime.ResourceHelper;
//import com.cloudimpl.outstack.runtime.ResultSet;
//import com.cloudimpl.outstack.runtime.domain.PolicyRef;
//import com.cloudimpl.outstack.runtime.domain.PolicyRefCreated;
//import com.cloudimpl.outstack.runtime.domain.Role;
//import com.cloudimpl.outstack.runtime.domain.RoleCreated;
//import com.cloudimpl.outstack.runtime.domainspec.Event;
//import com.cloudimpl.outstack.runtime.domainspec.Query;
//import com.cloudimpl.outstack.runtime.repo.SimpleTransaction;
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// *
// * @author nuwan
// */
//public class DynamodbRepositoryFactoryTest {
//
//    static DynamodbRepositoryFactory fac;
//
//    public DynamodbRepositoryFactoryTest() {
//    }
//
//    @BeforeAll
//    public static void setUpClass() {
//        fac = new DynamodbRepositoryFactory(new ResourceHelper("test", "example", "example"), new ComponentProvider.ProviderConfigs() {
//            @Override
//            public Optional<String> getOption(String option, String defaultVal) {
//                if (option.equals("endpoint")) {
//                    return Optional.of("http://dynamodb.us-east-1.amazonaws.com");
//                } else if (option.equals("region")) {
//                    return Optional.of("us-east-1");
//                } else if (option.equals("RoleTable")) {
//                    return Optional.of("Role");
//                } else {
//                    return Optional.empty();
//                }
//            }
//        });
//
//    }
//
//    public DynamodbRepositoryFactory getFac() {
//        if (fac == null) {
//            fac = new DynamodbRepositoryFactory(new ResourceHelper("test", "example", "example"), new ComponentProvider.ProviderConfigs() {
//                @Override
//                public Optional<String> getOption(String option, String defaultVal) {
//                    if (option.equals("endpoint")) {
//                        return Optional.of("http://dynamodb.us-east-1.amazonaws.com");
//                    } else if (option.equals("region")) {
//                        return Optional.of("us-east-1");
//                    } else if (option.equals("RoleTable")) {
//                        return Optional.of("Role");
//                    } else {
//                        return Optional.empty();
//                    }
//                }
//            });
//        }
//        return fac;
//    }
//
//    @AfterAll
//    public static void tearDownClass() {
//    }
//
//    @BeforeEach
//    public void setUp() {
//        System.out.println("setup");
//    }
//
//    @AfterEach
//    public void tearDown() {
//    }
//
//    /**
//     * Test of createOrGetRepository method, of class DynamodbRepositoryFactory.
//     */
//    @Test
//    public void testCreateOrGetRepository() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//
//        SimpleTransaction<Role> tx = new SimpleTransaction<>(Role.class, repo);
//        RoleCreated created1 = new RoleCreated("test");
//        created1.setAction(Event.Action.CREATE);
//        created1.setId("id-" + UUID.randomUUID().toString());
//        created1.setRootId(created1.id());
//        tx.apply(created1);
//
//        repo.saveTx(tx);
//
//        tx = new SimpleTransaction<>(Role.class, repo);
//        RoleCreated created2 = new RoleCreated("test2");
//        created2.setAction(Event.Action.CREATE);
//        created2.setId("id-" + UUID.randomUUID().toString());
//        created2.setRootId(created2.id());
//        tx.apply(created2);
//
//        repo.saveTx(tx);
//
//        tx = new SimpleTransaction<>(Role.class, repo);
//        PolicyRefCreated refCreated = new PolicyRefCreated("test", "example", "v1", "test", "ref1");
//        refCreated.setAction(Event.Action.CREATE);
//        refCreated.setId("id-" + UUID.randomUUID().toString());
//        refCreated.setRootId(created1.id());
//        tx.apply(refCreated);
//        repo.saveTx(tx);
//        
//        tx = new SimpleTransaction<>(Role.class, repo);
//        refCreated = new PolicyRefCreated("test", "example", "v1", "test", "ref2");
//        refCreated.setAction(Event.Action.CREATE);
//        refCreated.setId("id-" + UUID.randomUUID().toString());
//        refCreated.setRootId(created1.id());
//        tx.apply(refCreated);
//        repo.saveTx(tx);
//    }
//
//    @Test
//    public void testGetRootEntityIdById() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//        Role role = repo.getRootById(Role.class, "test", null).get();
//        System.out.println("role: " + GsonCodec.encode(role));
//    }
//
//    @Test
//    public void testGetChildEntityIdById() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//        PolicyRef policyRef = repo.getChildById(Role.class, "id-6284c1cf-5620-4c0c-b5c8-c457b3c3a324", PolicyRef.class, "ref1", null).get();
//        System.out.println("policyRef: " + GsonCodec.encode(policyRef));
//    }
//
//    @Test
//    public void testGetRootEvents() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//        ResultSet<Event<Role>> rs = repo.getEventsByRootId(Role.class, "id-6284c1cf-5620-4c0c-b5c8-c457b3c3a324", null, Query.PagingRequest.EMPTY);
//        rs.getItems().forEach(r -> System.out.println(GsonCodec.encode(r)));
//    }
//
//    @Test
//    public void testGetChildEvents() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//        ResultSet<Event<PolicyRef>> rs = repo.getEventsByChildId(Role.class, "id-6284c1cf-5620-4c0c-b5c8-c457b3c3a324", PolicyRef.class, "ref1", null, Query.PagingRequest.EMPTY);
//        rs.getItems().forEach(r -> System.out.println(GsonCodec.encode(r)));
//    }
//
//    @Test
//    public void testAllByRootType() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//        ResultSet<Role> rs = repo.getAllByRootType(Role.class, null, Query.PagingRequest.EMPTY);
//        rs.getItems().forEach(r -> System.out.println(GsonCodec.encode(r)));
//    }
//    
//    @Test
//    public void testAllChildByType() {
//        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
//        ResultSet<PolicyRef> rs = repo.getAllChildByType(Role.class,"id-6284c1cf-5620-4c0c-b5c8-c457b3c3a324",PolicyRef.class, null, Query.PagingRequest.EMPTY);
//        rs.getItems().forEach(r -> System.out.println(GsonCodec.encode(r)));
//    }
//}
