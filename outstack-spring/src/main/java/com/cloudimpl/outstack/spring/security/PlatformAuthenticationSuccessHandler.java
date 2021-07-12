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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class PlatformAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private ObjectMapper mapper = new ObjectMapper(); //TODO remove this

    private URI location = URI.create("/hello");

    private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private ServerRequestCache requestCache = new WebSessionServerRequestCache();

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange wfe, Authentication a) {
        if (!(a instanceof PlatformAuthenticationToken)) {
            throw new PlatformAuthenticationException("invalid authentication token", null);
        }
        PlatformAuthenticationToken pToken = PlatformAuthenticationToken.class.cast(a);
        if (pToken.getResponse() == null) {
            return Mono.error(new RuntimeException("token response is empty"));
        }
        if (pToken.getResponse() instanceof TokenResponse) {
            try {
                Mono<DataBuffer> buffer = Mono.just(mapper.writeValueAsBytes(pToken.getResponse())).map(b -> wfe.getExchange().getResponse().bufferFactory().wrap(b));
                wfe.getExchange().getResponse().getHeaders().add("Content-Type", "application/json");
                wfe.getExchange().getResponse().setStatusCode(HttpStatus.OK);
                return wfe.getExchange().getResponse().writeWith(buffer);
                //  objectMapper.convertValue(reply, LinkedHashMap.class);
                //  wfe.getExchange().getResponse().writeWith(Mono.just(pToken.getResponse()));
            } catch (JsonProcessingException ex) {
                Logger.getLogger(PlatformAuthenticationSuccessHandler.class.getName()).log(Level.SEVERE, null, ex);
                return Mono.error(ex);
            }
        } else if (pToken.getResponse() instanceof RedirectionUrl) {
            RedirectionUrl url = RedirectionUrl.class.cast(pToken.getResponse());
            ServerWebExchange exchange = wfe.getExchange();
            return this.requestCache.getRedirectUri(exchange).defaultIfEmpty(URI.create(url.getUrl()))
                    .flatMap((location) -> this.redirectStrategy.sendRedirect(exchange, location));
        }
        else
        {
            return Mono.error(new RuntimeException("unhandled token response: "+pToken.getResponse()));
        }
    }

}
