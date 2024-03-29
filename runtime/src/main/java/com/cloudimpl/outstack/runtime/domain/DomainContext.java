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
package com.cloudimpl.outstack.runtime.domain;

import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "DomainContexts",version = "v1")
public class DomainContext extends RootEntity {

    @Id
    private String domainId;
    private String domainOwner;
    private String domainContext;

    public DomainContext(String domainId) {
        this.domainId = domainId;
    }

    public String getDomainId() {
        return domainId;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getDomainContext() {
        return domainContext;
    }

    
    @Override
    public String entityId() {
        return domainId;
    }

    private void applyEvent(DomainContextCreated domainContextCreated) {
        this.domainContext = domainContextCreated.getDomainContext();
        this.domainOwner = domainContextCreated.getDomainOwner();
    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "DomainContextCreated": {
                applyEvent((DomainContextCreated) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "domainId";
    }

}
