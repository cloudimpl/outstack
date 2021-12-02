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
package com.cloudimpl.outstack.workflow.example;

import com.cloudimpl.outstack.workflow.AbstractWork;
import com.cloudimpl.outstack.workflow.ParallelWorkflow;
import com.cloudimpl.outstack.workflow.SequentialWorkflow;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkUnit;
import com.cloudimpl.outstack.workflow.Workflow;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import com.google.gson.JsonObject;

/**
 *
 * @author nuwan
 */
public class SerializeTest {

    public static void main(String[] args) throws InterruptedException {
        SequentialWorkflow sequence
                = SequentialWorkflow
                        .name("seq1")
                        .execute(WorkUnit.of("work1", Example.Work1.class).build())
                        .then(WorkUnit.of("work2", Example.Work2.class).withParam("work2").build())
                        .then(WorkUnit.of("work3", Example.Work2.class).withParam("work3").build())
                        .then(ParallelWorkflow.name("p1").execute(WorkUnit.of("work4", Example.Work2.class).withParam("work4-parallel").build())
                                .execute(WorkUnit.of("work5", Example.Work2.class).withParam("work5-parallel").build())
                                .build())
                        .build();

        JsonObject json = sequence.toJson();
        System.out.println("json: "+json);
        Workflow work = (Workflow) AbstractWork.fromJson(json);
        WorkflowEngine engine = new WorkflowEngine("1");
        engine.execute(work, new WorkContext()).subscribe();
        Thread.sleep(10000);
    }
}
