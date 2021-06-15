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

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.domain.Policy;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementRef;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementRefCreated;
import com.cloudimpl.outstack.runtime.domain.PolicyStatementRefRequest;
import com.cloudimpl.outstack.runtime.iam.PolicyValidationError;

/**
 *
 * @author nuwan
 */
public class CreatePolicyStatementRef extends EntityCommandHandler<PolicyStatementRef, PolicyStatementRefRequest, PolicyStatementRef>{

    @Override
    protected PolicyStatementRef execute(EntityContext<PolicyStatementRef> context, PolicyStatementRefRequest command) {
        PolicyStatement stmt = context.getEntityQueryProvider(PolicyStatement.class).getRoot(command.getPolicyStmtName()).orElseThrow(()->new PolicyValidationError("policy statement "+command.getPolicyStmtName()+" not found"));
        return context.<Policy,PolicyStatementRef>asChildContext().create(EntityIdHelper.idToRefId(stmt.id()),new PolicyStatementRefCreated(command.getPolicyName(),EntityIdHelper.idToRefId(stmt.id())));
    }

   
    
}
