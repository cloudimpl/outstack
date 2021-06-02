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
import java.util.Date;

/**
 *
 * @author nuwan
 */
public class JwtToken {

    private JWTClaimsSet jwt;
    private long expireTimeInSeconds;
    public JwtToken(long expireTimeInSeconds,JWTClaimsSet jwt) {
        this.jwt = jwt;
        this.expireTimeInSeconds = expireTimeInSeconds;
    }

    public JWTClaimsSet getJwt() {
        return jwt;
    }

    public long getExpireTimeInSeconds() {
        return expireTimeInSeconds;
    }

    
    public Date getExpireTime()
    {
        return jwt.getExpirationTime();
    }
    
}
