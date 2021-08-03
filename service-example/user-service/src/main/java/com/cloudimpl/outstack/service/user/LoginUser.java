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
package com.cloudimpl.outstack.service.user;

import com.cloudimpl.outstack.domain.example.User;
import com.cloudimpl.outstack.domain.example.UserLoggedIn;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationException;
import com.cloudimpl.outstack.spring.security.UserLoginRequest;
import com.cloudimpl.outstack.spring.security.UserLoginResponse;
import java.util.Collections;

/**
 *
 * @author nuwan
 */
public class LoginUser extends EntityCommandHandler<User, UserLoginRequest, UserLoginResponse>{

    @Override
    protected UserLoginResponse execute(EntityContext<User> context, UserLoginRequest command) {
        User user = context.<User>asRootContext().getEntity().orElseThrow(()->new PlatformAuthenticationException("user not found",null)); 
        context.update(user.entityId(), new UserLoggedIn(command.getMapAttr().get("remoteIp"),
                command.getMapAttr().get("browserDetail"), command.getUserId()));
        return new UserLoginResponse(user.id(),"","",user.getUsername(),"test@test.com",false,true,Collections.EMPTY_LIST);
    }
    
}
