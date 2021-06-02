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

import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.security.AuthenticationProvider;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import com.cloudimpl.outstack.spring.security.UserLoginRequest;
import com.cloudimpl.outstack.spring.security.UserLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        return Mono.just(authentication).doOnNext(a->a.setAuthenticated(true))
                .filter(token->token.getSystemToken() instanceof UsernamePasswordAuthenticationToken)
                .flatMap(token->onUsernamePasswordAuthentication((UsernamePasswordAuthenticationToken)token.getSystemToken()))
                .doOnNext(r->authentication.setAuthenticated(true)).map(r->authentication);
    }
    
    private Mono<UserLoginResponse> onUsernamePasswordAuthentication(UsernamePasswordAuthenticationToken token)
    {
        CommandWrapper wrap = CommandWrapper.builder().withCommand("LoginUser")
                .withId((String)token.getPrincipal())
                .withVersion("v1")
                .withRootId((String)token.getPrincipal())
                .withObject(UserLoginRequest.builder()
                .withUserId((String)token.getPrincipal()).withPassword((String)token.getCredentials()).build()).build();
        
        return cluster.requestReply("cloudimpl/example/v1/UserService",wrap);
    }
}
