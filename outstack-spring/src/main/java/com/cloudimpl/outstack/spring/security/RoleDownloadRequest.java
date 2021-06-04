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
public class RoleDownloadRequest extends Command {

    private final String context;
    private final String subject;

    public RoleDownloadRequest(Builder builder) {
        super(builder);
        this.context  = builder.context;
        this.subject = builder.subject;
    }

    public String getContext() {
        return context;
    }

    public String getSubject() {
        return subject;
    }

    public static RoleDownloadRequest.Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder {

        private String context;
        private String subject;
        
        public Builder withSubject(String subject)
        {
            this.subject = subject;
            return this;
        }
        
        public Builder withContext(String context)
        {
            this.context = context;
            return this;
        }
        
    }
}
