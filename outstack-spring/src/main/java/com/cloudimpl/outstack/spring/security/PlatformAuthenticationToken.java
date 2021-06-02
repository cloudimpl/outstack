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

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 *
 * @author nuwan
 */
public class PlatformAuthenticationToken extends AbstractAuthenticationToken {

    public enum TokenFlow {
        AUTHENTICATION_FLOW,
        TOKEN_FLOW,
        AUTHORIZATION_FLOW
    }
    
    public static final String TOKEN_CONTEXT_HEADER_NAME = "X-TokenContext";
    
    private final String principal;
    private final UserDetail userDetail;
    private final AuthenticationMeta authMeta;
    private Jwt jwtToken;
    private Authentication _systemToken;
    private Object response;
    public PlatformAuthenticationToken(AuthenticationMeta authMeta,String principal,Collection<PlatformGrantedAuthority> authorities,UserDetail userDetail) {
        super(authorities);
        this.principal = principal;
        this.userDetail = userDetail;
        this.authMeta = authMeta;
    }

    public String getUserId() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public AuthenticationMeta getAuthMeta() {
        return authMeta;
    }

    public Jwt getJwtToken() {
        return jwtToken;
    }

    public PlatformAuthenticationToken setJwtToken(Jwt jwtToken) {
        this.jwtToken = jwtToken;
        return this;
    }

    public PlatformAuthenticationToken setResponse(Object response) {
        this.response = response;
        return this;
    }

    public Object getResponse() {
        return response;
    }
    
    
    public Authentication getSystemToken()
    {
        return _systemToken;
    }

    public void setSystemToken(Authentication _systemToken) {
        this._systemToken = _systemToken;
    }
    
}
