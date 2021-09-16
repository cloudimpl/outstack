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

import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import com.cloudimpl.outstack.runtime.domainspec.AuthInput;
import com.cloudimpl.outstack.runtime.iam.ActionDescriptor;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author nuwan
 */
@Component
@Slf4j
public class PolicyStatementValidator {

    public  PlatformGrantedAuthority processPolicyStatementsForCommand(AuthInput input, PlatformAuthenticationToken token) {
        PlatformGrantedAuthority grant = token.getAuthorities().stream().map(g -> PlatformGrantedAuthority.class.cast(g)).findAny().orElseThrow(() -> new PlatformAuthenticationException("no grant found to authenticate", null));
        Optional<List<PolicyStatement>> denyStmts = grant.getDenyStatmentByResourceName(input.getRootType(),input.getDomainOwner(),input.getDomainContext());

        if (denyStmts.isPresent()) {
            denyStmts.get().stream().map(denyStmt -> {
                denyStmt.getCmdActions().stream().filter(a -> a.isActionMatched(input.getAction()))
                        .findAny().ifPresent(a -> {
                            throw new PolicyEvaluationException("command action {0} is denied from policy statement {1}", AuthInput.verbose(input), a);
                        });
                return denyStmt;
            }).forEachOrdered(denyStmt -> {
                denyStmt.getResources().stream().filter(a -> a.isResourceMatched(input, token.getJwtToken().getClaims())).findAny().ifPresent(a -> {
                    throw new PolicyEvaluationException("command resource {0} is denied from policy statement {1}", AuthInput.verbose(input), a);
                });
            });

        }
        Optional<List<PolicyStatement>> allowStmts = grant.getAllowStatmentByResourceName(input.getRootType());
        if (allowStmts.isPresent()) {
            for (PolicyStatement allowStmt : allowStmts.get()) {
                ActionDescriptor actionDesc = allowStmt.getCmdActions().stream().filter(a -> a.isActionMatched(input.getAction()))
                        .findAny().orElse(null);
                if (actionDesc == null) {
                    log.info("command action false -> {} -> {}", AuthInput.verbose(input), allowStmt);
                    continue;
                }
                log.info("command action true -> {} -> {}", AuthInput.verbose(input), allowStmt);
                //.orElseThrow(() -> new PlatformAuthenticationException(null, "command action {0} not define in the policy statement", input.getAction()));
                ResourceDescriptor resourceDesc = allowStmt.getResources().stream().filter(a -> a.isResourceMatched(input, token.getJwtToken().getClaims()))
                        .findAny().orElse(null);
                if (resourceDesc == null) {
                    log.info("command resource false -> {} -> {}", AuthInput.verbose(input), allowStmt);
                    continue;
                }
                log.info("command resource true -> {} -> {}", AuthInput.verbose(input), allowStmt);
                return grant;
                //.orElseThrow(() -> new PlatformAuthenticationException(null, "command resource {0} not define in the policy statement", input.getRootType()));
            }
            throw new PolicyEvaluationException("command resource {0} access not allowed for action {1} , {2}", input.getRootType(), input.getAction(), AuthInput.verbose(input));
        } else {
            throw new PolicyEvaluationException("command resource {0} access not allowed for action {1} , {2}", input.getRootType(), input.getAction(), AuthInput.verbose(input));
        }
        //  return grant;
    }

    public  PlatformGrantedAuthority processPolicyStatementsForQuery(AuthInput input, PlatformAuthenticationToken token) {
        PlatformGrantedAuthority grant = token.getAuthorities().stream().map(g -> PlatformGrantedAuthority.class.cast(g)).findAny().orElseThrow(() -> new PlatformAuthenticationException("no grant found to authenticate", null));
        Optional<List<PolicyStatement>> denyStmts = grant.getDenyStatmentByResourceName(input.getRootType(),input.getDomainOwner(),input.getDomainContext());
        if (denyStmts.isPresent()) {
            denyStmts.get().stream().map(denyStmt -> {
                denyStmt.getQueryActions().stream().filter(a -> a.isActionMatched(input.getAction()))
                        .findAny().ifPresent(a -> {
                            throw new PolicyEvaluationException("query action {0} is denied from policy statement {1}", AuthInput.verbose(input), a);
                        });
                return denyStmt;
            }).forEachOrdered(denyStmt -> {
                denyStmt.getResources().stream().filter(a -> a.isResourceMatched(input, token.getJwtToken().getClaims())).findAny().ifPresent(a -> {
                    throw new PolicyEvaluationException("query resource {0} is denied from policy statement {1}", AuthInput.verbose(input), a);
                });
            });

        }
        Optional<List<PolicyStatement>> allowStmts = grant.getAllowStatmentByResourceName(input.getRootType());
        if (allowStmts.isPresent()) {
            for (PolicyStatement allowStmt : allowStmts.get()) {
                ActionDescriptor actionDesc = allowStmt.getQueryActions().stream().filter(a -> a.isActionMatched(input.getAction()))
                        .findAny().orElse(null);
                if (actionDesc == null) {
                    log.info("query action false -> {} -> {}", AuthInput.verbose(input), allowStmt);
                    continue;
                }
                log.info("query action true -> {} -> {}", AuthInput.verbose(input), allowStmt);
                //.orElseThrow(() -> new PlatformAuthenticationException(null, "query action {0} not define in the policy statement", input.getAction()));
                ResourceDescriptor resourceDesc = allowStmt.getResources().stream().filter(a -> a.isResourceMatched(input, token.getJwtToken().getClaims()))
                        .findAny().orElse(null);
                if (resourceDesc == null) {
                    log.info("query resource false -> {} -> {}", AuthInput.verbose(input), allowStmt);
                    continue;
                }
                log.info("query resource true -> {} -> {}", AuthInput.verbose(input), allowStmt);
                return grant;
                //.orElseThrow(() -> new PlatformAuthenticationException(null, "query resource {0} not define in the policy statement", input.getRootType()));
            }
            throw new PolicyEvaluationException("query resource {0} access not allowed for action {1} , {2}", input.getRootType(), input.getAction(), AuthInput.verbose(input));
        } else {
            throw new PolicyEvaluationException("query resource {0} access not allowed for action {1}", input.getRootType(), input.getAction());
        }
    //    return grant;
    }

}
