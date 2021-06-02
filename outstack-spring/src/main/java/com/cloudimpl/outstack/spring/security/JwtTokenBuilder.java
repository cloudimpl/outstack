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

import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author nuwan
 */
public class JwtTokenBuilder {
    private final PlatformAuthenticationToken token;
    private final JWTClaimsSet.Builder builder;
    
    public JwtTokenBuilder(PlatformAuthenticationToken token) {
        this.token = token;
        this.builder = new JWTClaimsSet.Builder();
        this.builder.issueTime(new Date(Instant.now().toEpochMilli()));
        this.builder.jwtID(UUID.randomUUID().toString());
        this.builder.subject(token.getUserId());
        this.builder.claim("userId", token.)
    }
    
    
    public JwtTokenBuilder withClaims(Map<String,Object> claims)
    {
        claims.entrySet().forEach(e->this.builder.claim(e.getKey(),e.getValue()));
        return this;
    }
    
    public JwtTokenBuilder withClaim(String key,String value)
    {
        this.builder.claim(key, value);
        return this;
    }
    
    public JwtTokenBuilder withNotBefore(Date date)
    {
        this.builder.notBeforeTime(date);
        return this;
    }
    
    public JwtTokenBuilder withExpireTime(long seconds)
    {
        this.builder.expirationTime(new Date(Instant.now().plusSeconds(1000000000).toEpochMilli()));
        return this;
    }
    
    public JWTClaimsSet build()
    {
        return this.builder.build();
    }
}
