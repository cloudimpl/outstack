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
package com.cloudimpl.outstack.spring.security.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationException;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import com.cloudimpl.outstack.spring.security.UserLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
public class AuthenticationHelperService {

    @Value("${outstack.api-gateway.authService.domainOwner:@Null}")
    private String domainOwner;

    @Value("${outstack.api-gateway.authService.domainContext:@Null}")
    private String domainContext;

    @Value("${outstack.api-gateway.authService.version:@Null}")
    private String version;

    @Value("${outstack.api-gateway.authService.serviceName:@Null}")
    private String serviceName;

    @Value("${outstack.api-gateway.authService.pkceEnable:false}")
    private boolean pkceEnable;

    @Autowired
    Cluster cluster;

    private String authService = null;

  //  private ResourceCache<AuthorizeRequest> requestCache = new ResourceCache<>(10000, Duration.ofMinutes(1));

//    public Mono<PlatformAuthenticationToken> login(PlatformAuthenticationToken token) {
//        if (domainOwner == null || domainContext == null || version == null || serviceName == null) {
//            return Mono.error(() -> new PlatformAuthenticationException("authentication service not found", null));
//        }
//        if (authService == null) {
//            authService = domainOwner + "/" + domainContext + "/" + version + "/" + serviceName;
//        }
//
//        CloudMessage userDetailReq = CloudMessage.builder().withData(UserLoginRequest.builder().withUserId(token.getUserId()).withTenantId(token.tenantId()).withId(token.getUserId()).withVersion(version)).build();
//        return cluster.requestReply(authService, userDetailReq).map(o -> UserLoginResponse.class.cast(o)).map(this::onAuth).onErrorMap(err -> new PlatformAuthenticationException("user login error", err));
//    }

    public Mono<AuthorizeResponse> authorize(AuthorizeRequest req) {
        if ((req.getCodeChallenge() == null || req.getCodeChallengeMethod() == null) && pkceEnable) {
            return Mono.error(new PlatformAuthenticationException("PKCE mandatory", null));
        }
        return cluster.requestReply("AuthenticationService", CloudMessage.builder()
                .withData(req).withKey(cluster.getServiceRegistry().findLocalByName("AuthenticationService").orElseThrow(() -> new PlatformAuthenticationException("auth service not found", null)).id()).build());
//        CloudService service = cluster.getServiceRegistry().findLocalByName("AuthenticationService").orElseThrow(() -> new PlatformAuthenticationException("auth service not found", null));
//        String authKey = UUID.randomUUID().toString() + "," + service.id();
//        requestCache.put(authKey, req);
//        return Mono.just(new AuthorizeResponse(authKey));
    }

    private PlatformAuthenticationToken onAuth(UserLoginResponse resp) {
        // resp.getStmts().str
        // UserDetail user = new UserDetail(resp.getUserId(), resp.isActive(), resp.isLocked(), resp.getStmts());
        return null;
    }

    public Mono<AuthorizeRequest> getAuthRequest(String key) {
        if (key == null) {
            return Mono.empty();
        }
        String[] keys = key.split(",");
        if(keys.length != 2)
        {
            return Mono.error(() -> new PlatformAuthenticationException("invalid key", null));
        }
        return cluster.requestReply("AuthenticationService", CloudMessage.builder().withData(new AuthorizeKeyRequest(key)).withKey(keys[1]).build());
    }
}
