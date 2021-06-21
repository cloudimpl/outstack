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

/**
 *
 * @author nuwan
 */
public class AuthenticationMeta {

    private final PlatformAuthenticationToken.TokenFlow tokenFlow;
    private final String authKey;
    private final String tokenContext;
    private final GrantType grantType;
    private final ClientMeta clientMeta;

    public AuthenticationMeta(PlatformAuthenticationToken.TokenFlow tokenFlow, String authKey, String tokenContext, GrantType grantType, ClientMeta clientMeta) {
        this.tokenFlow = tokenFlow;
        this.authKey = authKey;
        this.tokenContext = tokenContext;
        this.grantType = grantType;
        this.clientMeta = clientMeta;
    }

    public String getAuthKey() {
        return authKey;
    }

    public String getTokenContext() {
        return tokenContext;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public ClientMeta getClientMeta() {
        return clientMeta;
    }

    public PlatformAuthenticationToken.TokenFlow getTokenFlow() {
        return tokenFlow;
    }

    @Override
    public String toString() {
        return "AuthenticationMeta{" + "tokenFlow=" + tokenFlow + ", authKey=" + authKey + ", tokenContext=" + tokenContext + ", grantType=" + grantType + ", clientMeta=" + clientMeta + '}';
    }

    public static final class ClientMeta {

        private final String clientId;
        private final String clientSecret;
        private final String code;
        private final String redirectUri;
        private final String codeVerifier;
        private final String tenantId;
        private final String accessType;
        private final String userAgent;
        private final String remoteIp;
        private final String userData;

        public ClientMeta(String clientId, String clientSecret, String code, String redirectUri, String codeVerifier, String tenantId, String accessType,String userAgent,String remoteIp,String userData) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.code = code;
            this.redirectUri = redirectUri;
            this.codeVerifier = codeVerifier;
            this.tenantId = tenantId;
            this.accessType = accessType;
            this.userAgent= userAgent;
            this.remoteIp = remoteIp;
            this.userData = userData;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getCode() {
            return code;
        }

        public String getAccessType() {
            return accessType;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public String getCodeVerifier() {
            return codeVerifier;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getRemoteIp() {
            return remoteIp;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public String getUserData() { return userData;
        }

        @Override
        public String toString() {
            return "ClientMeta{" + "clientId=" + clientId + ", clientSecret=" + clientSecret + ", code=" + code + ", redirectUri=" + redirectUri + ", codeVerifier=" + codeVerifier + ", tenantId=" + tenantId + ", accessType=" + accessType + ", userData=" + userData+ '}';
        }

    }
}
