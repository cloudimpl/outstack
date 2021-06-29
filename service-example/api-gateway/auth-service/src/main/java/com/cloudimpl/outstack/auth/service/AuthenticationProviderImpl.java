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

import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.security.AuthenticationProvider;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
public class AuthenticationProviderImpl implements AuthenticationProvider{

    @Autowired
    private Cluster cluster;
    
    @Override
    public Mono<PlatformAuthenticationToken> authenticate(PlatformAuthenticationToken authentication) {
        return AuthenticationUtil.loginUser(authentication,req->cluster.requestReply(null,"cloudimpl/example/v1/UserService", req));
    }
   
}
