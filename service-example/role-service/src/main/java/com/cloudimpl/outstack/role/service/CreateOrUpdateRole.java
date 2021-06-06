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
package com.cloudimpl.outstack.role.service;

import com.cloudimpl.outstack.runtime.AsyncEntityCommandHandler;
import com.cloudimpl.outstack.runtime.AsyncEntityContext;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.domain.Policy;
import com.cloudimpl.outstack.runtime.domain.PolicyRef;
import com.cloudimpl.outstack.runtime.domain.PolicyRefCreated;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import com.cloudimpl.outstack.runtime.domain.Role;
import com.cloudimpl.outstack.runtime.domain.RoleCreateRequest;
import com.cloudimpl.outstack.runtime.domain.RoleCreated;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;
import java.util.Collection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class CreateOrUpdateRole extends AsyncEntityCommandHandler<Role, RoleCreateRequest, Role>{

    @Override
    protected Mono<Role> execute(EntityContext<Role> context, RoleCreateRequest command) {
        AsyncEntityContext<Role> asyncContext = context.asAsyncEntityContext();
        Role role = asyncContext.getEntity().orElseGet(()->asyncContext.create(command.getRoleName(),new RoleCreated(command.getRoleName())));
        Collection<PolicyRef> existRefs  = asyncContext.<PolicyRef>getAllChildEntitiesByType(PolicyRef.class);
        
        Collection<PolicyRef> newRefs = command.getPolicyRef();
        return Flux.fromIterable(newRefs).flatMap(ref->validateAndCreatePolicyRef(asyncContext,role, ref,existRefs))
                .thenMany(Flux.fromIterable(existRefs).doOnNext(r->asyncContext.delete(r.id())))
                .then(Mono.just(role));
        
    }

    
    private void createOrUpdatePolicy(AsyncEntityContext<Role> asyncContext,Role role,Policy policy,PolicyRef ref)
    {
        asyncContext.create(PolicyRef.class,role.id(),EntityIdHelper.idToRefId(policy.id()), new PolicyRefCreated(ref.getDomainOwner(), ref.getDomainContext(),ref.getVersion(), role.getRoleName(), EntityIdHelper.idToRefId(policy.id())));
    }
    
    private Mono<Policy> validateAndCreatePolicyRef(AsyncEntityContext<Role> context,Role role,PolicyRef ref,Collection<PolicyRef> existRefs)
    {
        return context.<Policy>sendRequest(ref.getDomainOwner(), ref.getDomainContext(), ref.getVersion(), "PolicyService", QueryByIdRequest.builder().withId(ref.getPolicyRef()).build())
                .doOnNext(p->createOrUpdatePolicy(context, role, p, ref)).doOnNext(p->existRefs.removeIf(r->r.getPolicyRef().equals(ref.getPolicyRef())));
    }
  
}
