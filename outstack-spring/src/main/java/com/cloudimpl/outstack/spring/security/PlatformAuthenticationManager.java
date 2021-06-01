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
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public abstract class PlatformAuthenticationManager implements ReactiveAuthenticationManager {

    private AuthenticationProvider authenticationProvider;
    
    private AuthorizationProvider authorizationProvider;
    
    private TokenProvider tokenProvider;

    public PlatformAuthenticationManager(@Autowired(required = false) AuthenticationProvider authenticationProvider,@Autowired(required = false) AuthorizationProvider authorizationProvider,@Autowired(required = false) TokenProvider tokenProvider) {
        this.authenticationProvider = authenticationProvider == null ? t->Mono.error(()->new RuntimeException("AuthenticationProvider not provisioned")): authenticationProvider;
        this.authorizationProvider = authorizationProvider;
        this.tokenProvider = tokenProvider;
    }

    
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return convertToPlatformToken(authentication).flatMap(this::onPlatformToken);
    }

    private Mono<Authentication> onPlatformToken(PlatformAuthenticationToken token) {
        AuthenticationMeta meta = token.getAuthMeta();
        if (meta == null) {
            throw new PlatformAuthenticationException("no meta data found in the platform token", null);
        }
        switch (meta.getTokenFlow()) {
            case AUTHENTICATION_FLOW: {
                return authenticationProvider.authenticate(token);
            }
            case AUTHORIZATION_FLOW: {
                return authorizationProvider.authenticate(token);
            }
            case TOKEN_FLOW: {
                return tokenProvider.authenticate(token);
            }
            default: {
                return Mono.error(() -> new PlatformAuthenticationException("unknown token flow:" + meta.getTokenFlow(), null));
            }
        }
    }

    protected abstract Mono<PlatformAuthenticationToken> convertToPlatformToken(Authentication autentication);

    protected void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    protected void setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        this.authorizationProvider = authorizationProvider;
    }

    protected void setTokenProvider(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

}
