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
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.runtime.domainspec.Command;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwan
 */
class AuthClientCommand extends Command {

    private final String clientId;
    private String secret;
    private List<String> redirectUrls = new LinkedList<>();

    public AuthClientCommand(Builder builder) {
        super(builder);
        this.clientId = builder.clientId;
        this.secret = builder.secret;
        this.redirectUrls = builder.redirectUrls;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }

    public List<String> getRedirectUrls() {
        return redirectUrls;
    }

    public static final class Builder extends Command.Builder{

        private String clientId;
        private String secret;
        private List<String> redirectUrls = new LinkedList<>();
        
        public Builder withClientId(String clientId)
        {
            this.clientId = clientId;
            return this;
        }
        
        public Builder withSecret(String secret)
        {
            this.secret = secret;
            return this;
        }
        
        public Builder withRedirectUrls(String ... urls)
        {
            redirectUrls.addAll(Arrays.asList(urls));
            return this;
        }
        
        @Override
        public AuthClientCommand build()
        {
            return new AuthClientCommand(this);
        }
    }
}
