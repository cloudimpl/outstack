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

import com.cloudimpl.outstack.workflow.ConditionalWorkflow;
import com.cloudimpl.outstack.workflow.SequentialWorkflow;
import com.cloudimpl.outstack.workflow.WorkUnit;
import com.cloudimpl.outstack.workflow.WorkflowEngine;

/**
 *
 * @author nuwan
 */
public class ConditionalTest {

    public static void main(String[] args) throws InterruptedException {
        SequentialWorkflow seq1 = SequentialWorkflow.name("seq1")
                .execute(ConditionalWorkflow.name("test2")
                        .take(2)
                        .from(WorkUnit.of("work1", new CancelTest.DynamicWork("work1")).build())
                        .from(WorkUnit.of("work2", new CancelTest.DynamicWork("work2")).build())
                        .from(WorkUnit.of("work3", new CancelTest.DynamicWork("work3")).build())
                        .build())
                .build();

        WorkflowEngine engine = new WorkflowEngine("test");
        engine.execute(seq1).subscribe();

        Thread.sleep(5000);
        SequentialWorkflow seq2 = SequentialWorkflow.name("seq2")
                .execute(ConditionalWorkflow.name("test3")
                        .take(2)
                        .from(WorkUnit.waitFor("work1"))
                        .from(WorkUnit.gateFor("work2"))
                        .from(WorkUnit.of("work3", new CancelTest.DynamicWork("work3")).build())
                        .build())
                .build();

        engine = new WorkflowEngine("test");
        engine.execute(seq2).subscribe();

        try {
            engine.execute("work2", ctx -> "hello");

        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        Thread.sleep(1000);
        engine.execute("work1", ctx -> "hello2");

        Thread.sleep(10000000);
    }
}
