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

import com.cloudimpl.outstack.spring.security.Movie;
import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Controller
public class AuthController {

    @GetMapping(value = "authorize")
    private Mono<String> auth(final Model model, @RequestParam("response_type") String responseType, @RequestParam("client_id") String clientId, @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "scope", required = false) String scope, @RequestParam(name = "state") String state, @RequestParam(name = "code_challenge", required = false) String codeChallenge,
            @RequestParam(name = "code_challenge_method", required = false) String codeChallengeMethod, @RequestParam(name = "audience", required = false) String audience, @RequestParam("access_type") String accessType) {
        //  return doAuth()
        //  IReactiveDataDriverContextVariable reactiveDataDrivenMode =
        //       new ReactiveDataDriverContextVariable(Mono.just(), 1);
        model.addAttribute("auth", new AuthorizeRequest(responseType, clientId, redirectUri, scope, state, codeChallenge, codeChallengeMethod, audience));
//        model.addAttribute("client_id", clientId);
//        model.addAttribute("redirect_uri", redirectUri);
        model.addAttribute("scope", scope);
//        model.addAttribute("state", state);
//        model.addAttribute("code_challenge", codeChallenge);
//        model.addAttribute("code_challenge_method", codeChallengeMethod);
//        model.addAttribute("audience", audience);

        return Mono.just("loginForm");
    }

     @GetMapping(value = "authorize")
    private Mono<String> authLogin(final Model model, @RequestParam("response_type") String responseType, @RequestParam("client_id") String clientId, @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "scope", required = false) String scope, @RequestParam(name = "state") String state, @RequestParam(name = "code_challenge", required = false) String codeChallenge,
            @RequestParam(name = "code_challenge_method", required = false) String codeChallengeMethod, @RequestParam(name = "audience", required = false) String audience, @RequestParam("access_type") String accessType) {
        //  return doAuth()
        //  IReactiveDataDriverContextVariable reactiveDataDrivenMode =
        //       new ReactiveDataDriverContextVariable(Mono.just(), 1);
        model.addAttribute("auth", new AuthorizeRequest(responseType, clientId, redirectUri, scope, state, codeChallenge, codeChallengeMethod, audience));
//        model.addAttribute("client_id", clientId);
//        model.addAttribute("redirect_uri", redirectUri);
        model.addAttribute("scope", scope);
//        model.addAttribute("state", state);
//        model.addAttribute("code_challenge", codeChallenge);
//        model.addAttribute("code_challenge_method", codeChallengeMethod);
//        model.addAttribute("audience", audience);

        return Mono.just("loginForm");
    }

}
