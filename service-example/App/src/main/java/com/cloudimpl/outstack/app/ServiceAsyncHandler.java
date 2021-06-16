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
package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.OrganizationCreated;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.runtime.AsyncEntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class ServiceAsyncHandler extends AsyncEntityCommandHandler<Organization, OrganizationCreateRequest,Organization>{

    @Override
    protected Mono execute(EntityContext<Organization> context, OrganizationCreateRequest command) {
        return Mono.just(context.create(command.getOrgName(), new OrganizationCreated(command.getWebsite()+"xxxxxx", command.getOrgName())));
    }
    
}
