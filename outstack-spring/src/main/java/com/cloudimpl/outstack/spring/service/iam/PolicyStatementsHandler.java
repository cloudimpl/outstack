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
package com.cloudimpl.outstack.spring.service.iam;

import com.cloudimpl.outstack.runtime.AsyncEntityCommandHandler;
import com.cloudimpl.outstack.runtime.AsyncRootEntityQueryContext;
import com.cloudimpl.outstack.runtime.AyncEntityContext;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.iam.PolicyStatementCreated;
import com.cloudimpl.outstack.runtime.iam.PolicyStatementDescriptor;
import com.cloudimpl.outstack.runtime.iam.PolicyStatementGroup;
import com.cloudimpl.outstack.runtime.iam.PolicyStatementRequest;
import com.cloudimpl.outstack.runtime.iam.PolicyStatemetParser;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class PolicyStatementsHandler extends AsyncEntityCommandHandler<PolicyStatementGroup,PolicyStatementRequest,PolicyStatementDescriptor>{

    @Override
    protected Mono<PolicyStatementDescriptor> execute(EntityContext<PolicyStatementGroup> context, PolicyStatementRequest command) {
        
        PolicyStatementCreated stmt = parseStatement(command.getRootType(), command);
       // stmt.
        AyncEntityContext<PolicyStatementGroup> async =  context.asAsyncEntityContext();
        
        Optional<PolicyStatementGroup> stmtGroup = context.<PolicyStatementGroup>asAsyncEntityContext().getEntity();
        if(stmtGroup.isEmpty())
        {
            
           // async.create(command.getRootType(), event)
        }
        return null;
    }
    
    private static PolicyStatementCreated parseStatement(String rootType,PolicyStatementRequest req)
    {
        return PolicyStatemetParser.parseStatement(rootType, req);
    }
    
    private static void checkResourceConstrains(Collection<ResourceDescriptor> resources)
    {
        
        if(resources.stream().collect(Collectors.groupingBy(r->r.getTenantScope())).keySet().size() > 1)
        {
            
        }
    }
}
