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
public class AuthorizeRequest {

    private String responseType;
    private String clientId;
    private String redirectUri;
    private String scope;
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String audience;

    public AuthorizeRequest(String response_type, String client_id, String redirect_uri, String scope, String state, String code_challenge, String code_challenge_method, String audience) {
        this.responseType = response_type;
        this.clientId = client_id;
        this.redirectUri = redirect_uri;
        this.scope = scope;
        this.state = state;
        this.codeChallenge = code_challenge;
        this.codeChallengeMethod = code_challenge_method;
        this.audience = audience;
    }


    public String getScope() {
        return scope;
    }

    public String getState() {
        return state;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

   

    public String getAudience() {
        return audience;
    }

    
}
