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
    private final String tenantId;
    private final String context;
    private final UserDetail userDetail;
    private final TokenFlow tokenFlow;
    public PlatformAuthenticationToken(TokenFlow tokenFlow,String context, String principal, String tenantId, Collection<PlatformGrantedAuthority> authorities,UserDetail userDetail) {
        super(authorities);
        this.tokenFlow = tokenFlow;
        this.context = context;
        this.principal = principal;
        this.tenantId = tenantId;
        this.userDetail = userDetail;
    }

    public String getUserId() {
        return principal;
    }

    public String tenantId() {
        return tenantId;
    }

    public String context() {
        return context;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getContext() {
        return context;
    }

    public TokenFlow getTokenFlow() {
        return tokenFlow;
    }

    
}
