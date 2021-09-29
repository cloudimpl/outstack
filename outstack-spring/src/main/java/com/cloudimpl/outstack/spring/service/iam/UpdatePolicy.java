package com.cloudimpl.outstack.spring.service.iam;

import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domain.Policy;
import com.cloudimpl.outstack.runtime.domain.PolicyCreateRequest;
import com.cloudimpl.outstack.runtime.domain.PolicyUpdated;

public class UpdatePolicy extends EntityCommandHandler<Policy, PolicyCreateRequest, Policy> {

    @Inject
    private ResourceHelper helper;

    @Override
    protected Policy execute(EntityContext<Policy> context, PolicyCreateRequest command) {
        return context.update(command.getPolicyName(), new PolicyUpdated(command.getPolicyName(), command.getPolicyContext(), helper.getDomainOwner(), helper.getDomainContext(), helper.getApiContext(), command.getBoundary()));
    }

}
