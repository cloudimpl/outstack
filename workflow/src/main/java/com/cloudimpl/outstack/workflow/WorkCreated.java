/*
 * Copyright 2021 nuwansa.
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
package com.cloudimpl.outstack.workflow;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.workflow.domain.WorkEntity;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public class WorkCreated extends Event<WorkEntity>{

    private final String name;
    private final WorkEntity.Status status;
    private final List<WorkEntity.Param> params;

    public WorkCreated(String name, WorkEntity.Status status,List<WorkEntity.Param> params) {
        this.name = name;
        this.status = status;
        this.params = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public List<WorkEntity.Param> getParams() {
        return params;
    }

    public WorkEntity.Status getStatus() {
        return status;
    }
    
    @Override
    public Class<? extends Entity> getOwner() {
        return WorkEntity.class;
    }

    @Override
    public Class<? extends RootEntity> getRootOwner() {
         return WorkEntity.class;
    }

    @Override
    public String entityId() {
        return name;
    }

    @Override
    public String rootEntityId() {
         return name;
    }
    
}
