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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class JwtToken{
    private final Map<String,String> claims;
    private final Map<String,String> headers;

    public JwtToken(Builder builder) {
        this.claims = Collections.unmodifiableMap(builder.claims);
        this.headers = Collections.unmodifiableMap(builder.headers);
    }
    
    public Optional<String> getClaim(String claim)
    {
        return Optional.ofNullable(claims.get(claim));
    }
    
    public Optional<String> getHeader(String header)
    {
        return Optional.ofNullable(headers.get(header));
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder
    {
        private Map<String,String> claims = new HashMap<>();
        private Map<String,String> headers = new HashMap<>();
        
        public Builder withClaim(String claimName,String value)
        {
            this.claims.put(claimName, value);
            return this;
        }
        
        public Builder withHeader(String header,String value)
        {
            this.claims.put(header, value);
            return this;
        }
        
        public JwtToken build()
        {
            return new JwtToken(this);
        }
    }
    
}
