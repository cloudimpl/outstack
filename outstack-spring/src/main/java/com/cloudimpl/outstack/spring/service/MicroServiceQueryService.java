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
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.spring.component.SpringQueryService;
import com.cloudimpl.outstack.spring.domain.CommandHandlerEntity;
import com.cloudimpl.outstack.spring.domain.MicroService;
import com.cloudimpl.outstack.spring.domain.QueryHandlerEntity;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "MicroServiceQueryService")
@Router(routerType = RouterType.ROUND_ROBIN)
public class MicroServiceQueryService extends SpringQueryService<MicroService>{
    static
    {
        $$(MicroService.class);
        $$(CommandHandlerEntity.class);
        $$(QueryHandlerEntity.class);
    }
    
    public MicroServiceQueryService(EventRepositoryFactory factory) {
        super(factory);
    }
    
}
