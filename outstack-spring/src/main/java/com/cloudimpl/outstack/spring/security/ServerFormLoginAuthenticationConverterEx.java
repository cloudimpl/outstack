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

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerFormLoginAuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author nuwan
 */
public class ServerFormLoginAuthenticationConverterEx extends ServerFormLoginAuthenticationConverter {

  private final String usernameParameter = "username";

  private final String passwordParameter = "password";

  private final String authorizationKeyParameter = "authKey";

  @Override
  public Mono<Authentication> apply(ServerWebExchange exchange) {
    if(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin") == null) {
      exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
      exchange.getResponse().getHeaders()
          .add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    }
    if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
      return Mono.empty();
    }
    return exchange.getFormData().flatMap((data) -> createAuthentication(exchange, data));
  }

  private Mono<UsernamePasswordAuthenticationToken> createAuthentication(ServerWebExchange exchange, MultiValueMap<String, String> data) {
    String context = exchange.getRequest().getHeaders().getFirst(PlatformAuthenticationToken.TOKEN_CONTEXT_HEADER_NAME);
    String username = data.getFirst(this.usernameParameter);
    String password = data.getFirst(this.passwordParameter);
    if (username == null || password == null) {
      return Mono.empty();
    }
    String authKey = data.getFirst(this.authorizationKeyParameter);
    AuthenticationMeta meta = Auth2Util.createAuthMeta(authKey, context, exchange);
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
    token.setDetails(meta);
    return Mono.just(token);
  }
}
