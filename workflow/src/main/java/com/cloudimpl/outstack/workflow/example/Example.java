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

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.workflow.ExternalTrigger;
import com.cloudimpl.outstack.workflow.ParallelWorkflow;
import com.cloudimpl.outstack.workflow.SequentialWorkflow;
import com.cloudimpl.outstack.workflow.Work;
import com.cloudimpl.outstack.workflow.Work.Status;
import com.cloudimpl.outstack.workflow.WorkContext;
import com.cloudimpl.outstack.workflow.WorkStatus;
import com.cloudimpl.outstack.workflow.WorkUnit;
import com.cloudimpl.outstack.workflow.WorkflowEngine;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class Example {

    static class Test
    {
        Map<String,AtomicReference<Status>> stats = new HashMap<>();
        public Test()
        {
            stats.put("asfas", new AtomicReference<>(Work.Status.PENDING));
        }

    }
    
    public static void main(String[] args) throws InterruptedException {
        
        String data = GsonCodec.encode(new Test());
        System.out.println(data);
        Test k = GsonCodec.decode(Test.class, data);
        System.out.println(k.stats.get("asfas").get());
        SequentialWorkflow sequence
                = SequentialWorkflow
                        .name("seq1")
                        .execute(WorkUnit.of("work1",new Work2(Arrays.asList("nuwan","sanjeewa"))).build())
                        .then(WorkUnit.of("work2", new Work2("work2")).build())
                        .then(WorkUnit.of("work3", new Work2("work3")).build())
                        .then(WorkUnit.waitFor("work6"))
                        .then(ParallelWorkflow.name("parrallal").execute(WorkUnit.of("work4",new Work2("work4-parallel")).build())
                                .execute(WorkUnit.of("work5", new Work2("work5-parallel"))
                                .build())
                        .build()).build();
        WorkflowEngine engine  = new WorkflowEngine("1");
        
        engine.execute(sequence).subscribe();
//
//        Thread.sleep(2000);
//        engine.externalTrigger("work6",(t) -> {
//            return Mono.error(()->new RuntimeException("xxx"));
//        }).doOnError(err->Throwable.class.cast(err).printStackTrace()).doOnNext(s->System.out.println("s: "+s)).subscribe();
//        
        Thread.sleep(5000);
         engine.executeAsync("work6",(t) -> {
            return Mono.just("hello");
        }).doOnError(err->Throwable.class.cast(err).printStackTrace()).doOnNext(s->System.out.println("s: "+s)).subscribe();
        
         
         engine.executeAsync("work6",(t) -> {
            return Mono.just("hello");
        }).doOnError(err->Throwable.class.cast(err).printStackTrace()).doOnNext(s->System.out.println("s: "+s)).subscribe();
         
         
        System.out.println("---------------------------");
        SequentialWorkflow seq1 = SequentialWorkflow.name("seq1").execute(WorkUnit.of("work1-seq1", new Work2("work1-seq1")).build()).then(WorkUnit.of("work2-seq1", new Work2("work2-seq1")).build()).build();
        SequentialWorkflow seq2 = SequentialWorkflow.name("seq2").execute(WorkUnit.of("work1-seq2", new Work2("work1-seq2")).build()).then(WorkUnit.of("work2-seq2", new Work2("work2-seq2")).build()).build();
        ParallelWorkflow parallal = ParallelWorkflow
                .name("parrallel1").execute(
                seq1, seq2
        )
                .build();

        engine = new WorkflowEngine("1");
        engine.execute(parallal).subscribe();

        Thread.sleep(10000000);
    }

    public static class Work1 implements Work {

        public Work1() {
        }

        @Override
        public Mono<WorkStatus> execute(WorkContext context) {
            System.out.println("work1 " + " executed" + " Thread : " + Thread.currentThread().getName());
            return Mono.just(WorkStatus.publish(Work.Status.COMPLETED, context));
        }
    }

    public static class Work2 implements Work {

        private List<String> msg;

        public Work2(List<String> msg) {
            this.msg = msg;
        }

         public Work2(String  msg) {
            this.msg = Collections.singletonList(msg);
        }
        @Override
        public Mono<WorkStatus> execute(WorkContext context) {
//            if(msg.equals("work4-parallel"))
//                return Mono.error(()->new RuntimeException("xxx"));

            System.out.println(msg + " executed" + " Thread : " + Thread.currentThread().getName());
            return Mono.just(WorkStatus.publish(Work.Status.COMPLETED, context));
        }
    }
}
