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

/**
 *
 * @author nuwan
 */
import com.cloudimpl.outstack.spring.security.service.AuthenticationHelperService;
import com.cloudimpl.outstack.spring.security.service.AuthorizeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
public class LoginFormController {

    @Autowired
    private AuthenticationHelperService authService;
    
    @GetMapping("/login")
    private Mono<String> auth(final Model model, @RequestParam(name = "id",required = false) String id) {
           return authService.getAuthRequest(id)
                   .map(req->id)
                .switchIfEmpty(Mono.just(""))
                .map(resp->model.addAttribute("authKey", resp)).map(m->"loginForm"); 
        
    }


}