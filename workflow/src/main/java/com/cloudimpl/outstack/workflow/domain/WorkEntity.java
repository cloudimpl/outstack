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
package com.cloudimpl.outstack.workflow.domain;

import com.cloudimpl.outstack.runtime.configs.ConfigCreated;
import com.cloudimpl.outstack.runtime.configs.ConfigUpdated;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.workflow.WorkCreated;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwansa
 */
public class WorkEntity extends RootEntity{
    public enum Status
    {
        PENDING,
        COMPLETED,
        FAILED
    }
    private String name;
    private List<Param> params = new LinkedList<>();
    private Status status;

    public WorkEntity(String name) {
        this.name = name;
        this.status = Status.PENDING;
    }
    
    
    @Override
    public String entityId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void applyEvent(WorkCreated evt)
    {
       this.name = evt.getName();
       this.params = evt.getParams();
       this.status = evt.getStatus();
    }
    
    @Override
    protected void apply(Event event) {
         switch (event.getClass().getSimpleName()) {
            case "WorkCreated": {
                applyEvent((WorkCreated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }
    
    @Override
    public String idField() {
        return "name";
    }
    
    
    public static final class Param
    {
        private final Object item;
        private final String type;

        public Param(Object item, String type) {
            this.item = item;
            this.type = type;
        }

        public Object getItem() {
            return item;
        }

        public String getType() {
            return type;
        }
        
        
    }
}
