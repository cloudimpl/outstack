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
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationException;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import com.cloudimpl.outstack.spring.security.UserLoginRequest;
import com.cloudimpl.outstack.spring.security.UserLoginResponse;
import java.util.function.Function;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class AuthenticationUtil {

    public static Mono<PlatformAuthenticationToken> loginUser(PlatformAuthenticationToken token,Function<UserLoginRequest,Mono<UserLoginResponse>> loginProvider) {
        Authentication auth = token.getSystemToken();
        if (auth == null) {
            return Mono.error(() -> new PlatformAuthenticationException("system token unavailable", null));
        }
        if (UsernamePasswordAuthenticationToken.class.isInstance(auth)) {
            return onUsernamePasswordAuthentication((UsernamePasswordAuthenticationToken) auth,loginProvider)
                    .map(resp->token)
                    .doOnNext(t->t.setAuthenticated(true));
        } else {
            return Mono.error(new PlatformAuthenticationException("invalid authentication medium", null));
        }
    }

    public static Mono<UserLoginResponse> onUsernamePasswordAuthentication(UsernamePasswordAuthenticationToken token,Function<UserLoginRequest,Mono<UserLoginResponse>> loginProvider) {
//        CommandWrapper wrap = CommandWrapper.builder().withCommand("LoginUser")
//                .withId((String)token.getPrincipal())
//                .withVersion("v1")
//                .withRootId((String)token.getPrincipal())
//                .withObject().build();
        UserLoginRequest userLoginReq = UserLoginRequest.builder()
                .withUserId((String) token.getPrincipal()).withPassword((String) token.getCredentials())
                .withVersion("v1")
                .withId((String) token.getPrincipal())
                .withRootId((String) token.getPrincipal())
                .withCommandName("LoginUser").build();
        return loginProvider.apply(userLoginReq);
    }
    
    
}
