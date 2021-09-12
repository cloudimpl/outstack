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

import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementCreated;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.iam.ActionDescriptor;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import com.cloudimpl.outstack.spring.security.AuthorizationProvider;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import com.cloudimpl.outstack.spring.security.PlatformGrantedAuthority;
import java.util.Collections;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@Component
public class AuthorizationProviderImpl implements AuthorizationProvider {

    private final PlatformGrantedAuthority adminAuth;
    private PlatformAuthenticationToken token;

    public AuthorizationProviderImpl() {
        PolicyStatement adminPolicy = new PolicyStatement("fullAdminAccess", null);
        EntityHelper.applyEvent(adminPolicy,new PolicyStatementCreated("fullAdminAccess","*","*", PolicyStatement.EffectType.DENY,
                Collections.singleton(new ActionDescriptor("*", ActionDescriptor.ActionScope.ALL)),Collections.singleton(new ActionDescriptor("*", ActionDescriptor.ActionScope.ALL)), Collections.singleton(ResourceDescriptor.builder().withResourceScope(ResourceDescriptor.ResourceScope.GLOBAL).build()),Collections.EMPTY_LIST));
        adminAuth = new PlatformGrantedAuthority(Collections.EMPTY_LIST, Collections.singletonList(adminPolicy));
    }

    @Override
    public Mono<PlatformAuthenticationToken> authenticate(PlatformAuthenticationToken authentication) {

        return Mono.just(authentication)
                .map(this::onAuth)
                .doOnNext(a -> a.setAuthenticated(true));
    }

    private PlatformAuthenticationToken onAuth(PlatformAuthenticationToken token) {
        if (this.token == null) {
            this.token = token.copy(Collections.singleton(adminAuth));
        }
        return this.token;
    }
}
