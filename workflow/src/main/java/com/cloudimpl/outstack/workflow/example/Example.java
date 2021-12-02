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

import com.cloudimpl.outstack.workflow.ExternalTrigger;
import com.cloudimpl.outstack.workflow.ParallelWorkflow;
import com.cloudimpl.outstack.workflow.SequentialWorkflow;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkResult;
import com.cloudimpl.outstack.workflow.WorkUnit;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class Example {

    public static void main(String[] args) throws InterruptedException {
        SequentialWorkflow sequence
                = SequentialWorkflow
                        .name("seq1")
                        .execute(WorkUnit.of("work1", Work1.class).build())
                        .then(WorkUnit.of("work2", Work2.class).withParam("work2").build())
                        .then(WorkUnit.of("work3", Work2.class).withParam("work3").build())
                        .then(WorkUnit.of("work6", ExternalTrigger.class).build())
                        .then(ParallelWorkflow.name("parrallal").execute(WorkUnit.of("work4", Work2.class).withParam("work4-parallel").build())
                                .execute(WorkUnit.of("work5", Work2.class).withParam("work5-parallel").build())
                                .build())
                        .build();
        WorkflowEngine engine  = new WorkflowEngine();
        
        engine.execute(sequence, new WorkContext()).subscribe();

        Thread.sleep(2000);
        engine.externalTrigger("work6",(t) -> {
            return Mono.error(()->new RuntimeException("xxx"));
        }).doOnError(err->Throwable.class.cast(err).printStackTrace()).doOnNext(s->System.out.println("s: "+s)).subscribe();
        
        Thread.sleep(2000);
         engine.externalTrigger("work6",(t) -> {
            return "hello";
        }).doOnError(err->Throwable.class.cast(err).printStackTrace()).doOnNext(s->System.out.println("s: "+s)).subscribe();
        
         
         engine.externalTrigger("work6",(t) -> {
            return "hello";
        }).doOnError(err->Throwable.class.cast(err).printStackTrace()).doOnNext(s->System.out.println("s: "+s)).subscribe();
         
         
        System.out.println("---------------------------");
        SequentialWorkflow seq1 = SequentialWorkflow.name("seq1").execute(WorkUnit.of("work1-seq1", Work2.class).withParam("work1-seq1").build()).then(WorkUnit.of("work2-seq1", Work2.class).withParam("work2-seq1").build()).build();
        SequentialWorkflow seq2 = SequentialWorkflow.name("seq2").execute(WorkUnit.of("work1-seq2", Work2.class).withParam("work1-seq2").build()).then(WorkUnit.of("work2-seq2", Work2.class).withParam("work2-seq2").build()).build();
        ParallelWorkflow parallal = ParallelWorkflow
                .name("parrallel1").execute(
                seq1, seq2
        )
                .build();

        engine = new WorkflowEngine();
        engine.execute(parallal, new WorkContext()).subscribe();

        Thread.sleep(10000000);
    }

    public static class Work1 implements Work {

        public Work1() {
        }

        @Override
        public Mono<WorkResult> execute(WorkContext context) {
            System.out.println("work1 " + " executed" + " Thread : " + Thread.currentThread().getName());
            return Mono.just(new WorkResult(Work.Status.COMPLETED, context));
        }
    }

    public static class Work2 implements Work {

        private String msg;

        public Work2(String msg) {
            this.msg = msg;
        }

        @Override
        public Mono<WorkResult> execute(WorkContext context) {
//            if(msg.equals("work4-parallel"))
//                return Mono.error(()->new RuntimeException("xxx"));

            System.out.println(msg + " executed" + " Thread : " + Thread.currentThread().getName());
            return Mono.just(new WorkResult(Work.Status.COMPLETED, context));
        }
    }
}
