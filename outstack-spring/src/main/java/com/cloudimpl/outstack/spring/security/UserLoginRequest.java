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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public class UserLoginRequest extends Command{
    private final String userId;
    private final String password;
    private final Map<String,String> mapAttr;
    
    public UserLoginRequest(Builder builder) {
        super(builder);
        this.userId = builder.userId;
        this.password = builder.password;
        this.mapAttr = Collections.unmodifiableMap(builder.mapAttr);
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, String> getMapAttr() {
        return mapAttr;
    }
   
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder
    {
        private String userId;
        private String password;
        private Map<String,String> mapAttr = new HashMap<>()    ;
        
        public Builder withUserId(String userId)
        {
            this.userId = userId;
            return this;
        }
        
        public Builder withPassword(String password)
        {
            this.password = password;
            return this;
        }
        
        public Builder withAttr(String key,String value)
        {
            this.mapAttr.put(key, value);
            return this;
        }
        
        @Override
        public UserLoginRequest build()
        {
            return new UserLoginRequest(this);
        }
    }
}
