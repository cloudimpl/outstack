/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.User;
import java.security.InvalidParameterException;

/**
 *
 * @author nuwansa
 */
public class CreateUser extends EntityCommandHandler<User, CreateUserRequest, String>{

    @Override
    public String apply(CreateUserRequest t) {
        String tenantId = t.getTenantId();
        if(tenantId == null)
        {
            throw new InvalidParameterException("tenantId is missing");
        }
        
    }
    
}
