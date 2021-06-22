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

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 *
 * @author nuwan
 */
public class Auth2Util {

    public static AuthenticationMeta createAuthMeta(String authKey,String context,ServerWebExchange exchange){
        PlatformAuthenticationToken.TokenFlow flow = Auth2Util.createFlow(exchange);
        return new AuthenticationMeta(flow, authKey,context,Auth2Util.getGrantType(flow, exchange),Auth2Util.getClientMeta(exchange));
    }
    
    public static PlatformAuthenticationToken.TokenFlow createFlow(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (path.equals("/login")) {
            return PlatformAuthenticationToken.TokenFlow.AUTHENTICATION_FLOW;
        } else if (path.equals("/token")) {
            return PlatformAuthenticationToken.TokenFlow.TOKEN_FLOW;
        } else {
            return PlatformAuthenticationToken.TokenFlow.AUTHORIZATION_FLOW;
        }
    }

    public static GrantType getGrantType(PlatformAuthenticationToken.TokenFlow tokenFlow, ServerWebExchange exchange) {
        if (tokenFlow == PlatformAuthenticationToken.TokenFlow.TOKEN_FLOW) {
            String type = exchange.getRequest().getQueryParams().getFirst("grant_type");
            return GrantType.from(type);
        }
        return null;
    }

    public static AuthenticationMeta.ClientMeta getClientMeta(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        String clientId = req.getQueryParams().getFirst("client_id");
        String clientSecret = req.getQueryParams().getFirst("client_secret");
        String codeVerfier = req.getQueryParams().getFirst("code_verifier");
        String code = req.getQueryParams().getFirst("code");
        String redirectUri = req.getQueryParams().getFirst("redirect_uri");
        String accessType = req.getQueryParams().getFirst("access_type");
        String tenantId = req.getHeaders().getFirst("X-TenantId");
        String userAgent = req.getHeaders().getFirst("User-Agent");
        String userData = req.getHeaders().getFirst("X-UserData");
        String remoteIp = req.getRemoteAddress().toString();
        return new AuthenticationMeta.ClientMeta(clientId, clientSecret, code, redirectUri, codeVerfier, tenantId,accessType,userAgent,remoteIp,userData);
    }

    public static void validateAuthentcationMeta(Object detail) {
        if (detail == null || !(detail instanceof AuthenticationMeta)) {
            throw new PlatformAuthenticationException("meta data not found in the token", null);
        }
    }
}
