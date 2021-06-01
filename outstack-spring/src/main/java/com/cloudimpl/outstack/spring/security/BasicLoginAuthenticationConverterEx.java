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
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class BasicLoginAuthenticationConverterEx extends ServerHttpBasicAuthenticationConverter {

    private final String usernameParameter = "username";

    private final String passwordParameter = "password";

    private final String authorizationKeyParameter = "authKey";

    @Override
    public Mono<Authentication> apply(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String context = request.getHeaders().getFirst(PlatformAuthenticationToken.TOKEN_CONTEXT_HEADER_NAME);
        if (!StringUtils.startsWithIgnoreCase(authorization, "basic ")) {
            return Mono.empty();
        }
        String credentials = (authorization.length() <= BASIC.length()) ? ""
                : authorization.substring(BASIC.length(), authorization.length());
        String decoded = new String(base64Decode(credentials));
        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            return Mono.empty();
        }
        PlatformAuthenticationToken.TokenFlow flow = Auth2Util.createFlow(exchange);
        return Mono.just(new UsernamePasswordAuthenticationToken(parts[0], parts[1])).doOnNext(token->token.setDetails(new AuthenticationMeta(flow, null, context,Auth2Util.getGrantType(flow, exchange),Auth2Util.getClientMeta(exchange))))
                .cast(Authentication.class);
    }

    private byte[] base64Decode(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (Exception ex) {
            return new byte[0];
        }
    }
  
}
