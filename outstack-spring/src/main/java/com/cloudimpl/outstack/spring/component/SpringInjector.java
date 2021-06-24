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

import com.cloudimpl.outstack.core.Injector;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 *
 * @author nuwan
 */
public class SpringInjector extends Injector {

    private final AutowireCapableBeanFactory beanFactory;
    
    public SpringInjector(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    @Override
    public <T> T inject(Class<T> clazz) {
        T instance = super.inject(clazz);
        beanFactory.autowireBean(instance);
        return instance;
    }
    
    @Override
    public void inject(Object injectableObject) {
        super.inject(injectableObject);
        beanFactory.autowireBean(injectableObject);
    }
    
}
