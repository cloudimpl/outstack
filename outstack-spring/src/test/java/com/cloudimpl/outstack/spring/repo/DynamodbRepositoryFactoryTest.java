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

import com.cloudimpl.outstack.core.ComponentProvider;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domain.PolicyRef;
import com.cloudimpl.outstack.runtime.domain.PolicyRefCreated;
import com.cloudimpl.outstack.runtime.domain.Role;
import com.cloudimpl.outstack.runtime.domain.RoleCreated;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author nuwan
 */
public class DynamodbRepositoryFactoryTest {

    static DynamodbRepositoryFactory fac;

    public DynamodbRepositoryFactoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        fac = new DynamodbRepositoryFactory(new ResourceHelper("test", "example", "example"), new ComponentProvider.ProviderConfigs() {
            @Override
            public Optional<String> getOption(String option, String defaultVal) {
                if (option.equals("endpoint")) {
                    return Optional.of("http://dynamodb.us-east-1.amazonaws.com");
                } else if (option.equals("region")) {
                    return Optional.of("US-EAST-1");
                } else if (option.equals("RoleTable")) {
                    return Optional.of("Role");
                } else {
                    return Optional.empty();
                }
            }
        });

    }

    public DynamodbRepositoryFactory getFac() {
        if (fac == null) {
            fac = new DynamodbRepositoryFactory(new ResourceHelper("test", "example", "example"), new ComponentProvider.ProviderConfigs() {
                @Override
                public Optional<String> getOption(String option, String defaultVal) {
                    if (option.equals("endpoint")) {
                        return Optional.of("http://dynamodb.us-east-1.amazonaws.com");
                    } else if (option.equals("region")) {
                        return Optional.of("us-east-1");
                    } else if (option.equals("RoleTable")) {
                        return Optional.of("Role");
                    } else {
                        return Optional.empty();
                    }
                }
            });
        }
        return fac;
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        System.out.println("setup");
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of createOrGetRepository method, of class DynamodbRepositoryFactory.
     */
    @Test
    public void testCreateOrGetRepository() {
        EventRepositoy<Role> repo = getFac().createOrGetRepository(Role.class);
        
        List<Event> events = new LinkedList<>();
        
        RoleCreated created = new RoleCreated("test");
        created.setAction(Event.Action.CREATE);
        created.setId(UUID.randomUUID().toString());
        created.setRootId(created.id());
        events.add(created);
        
        PolicyRefCreated refCreated = new PolicyRefCreated("test", "example", "v1", "test", UUID.randomUUID().toString());
        refCreated.setAction(Event.Action.CREATE);
        refCreated.setId(UUID.randomUUID().toString());
        refCreated.setRootId(created.id());
        events.add(refCreated);
        repo.saveTx(events);
    }

}
