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
package com.cloudimpl.outstack.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
public class AuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<ServerWebExchange> {

    private BearerTokenAuthenticationManager bearerTokenAuthentication;

    private BasicTokenAuthenticationManager basicTokenAuthentication;

    public AuthenticationManagerResolver(ReactiveJwtDecoder jwtDecoder) {
        bearerTokenAuthentication = new BearerTokenAuthenticationManager(jwtDecoder);
        basicTokenAuthentication = new BasicTokenAuthenticationManager();
    }

//    @Autowired
//    RSAPublicKey publicKey;
    @Override
    public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange c) {
        c.getRequest().getHeaders().forEach((k, l) -> System.out.println(k + ":" + l));
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String authorization = c.getRequest().getHeaders().getOrEmpty("Authorization").stream().findFirst().orElseThrow(() -> new PlatformAuthenticationException("Authorization not found", null));
        if (authorization.toLowerCase().startsWith("bearer")) {
            return Mono.just(bearerTokenAuthentication);
        } else if (authorization.toLowerCase().startsWith("basic")) {
            return Mono.just(basicTokenAuthentication);
        }
//            return Mono.just(new BearerTokenAuthenticationManager(new NimbusReactiveJwtDecoder(publicKey)));
//        }
        return Mono.error(() -> new PlatformAuthenticationException("unknown auth type:" + authorization, null));

    }
}
