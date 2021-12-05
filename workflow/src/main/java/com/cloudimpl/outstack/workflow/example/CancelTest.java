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

import com.cloudimpl.outstack.workflow.ParallelWorkflow;
import com.cloudimpl.outstack.workflow.SequentialWorkflow;
import com.cloudimpl.outstack.workflow.StatefullWork;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkStatus;
import com.cloudimpl.outstack.workflow.WorkUnit;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class CancelTest {

    public static void main(String[] args) throws InterruptedException {
//        SequentialWorkflow seq = SequentialWorkflow.name("seq1")
//                .execute(WorkUnit.of("work1", new DynamicWork("work1")).build())
//                .then(WorkUnit.of("work2", new DynamicWork("work2")).build())
//                .then(WorkUnit.waitFor("work3"))
//                .then(ParallelWorkflow.name("parallel").execute(WorkUnit.of("work4", new DynamicWork("work4")).build(), WorkUnit.of("work5", new DynamicWork("work5")).build()).build())
//                .build();
//
//        WorkflowEngine engine = new WorkflowEngine("1");
//        engine.execute(seq).doOnNext(s -> System.out.println(s)).subscribe();
//
//        Thread.sleep(5000);
//        engine.executeNext((c) -> {
//            c.put("1", "work3");
//            return Mono.just("hello");
//        }).subscribe();
//        Thread.sleep(5000);

        SequentialWorkflow seq = SequentialWorkflow.name("seq1")
                .execute(WorkUnit.of("work1", new DynamicWork("work1")).build())
                .then(WorkUnit.of("work2", new DynamicWork("work2")).build())
                .then(WorkUnit.waitFor("work3"))
                .then(ParallelWorkflow.name("parallel").execute(WorkUnit.waitFor("work4"), WorkUnit.waitFor("work5")).build())
                .build();

        WorkflowEngine engine = new WorkflowEngine("2");
        engine.execute(seq).doOnNext(s -> System.out.println(s)).subscribe();
        Thread.sleep(5000);
        engine.executeAsyncNext((c) -> {
            c.put("1", "work3");
            return Mono.just(WorkStatus.publish(Work.Status.CANCELLED,"hello"));
        }).subscribe();
        Thread.sleep(1000);
       // engine.cancel();
        Thread.sleep(5000);
        engine.executeAsync("work4",(c) -> {
            c.put("1", "work4");
            return Mono.just("hello");
        }).subscribe();
         engine.executeAsync("work5",(c) -> {
            c.put("1", "work5");
            return Mono.just("hello");
        }).subscribe();
        //engine.cancel();
         Thread.sleep(500000000);
    }

    public static final class DynamicWork implements StatefullWork {

        private String msg;

        public DynamicWork(String msg) {
            this.msg = msg;
        }

        @Override
        public Mono<WorkStatus> execute(WorkContext context) {
            context.put(msg, msg);
            if (msg.equals("work1")) {

                return Mono.just(WorkStatus.publish(Status.COMPLETED));
            } else if (msg.equals("work2")) {

                return Mono.just(WorkStatus.publish(Status.COMPLETED));
            }

            return Mono.just(WorkStatus.publish(Status.COMPLETED));
        }

    }
}
