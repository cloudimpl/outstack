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
public class Example2 {
    public static void main(String[] args) throws InterruptedException {
        
        ParallelWorkflow parallel = ParallelWorkflow.name("p1")
                .execute(WorkUnit.of("work3", DynamicWork.class).withParam("work3").build(),WorkUnit.of("work4", DynamicWork.class).withParam("work4").build())
                .build();
        
        SequentialWorkflow sequential = SequentialWorkflow.name("seq1").execute(WorkUnit.of("work1", Work1.class).build())
                .then(WorkUnit.of("work2", DynamicWork.class).withParam("work2").build())
                .then(parallel)
                .build();
        
        WorkflowEngine engine = new WorkflowEngine();
        engine.execute(sequential, new WorkContext()).subscribe();
        
        Thread.sleep(10000000);
    }
    
    
    public static class Work1 implements Work
    {

        @Override
        public Mono<WorkResult> execute(WorkContext context) {
            System.out.println("work1 executed at "+Thread.currentThread().getName());
            return Mono.just(new WorkResult(Status.COMPLETED, context));
        }
        
    }
    
    public static class DynamicWork implements Work
    {
        private String msg;

        public DynamicWork(String msg) {
            this.msg = msg;
        }
        
        @Override
        public Mono<WorkResult> execute(WorkContext context) {
            System.out.println(msg+" executed at "+Thread.currentThread().getName());
            return Mono.just(new WorkResult(Work.Status.COMPLETED, context));
        }
        
    }
    
     public static class BuggyWork implements Work
    {
        private String msg;

        public BuggyWork(String msg) {
            this.msg = msg;
        }
        
        @Override
        public Mono<WorkResult> execute(WorkContext context) {
            if(true)
            {
                return Mono.error(()->new RuntimeException("buggy"));
            }
            System.out.println(msg+" executed at "+Thread.currentThread().getName());
            return Mono.just(new WorkResult(Work.Status.COMPLETED, context));
        }
        
    }
}
