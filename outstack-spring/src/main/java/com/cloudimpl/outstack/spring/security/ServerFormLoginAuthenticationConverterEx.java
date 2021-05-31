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

import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import static org.springframework.security.web.server.ServerHttpBasicAuthenticationConverter.BASIC;
import org.springframework.security.web.server.authentication.ServerFormLoginAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerHttpBasicAuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class ServerFormLoginAuthenticationConverterEx extends ServerFormLoginAuthenticationConverter {

    private final String usernameParameter = "username";

    private final String passwordParameter = "password";

    private final String authorizationKeyParameter = "authKey";

    @Override
    public Mono<Authentication> apply(ServerWebExchange exchange) {
        return exchange.getFormData().map((data) -> createAuthentication(exchange,data));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(ServerWebExchange exchange,MultiValueMap<String, String> data) {
        String context = exchange.getRequest().getHeaders().getFirst(PlatformAuthenticationToken.TOKEN_CONTEXT_HEADER_NAME);
        String username = data.getFirst(this.usernameParameter);
        String password = data.getFirst(this.passwordParameter);
        String authKey = data.getFirst(this.authorizationKeyParameter);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        AuthenticationMeta meta = new AuthenticationMeta(createFlow(exchange), authKey,context);
        token.setDetails(meta);
        return token;
    }

    private PlatformAuthenticationToken.TokenFlow createFlow(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (path.equals("/login")) {
            return PlatformAuthenticationToken.TokenFlow.AUTHENTICATION_FLOW;
        } else if (path.equals("/token")) {
            return PlatformAuthenticationToken.TokenFlow.TOKEN_FLOW;
        } else {
            throw new PlatformAuthenticationException("unknow authenication endpoint", null);
        }
    }
}
