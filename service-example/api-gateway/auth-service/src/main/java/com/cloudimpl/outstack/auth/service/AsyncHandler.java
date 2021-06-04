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
package com.cloudimpl.outstack.auth.service;

import com.cloudimpl.outstack.domain.example.User;
import com.cloudimpl.outstack.domain.example.command.UserCreateReq;
import com.cloudimpl.outstack.runtime.AsyncEntityCommandHandler;
import com.cloudimpl.outstack.runtime.CommandResponse;
import com.cloudimpl.outstack.runtime.EntityContext;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class AsyncHandler extends AsyncEntityCommandHandler<Test, TestRequest, CommandResponse> {

    @Override
    protected Mono<CommandResponse> execute(EntityContext<Test> context, TestRequest command) {
        return context.asAsyncEntityContext().<User>sendRequest("cloudimpl", "example", "v1", "UserService", UserCreateReq.builder().withUsername("nuwan")
                .withPassword("1234").withCommandName("CreateUser").withVersion("v1").build()).map(r -> new CommandResponse(r.entityId()));
    }

}
