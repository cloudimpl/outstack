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

import com.cloudimpl.outstack.runtime.domain.PolicyStatementEntity;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class PolicyStatementValidator {
    public static PlatformGrantedAuthority processPolicyStatements(String action,String rootType,PlatformAuthenticationToken token)
    {
        PlatformGrantedAuthority grant =  token.getAuthorities().stream().map(g->PlatformGrantedAuthority.class.cast(g)).findAny().orElseThrow(()->new PlatformAuthenticationException("no grant found to authenticate", null));
        Optional<PolicyStatementEntity> denyStmt = grant.getDenyStatmentByResourceName(rootType);
        if(denyStmt.isPresent())
        {
            denyStmt.get().getActions().stream().filter(a->a.isActionMatched(action))
                    .findAny().ifPresent(a->{
                        throw new PlatformAuthenticationException(null,"action {0} is denied from policy statement {1}",action,a);
                    });
            denyStmt.get().getResources().stream().filter(a->a.isResourceMatched(rootType)) .findAny().ifPresent(a->{
                        throw new PlatformAuthenticationException(null,"resource {0} is denied from policy statement {1}",action,a);
                    });
        }
        Optional<PolicyStatementEntity> allowStmt  = grant.getAllowStatmentByResourceName(rootType);
        if(allowStmt.isPresent())
        {
            allowStmt.get().getActions().stream().filter(a->a.isActionMatched(action))
                    .findAny().orElseThrow(()-> new PlatformAuthenticationException(null,"action {0} not define in the policy statement",action));
             allowStmt.get().getResources().stream().filter(a->a.isResourceMatched(rootType))
                    .findAny().orElseThrow(()-> new PlatformAuthenticationException(null,"resource {0} not define in the policy statement",rootType));
        }else
        {
            throw new PlatformAuthenticationException(null,"resource {0} access not allowed for action {1}",rootType,action);
        }
        return grant;
    }
    
}
