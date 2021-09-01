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

import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.RootEntityContext;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementCreated;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementRequest;
import com.cloudimpl.outstack.runtime.iam.PolicyStatemetParser;
import com.cloudimpl.outstack.runtime.iam.ResourceDescriptor;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author nuwan
 */
public class CreatePolicyStatement extends EntityCommandHandler<PolicyStatement,PolicyStatementRequest,PolicyStatement>{

    @Inject
    private ResourceHelper helper;

    public CreatePolicyStatement() {
    }
    
    @Override
    protected PolicyStatement execute(EntityContext<PolicyStatement> context, PolicyStatementRequest command) {
        
        PolicyStatementCreated stmt = parseStatement(command);
        PolicyStatemetParser.validate(helper,(RootEntityContext<PolicyStatement>) context, stmt);
        return context.create(stmt.getSid(), stmt);
    }
    
    private static PolicyStatementCreated parseStatement(PolicyStatementRequest req)
    {
        return PolicyStatemetParser.parseStatement(req);
    }
    
    private static void checkResourceConstrains(Collection<ResourceDescriptor> resources)
    {
        
        if(resources.stream().collect(Collectors.groupingBy(r->r.getTenantScope())).keySet().size() > 1)
        {
            
        }
    }
    
    
}
