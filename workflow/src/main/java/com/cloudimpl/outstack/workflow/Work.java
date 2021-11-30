/*
 * Copyright 2021 nuwansa.
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

import com.cloudimpl.outstack.workflow.domain.WorkEntity;
import java.util.LinkedList;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwansa
 */
public interface Work {

    String id();

    default String name() {
        return "";
    }

    Mono<WorkResult> execute(WorkContext context);

    public static Builder of(Class<? extends Work> cls) {
        return new Builder().withWork(cls);
    }

    public static final class Builder {

        private String work;
        private final List<WorkEntity.Param> params = new LinkedList<>();

        public Builder withWork(Class<? extends Work> cls) {
            work = cls.getName();
            return this;
        }

        public Builder withParam(Object... param) {
            this.params.add(new WorkEntity.Param(param, param.getClass().getName()));
            return this;
        }

        public WorkCreated toEvent() {
            return new WorkCreated(work, WorkEntity.Status.PENDING, params);
        }
    }
}
