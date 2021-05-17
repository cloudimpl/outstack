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
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.core.CloudUtil;
import com.cloudimpl.outstack.core.ComponentProvider;
import com.cloudimpl.outstack.core.ComponentProviderManager;
import com.cloudimpl.outstack.core.Injector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author nuwan
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("outstack")
public class SpringApplicationConfigManager implements ComponentProviderManager {

    private String apiContext;
    private List<Provider> providers;
    private Injector injector;

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    protected void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    @Override
    public ComponentProvider getProvider(String name) {
        return providers.stream().filter(p -> p.getName().equals(name))
                .filter(p->p.getStatus().isPresent())
                .filter(p->p.getStatus().get().equalsIgnoreCase("active"))
                .peek(p->p.setInjector(injector)).findFirst().get();
    }

    public static final class Provider implements ComponentProvider {

        private String name;
        private String impl;
        public Map<String, String> configs = new HashMap<>();
        private String status;
        private Injector injector;

        public void setImpl(String impl) {
            this.impl = impl;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Optional<String> getStatus() {
            return Optional.ofNullable(status);
        }

        
        @Override
        public String getName() {
            return name;
        }

        protected void setInjector(Injector injector) {
            this.injector = injector;
        }

        @Override
        public String getImpl() {
            return impl;
        }

        public void setConfigs(Map<String, String> configs) {
            this.configs = configs;
        }

        @Override
        public String toString() {
            return "Provider{" + "name=" + name + ", impl=" + impl + '}';
        }

        @Override
        public <T> T getInstance() {
            return injector
                    .with(ProviderConfigs.class, getOptions())
                    .inject(CloudUtil.classForName(getImpl()));
        }

        @Override
        public ProviderConfigs getOptions() {
            return (String option, String defaultVal) -> Optional.ofNullable(configs.getOrDefault(option, defaultVal));
        }

    }
}
