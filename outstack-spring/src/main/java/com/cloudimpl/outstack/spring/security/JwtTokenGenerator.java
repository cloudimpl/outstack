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
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author nuwan
 */
@Component
public class JwtTokenGenerator {

    @Value("${outstack.oauth.token.issuer:http://www.cloudimpl.com}")
    private String tokenIssuer;

    @Value("${outstack.oauth.token.access_token_lifetime:60}")
    private long access_token_lifetime;

    @Value("${outstack.oauth.token.refresh_token_lifetime:180}")
    private long refresh_token_lifetime;

    @Value("${outstack.apiContext}")
    private String apiContext;
    
    private final JWSSigner signer;

    public static JwtTokenGenerator instance;
    public JwtTokenGenerator(@Autowired(required = false) JwtKeyProvider keyProvider) {
        if (keyProvider != null) {
            signer = new RSASSASigner(keyProvider.getPrivateKey());
        } else {
            signer = null;
        }
        JwtTokenGenerator.instance = this;
    }

    public JwtToken createAccessToken(JwtTokenBuilder builder) {
        builder.withIssuer(tokenIssuer);
        builder.withExpireTime(access_token_lifetime);
        return builder.build();
    }

    public JwtToken createRefreshToken(JwtTokenBuilder builder) {
        builder.withIssuer(tokenIssuer);
        builder.withExpireTime(refresh_token_lifetime);
        return builder.build();
    }

    public JwtToken createOneTimeToken(JwtOneTimeTokenBuilder builder,long expireTime){
        builder.withClaim("@apiContext",apiContext);
        builder.withIssuer(tokenIssuer);
        builder.withExpireTime(expireTime);
        return builder.build();
    }
    
    public static JwtTokenGenerator instance()
    {
        return JwtTokenGenerator.instance;
    }
    
    public String serializeToken(JwtToken token) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), token.getJwt());
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException ex) {
            throw new PlatformAuthenticationException(ex.getMessage(), ex);
        }
    }

    public TokenResponse createTokenResponse(JwtToken accessToken, JwtToken refreshToken) {

        return new TokenResponse(serializeToken(accessToken), refreshToken == null ? null : serializeToken(refreshToken), "bearer", accessToken.getExpireTimeInSeconds());
    }

}
