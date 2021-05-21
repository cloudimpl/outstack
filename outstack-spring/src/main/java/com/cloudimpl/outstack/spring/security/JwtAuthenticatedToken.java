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

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 *
 * @author nuwan
 */
public class JwtAuthenticatedToken extends AbstractAuthenticationToken{

    String token;
    public JwtAuthenticatedToken(JwtToken token,String credential) {
        super(Collections.EMPTY_SET);
        this.token = credential;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return new UserDetail("nuwan");
    }
    
}
