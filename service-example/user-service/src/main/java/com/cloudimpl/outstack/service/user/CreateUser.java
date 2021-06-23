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
package com.cloudimpl.outstack.service.user;

import com.cloudimpl.outstack.domain.example.User;
import com.cloudimpl.outstack.domain.example.UserCreated;
import com.cloudimpl.outstack.domain.example.command.UserCreateReq;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.spring.security.JwtOneTimeTokenBuilder;
import com.cloudimpl.outstack.spring.security.JwtToken;
import com.cloudimpl.outstack.spring.security.JwtTokenGenerator;

/**
 *
 * @author nuwan
 */
public class CreateUser extends EntityCommandHandler<User, UserCreateReq, User>{

    @Override
    protected User execute(EntityContext<User> context, UserCreateReq command) {
        
        JwtOneTimeTokenBuilder builder = new JwtOneTimeTokenBuilder();
        builder.withAction("CreateUser")
                .withActionInput(new UserCreateReq.Builder().withUsername("auto").withPassword("1234").build())
                .withRootEntityType(User.class);
        JwtToken token = JwtTokenGenerator.instance().createOneTimeToken(builder, 10000);
        String str = JwtTokenGenerator.instance().serializeToken(token);
        System.out.println("token : "+str);
        String tenantId = context.getTenantId();
        if(tenantId != null)
        {
            User nonTenantUser = context.<User>asRootContext().asNonTenantContext(command.getUsername()).getEntity().orElseThrow(()->new DomainEventException(DomainEventException.ErrorCode.ENTITY_NOT_FOUND,"user {0} not found", command.getUsername()));
            return context.create(EntityIdHelper.idToRefId(nonTenantUser.id()), new UserCreated(EntityIdHelper.idToRefId(nonTenantUser.id()), command.getPassword()));
        }
        return context.create(command.getUsername(), new UserCreated(command.getUsername(), command.getPassword()));
    }
    
}
