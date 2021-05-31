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
package com.cloudimpl.outstack.spring.security.login;

import com.cloudimpl.outstack.spring.security.service.AuthenticationHelperService;
import com.cloudimpl.outstack.spring.security.service.AuthorizeRequest;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@RestController
@RequestMapping("/")
public class AuthController {

    @Autowired
    private AuthenticationHelperService authService;
    
    @GetMapping(value = "authorize")
    private Mono<Void> auth(@RequestParam("response_type") String responseType, @RequestParam("client_id") String clientId, @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "scope", required = false) String scope, @RequestParam(name = "state") String state, @RequestParam(name = "code_challenge", required = false) String codeChallenge,
            @RequestParam(name = "code_challenge_method", required = false) String codeChallengeMethod, @RequestParam(name = "audience", required = false) String audience,
            @RequestParam(name = "access_type",required = false) String accessType,ServerHttpResponse response) {
        return authService.authorize(new AuthorizeRequest(responseType, clientId, redirectUri, scope, state, codeChallenge, codeChallengeMethod, audience))
                .flatMap(resp->redirect(response, "/login?id="+resp.getReqKey()));
    }

    
    
    private Mono<Void> redirect(ServerHttpResponse resp,String redirectUri)
    {
        resp.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        resp.getHeaders().setLocation(URI.create(redirectUri));
        return resp.setComplete();
    }
}
