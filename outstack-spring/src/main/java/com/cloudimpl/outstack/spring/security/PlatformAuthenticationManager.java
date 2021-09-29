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

import com.cloudimpl.outstack.runtime.ValidationErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public abstract class PlatformAuthenticationManager implements ReactiveAuthenticationManager {

    private final AuthenticationProvider authenticationProvider;
    
    private final AuthorizationProvider authorizationProvider;
    
    private final TokenProvider tokenProvider;

    public PlatformAuthenticationManager(@Autowired(required = false) AuthenticationProvider authenticationProvider,@Autowired(required = false) AuthorizationProvider authorizationProvider,@Autowired(required = false) TokenProvider tokenProvider) {
        this.authenticationProvider = authenticationProvider == null ? t->Mono.error(()->new RuntimeException("AuthenticationProvider not provisioned")): authenticationProvider;
        this.authorizationProvider = authorizationProvider == null ? t->Mono.error(()->new PlatformAuthenticationException("AuthorizationProvider not provisioned",null)): authorizationProvider;
        this.tokenProvider = tokenProvider == null ? t->Mono.error(()->new PlatformAuthenticationException("TokenProvider not provisioned",null)): tokenProvider;
    }
  
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return convertToPlatformToken(authentication).doOnNext(pt->pt.setSystemToken(authentication)).flatMap(this::onPlatformToken).cast(Authentication.class);
    }

    private Mono<PlatformAuthenticationToken> onPlatformToken(PlatformAuthenticationToken token) {
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
                return Mono.just(token).doOnNext(t->validateTokenFlow(t)).flatMap(t->tokenProvider.authenticate(t)).doOnNext(this::validateTokenResponse);
            }
            default: {
                return Mono.error(() -> new PlatformAuthenticationException("unknown token flow:" + meta.getTokenFlow(), null));
            }
        }
    }

    protected abstract Mono<PlatformAuthenticationToken> convertToPlatformToken(Authentication autentication);

    
    private void validateTokenResponse(PlatformAuthenticationToken token)
    {
        if(token.getResponse() == null || !(token.getResponse() instanceof TokenResponse))
        {
            throw new PlatformAuthenticationException("token response is null or invalid", null);
        }
    }
    
    private void validateTokenFlow(PlatformAuthenticationToken token)
    {
        switch(token.getAuthMeta().getGrantType())
        {
            case AUTHORIZATION_CODE:
            {
                break;
            }
            case PASSWORD:
            {
                if(!UsernamePasswordAuthenticationToken.class.isInstance(token.getSystemToken()))
                {
                    throw new ValidationErrorException("grant type does not match with token type");
                }
                break;
            }
            case REFRESH_TOKEN:
            {
                if(!BearerTokenAuthenticationToken.class.isInstance(token.getSystemToken()))
                {
                    throw new ValidationErrorException("grant type does not match with token type");
                }
                break;
            }
            case CUSTOM_TOKEN:
            {
                if(!BearerTokenAuthenticationToken.class.isInstance(token.getSystemToken()))
                {
                    throw new ValidationErrorException("grant type does not match with token type");
                }
                break;
            }
        }
    }
}
