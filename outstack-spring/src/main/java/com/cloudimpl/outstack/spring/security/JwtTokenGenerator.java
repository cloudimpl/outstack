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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author nuwan
 */
@Component
public class JwtTokenGenerator {

    private final JWSSigner signer;

    public JwtTokenGenerator(@Autowired(required = false) JwtKeyProvider keyProvider) {
        if (keyProvider != null) {
            signer = new RSASSASigner(keyProvider.getPrivateKey());
        } else {
            signer = null;
        }
    }

    public String createToken(JWTClaimsSet jwtClaimsSet) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), jwtClaimsSet);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException ex) {
            throw new PlatformAuthenticationException(ex.getMessage(), ex);
        }
    }

    public TokenResponse createTokenResponse(JWTClaimsSet accessTokenJwt,JWTClaimsSet refershTokenJwt)
    {
        long millis = accessTokenJwt.getExpirationTime().getTime() - System.currentTimeMillis();
        
        return new TokenResponse(createToken(accessTokenJwt),createToken(refershTokenJwt),"bearer",(int)(millis/1000));
    }
    
    public String createOneTimeToken(JWTClaimsSet jwtClaimsSet) {
        try {
            //  new JWTClaimsSet.Builder(jwtClaimsSet).claim(name, signer)
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), jwtClaimsSet);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException ex) {
            throw new PlatformAuthenticationException(ex.getMessage(), ex);
        }
    }
}
