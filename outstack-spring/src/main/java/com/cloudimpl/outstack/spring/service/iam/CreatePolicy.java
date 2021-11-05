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
import com.cloudimpl.outstack.runtime.domain.Policy;
import com.cloudimpl.outstack.runtime.domain.PolicyCreateRequest;
import com.cloudimpl.outstack.runtime.domain.PolicyCreated;

/**
 *
 * @author nuwan
 */
public class CreatePolicy extends EntityCommandHandler<Policy, PolicyCreateRequest, Policy> {

    @Inject
    private ResourceHelper helper;

    @Override
    protected Policy execute(EntityContext<Policy> context, PolicyCreateRequest command) {
        return context.create(command.getPolicyName(), new PolicyCreated(command.getPolicyName(), command.getPolicyContext(), helper.getDomainOwner(), helper.getDomainContext(), helper.getApiContext(), command.getBoundary(), command.getDependentPolicies()));
    }

}
