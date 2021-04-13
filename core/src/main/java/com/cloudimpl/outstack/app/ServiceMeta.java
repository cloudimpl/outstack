/*
 * Copyright 2020 nuwansa.
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

import com.cloudimpl.outstack.core.CloudException;
import com.cloudimpl.outstack.core.CloudRouter;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.routers.DynamicRouter;
import com.cloudimpl.outstack.routers.LeaderRouter;
import com.cloudimpl.outstack.routers.NodeIdRouter;
import com.cloudimpl.outstack.routers.RoundRobinRouter;
import com.cloudimpl.outstack.routers.ServiceIdRouter;


/**
 *
 * @author nuwansa
 */
public class ServiceMeta {

    private final CloudFunction func;
    private final Router router;
    private final Class<?> serviceType;
    
    public ServiceMeta(Class<?> serviceType, CloudFunction func, Router router) {
        this.serviceType = serviceType;
        this.func = func;
        this.router = router;
    }

    public CloudFunction getFunc() {
        return func;
    }

    public Router getRouter() {
        return router;
    }

    public Class<?> getServiceType() {
        return serviceType;
    }

    public Class<? extends CloudRouter> getRouterType() {
        switch (router.routerType()) {
            case ROUND_ROBIN:
                return RoundRobinRouter.class;
            case DYNAMIC:
                return DynamicRouter.class;
            case SERVICE_ID:
                return ServiceIdRouter.class;
            case NODE_ID:
                return NodeIdRouter.class;
            case LEADER:
                return LeaderRouter.class;
            default:
                throw new CloudException(router.routerType()+" not supported");
        }
    }

}
