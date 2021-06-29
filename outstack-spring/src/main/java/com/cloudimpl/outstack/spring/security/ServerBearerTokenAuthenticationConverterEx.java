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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author nuwan
 */
public class ServerBearerTokenAuthenticationConverterEx extends ServerBearerTokenAuthenticationConverter {

  private final boolean tokenEndpoint;

  public ServerBearerTokenAuthenticationConverterEx(boolean tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    if(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin") == null) {
      exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
      exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
      exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    }
   
    if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
      return Mono.empty();
    }
    String context = exchange.getRequest().getHeaders().getFirst(PlatformAuthenticationToken.TOKEN_CONTEXT_HEADER_NAME);
    AuthenticationMeta meta = Auth2Util.createAuthMeta(null, context, exchange);
    if (tokenEndpoint && meta.getTokenFlow() != PlatformAuthenticationToken.TokenFlow.TOKEN_FLOW) {
      return Mono.empty();
    }
    return super.convert(exchange).cast(BearerTokenAuthenticationToken.class).doOnNext(token -> token.setDetails(meta)).cast(Authentication.class);
  }
}
