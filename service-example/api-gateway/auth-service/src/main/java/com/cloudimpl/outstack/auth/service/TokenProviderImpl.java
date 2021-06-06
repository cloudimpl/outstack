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
package com.cloudimpl.outstack.auth.service;

import com.cloudimpl.outstack.spring.security.JwtTokenBuilder;
import com.cloudimpl.outstack.spring.security.JwtTokenGenerator;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationException;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import com.cloudimpl.outstack.spring.security.TokenProvider;
import com.cloudimpl.outstack.spring.security.TokenResponse;
import com.cloudimpl.outstack.spring.security.UserLoginRequest;
import com.cloudimpl.outstack.spring.security.UserLoginResponse;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
public class TokenProviderImpl implements TokenProvider {

    @Autowired
    JwtTokenGenerator tokenGen;

    @Value("${outstack.oauth.token.admin_user:admin}")
    private String username;

    @Value("${outstack.oauth.token.admin_password:1234}")
    private String password;

    @Value("${outstack.oauth.token.admin_user_id:1111}")
    private String userid;

    @Override
    public Mono<PlatformAuthenticationToken> authenticate(PlatformAuthenticationToken authentication) {
        return AuthenticationUtil.loginUser(authentication, req ->validateLogin(req))
                .doOnNext(t -> t.setAuthenticated(true))
                .map(t -> t.setResponse(issueToken(t)));
    }

    private Mono<UserLoginResponse> validateLogin(UserLoginRequest req) {
        if (req.getUserId() != null && req.getPassword() != null) {
            if (req.getUserId().equals(username) && req.getPassword().equals(password)) {
                return Mono.just(new UserLoginResponse(userid,"","", username, "test@gmail.com", false, true, Collections.EMPTY_LIST));
            }

        }
        throw new PlatformAuthenticationException("wrong username or password", null);
    }

    public TokenResponse issueToken(PlatformAuthenticationToken token) {
        String accessType = token.getAuthMeta().getClientMeta().getAccessType();
        boolean offline = false;
        if (accessType != null && accessType.toLowerCase().equals("offline")) {
            offline = true;
        }
        return tokenGen.createTokenResponse(tokenGen.createAccessToken(new JwtTokenBuilder(token)), offline ? tokenGen.createRefreshToken(new JwtTokenBuilder(token)) : null);
    }
}
