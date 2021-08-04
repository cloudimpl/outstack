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
package com.cloudimpl.outstack.runtime.configs;

import com.cloudimpl.outstack.runtime.domainspec.Command;

/**
 *
 * @author nuwan
 */
public class CreateConfigRequest extends Command {

    private String groupName;
    private String configName;
    private String value;
    private String configType;

    public CreateConfigRequest(Builder builder) {
        super(builder);
        this.groupName = builder.groupName;
        this.configName = builder.configName;
        this.value = builder.value;
        this.configType = builder.configType;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getConfigName() {
        return configName;
    }

    public String getValue() {
        return value;
    }
    public String getConfigType() {
        return configType;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Command.Builder {

        private String groupName;
        private String configName;
        private String value;
        private String configType;


        public Builder() {
        }

        public Builder withGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder withConfigName(String configName) {
            this.groupName = configName;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withConfigType(String configType) {
            this.configType = configType;
            return this;
        }
    }
}
