/*
 * Copyright 2020 nuwansa.
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
package com.cloudimpl.outstack.le;

import com.cloudimpl.outstack.collection.AwsCollectionProvider;
import com.cloudimpl.outstack.collection.CollectionOptions;
import com.cloudimpl.outstack.collection.CollectionProvider;
import com.cloudimpl.outstack.collection.MemCollectionProvider;
import com.cloudimpl.outstack.core.logger.ILogger;
import com.cloudimpl.outstack.logger.ConsoleLogWriter;
import com.cloudimpl.outstack.logger.Logger;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Flux;

/**
 *
 * @author nuwansa
 */
public class LeaderElectionTest {

    public static void main(String[] args) throws InterruptedException {
        CollectionProvider provider = new MemCollectionProvider();
        ILogger logger = new Logger("test", "node", new ConsoleLogWriter());
        logger.setLevel(ILogger.LogLevel.DEBUG);
        LeaderElectionManager man = new LeaderElectionManager(provider);
        Flux.interval(Duration.ofSeconds(5)).take(100).doOnNext(i->new LeaderWorker(logger, provider)).subscribe();
        Thread.sleep(1000000);
    }

    public static final class LeaderWorker {
        public static final AtomicInteger id = new AtomicInteger();
        public static final Set<String> members = new ConcurrentSkipListSet<>();
        public static final List<LeaderElection> els = new CopyOnWriteArrayList<>();
        public LeaderWorker(ILogger logger,CollectionProvider provider) {
            LeaderElectionManager man = new LeaderElectionManager(provider);
            LeaderElection el = man.create("TestGroup", "member:"+id.incrementAndGet(), 10000, logger);
            System.out.println("member initialized: "+el.getMemberId());
            members.forEach(m->el.addMember(m));
            members.add(el.getMemberId());
            els.forEach(e->e.addMember(el.getMemberId()));
            els.add(el);
            el.run((LeaderElection le, String leaderId) -> {
                System.out.println(le.getMemberId()+":leader changed: " + leaderId);
                if(leaderId.equals(le.getMemberId()))
                {
                    Executors.newScheduledThreadPool(1).schedule(()->{
                        System.out.println("terminating member :"+le.getMemberId());
                        le.close();
                        els.remove(le);
                        els.forEach(e->e.removeMember(le.getMemberId()));
                        members.remove(le.getMemberId());
                    }, 20, TimeUnit.SECONDS);
                }
            });
        }

        
    }
}
