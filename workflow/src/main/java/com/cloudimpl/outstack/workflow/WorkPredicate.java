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
package com.cloudimpl.outstack.workflow;

import com.cloudimpl.outstack.core.CloudUtil;

/**
 *
 * @author nuwan
 */
public interface WorkPredicate {

    boolean apply(WorkContext context);

    public static String of(Class<? extends WorkPredicate> predicate) {
        return predicate.getName();
    }

    public static <T extends WorkPredicate> T from(String predicate) {
        return CloudUtil.newInstance(CloudUtil.classForName(predicate));
    }
}
