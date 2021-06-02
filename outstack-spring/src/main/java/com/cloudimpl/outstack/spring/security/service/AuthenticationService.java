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
package com.cloudimpl.outstack.spring.security.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.ServiceRegistryReadOnly;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.ResourceCache;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationException;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "AuthenticationService")
@Router(routerType = RouterType.SERVICE_ID)
public class AuthenticationService implements Function<CloudMessage,Mono<? extends Object>>{
    private final ResourceCache<AuthorizeRequest> requestCache;
    private final ServiceRegistryReadOnly serviceRegistry;
    public AuthenticationService(ServiceRegistryReadOnly serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.requestCache = new ResourceCache<>(10000, Duration.ofMinutes(1));
    }
    
    
    @Override
    public Mono<? extends Object> apply(CloudMessage t) {
        if(t.data() instanceof AuthorizeRequest)
        {
            return onAuthenticationRequest(t.data());
        }
        else if(t.data() instanceof AuthorizeKeyRequest)
        {
            return onAuthKeyRequest(t.data());
        }
        return Mono.empty();
    }

    private Mono<AuthorizeResponse> onAuthenticationRequest(AuthorizeRequest req) {
        CloudService service = serviceRegistry.findLocalByName("AuthenticationService").orElseThrow(() -> new PlatformAuthenticationException("auth service not found", null));
        String authKey = UUID.randomUUID().toString() + "," + service.id();
        requestCache.put(authKey, req);
        System.out.println("auth key generated"+authKey);
        return Mono.just(new AuthorizeResponse(authKey));
    }
    
    private Mono<AuthorizeResponse> onAuthKeyRequest(AuthorizeKeyRequest req){
        return Mono.justOrEmpty(requestCache.get(req.getKey()));
    }
}
