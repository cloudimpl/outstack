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

import com.cloudimpl.outstack.runtime.domainspec.Command;

/**
 *
 * @author nuwan
 */
public class UserLoginRequest extends Command{
    private final String userId;
    public UserLoginRequest(Builder builder) {
        super(builder);
        this.userId = builder.userId;
    }

    public String getUserId() {
        return userId;
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder
    {
        private String userId;
        
        public Builder withUserId(String userId)
        {
            this.userId = userId;
            return this;
        }
        
        @Override
        public UserLoginRequest build()
        {
            return new UserLoginRequest(this);
        }
    }
}
