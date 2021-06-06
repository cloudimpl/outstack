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
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author nuwan
 */
public class PlatformGrantedAuthority implements GrantedAuthority{

    private final Map<String,PolicyStatement> denyPolicyStmts;
    private final Map<String,PolicyStatement> allowPolicyStmts;

    public PlatformGrantedAuthority(Map<String, PolicyStatement> denyPolicyStmts, Map<String, PolicyStatement> allowPolicyStmts) {
        this.denyPolicyStmts = denyPolicyStmts;
        this.allowPolicyStmts = allowPolicyStmts;
    }

   
    
    public Optional<PolicyStatement> getDenyStatmentByResourceName(String resourceName)
    {
        
        return Optional.ofNullable(denyPolicyStmts.get("*")).or(()->Optional.ofNullable(denyPolicyStmts.get(resourceName)));
    }
    
    public Optional<PolicyStatement> getAllowStatmentByResourceName(String resourceName)
    {
        return Optional.ofNullable(allowPolicyStmts.get("*")).or(()->Optional.ofNullable(allowPolicyStmts.get(resourceName)));
    }
    
    @Override
    public String getAuthority() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
