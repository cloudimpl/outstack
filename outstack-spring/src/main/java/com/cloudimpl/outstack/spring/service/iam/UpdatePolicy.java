package com.cloudimpl.outstack.spring.service.iam;

import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import com.cloudimpl.outstack.runtime.domain.Policy;
import com.cloudimpl.outstack.runtime.domain.PolicyCreateRequest;
import com.cloudimpl.outstack.runtime.domain.PolicyUpdated;

public class UpdatePolicy extends EntityCommandHandler<Policy, PolicyCreateRequest, Policy> {

    @Inject
    private ResourceHelper helper;

    @Override
    protected Policy execute(EntityContext<Policy> context, PolicyCreateRequest command) {
        Policy entity = context.<Policy>asRootContext().getEntity().orElseThrow(()->  new ValidationErrorException("Policy does not exists"));
        if (!command.getPolicyName().equals(entity.getPolicyName())) {
            context.rename(entity.getPolicyName(), command.getPolicyName());
        }
        return context.update(entity.id(), new PolicyUpdated(command.getPolicyName(), command.getPolicyContext(), helper.getDomainOwner(), helper.getDomainContext(), helper.getApiContext(), command.getBoundary(), command.getDependentPolicies()));
    }

}
