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

import com.cloudimpl.outstack.common.Pair;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author nuwan
 */
public class PlatformGrantedAuthority implements GrantedAuthority{

    private final Map<String,List<PolicyStatement>> denyPolicyStmts;
    private final Map<String,List<PolicyStatement>> allowPolicyStmts;

    public PlatformGrantedAuthority(Collection<PolicyStatement> denyPolicyStmts, Collection<PolicyStatement> allowPolicyStmts) {
        this.denyPolicyStmts = denyPolicyStmts.stream().flatMap(s->s.getResources().stream()
                .map(r->new Pair<>(r.getRootType(),s)))
                .collect(Collectors.groupingBy(s->s.getKey(), Collectors.mapping(s->s.getValue(), Collectors.toList())));
        this.allowPolicyStmts = allowPolicyStmts.stream().flatMap(s->s.getResources().stream()
                .map(r->new Pair<>(r.getRootType(),s)))
                .collect(Collectors.groupingBy(s->s.getKey(), Collectors.mapping(s->s.getValue(), Collectors.toList())));
    }

    public Optional<List<PolicyStatement>> getDenyStatmentByResourceName(String resourceName,String domainOwner,String domainContext)
    {     
        return Optional.ofNullable(denyPolicyStmts.get("*")).or(()->Optional.ofNullable(denyPolicyStmts.get(resourceName))).map(l->l.stream().filter(s->validate(s, domainOwner, domainContext)).collect(Collectors.toList()));
    }
    

    private boolean validate(PolicyStatement stmt,String domainOwner,String domainContext)
    {
        if(stmt.getDomainOwner().equals("*") && stmt.getDomainContext().equals("*"))
        {
            return true;
        }
        
        if(stmt.getDomainOwner().equals("*") && stmt.getDomainContext().equalsIgnoreCase(domainContext))
        {
            return true;
        }
        
        if(stmt.getDomainContext().equals("*") && stmt.getDomainOwner().equalsIgnoreCase(domainOwner))
        {
            return true;
        }
        
        return stmt.getDomainOwner().equalsIgnoreCase(domainOwner) && stmt.getDomainContext().equalsIgnoreCase(domainContext);
    }
    
    public Optional<List<PolicyStatement>> getAllowStatmentByResourceName(String resourceName)
    {
        return Optional.ofNullable(allowPolicyStmts.get("*")).or(()->Optional.ofNullable(allowPolicyStmts.get(resourceName)));
    }
    
    @Override
    public String getAuthority() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
