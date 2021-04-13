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
package com.cloudimpl.outstack.common;

import java.time.Duration;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

/**
 *
 * @author nuwansa
 */
public class JoinFlux<T> {
    private final SingleFluxProcessor<T> fluxProcessor;
    private volatile boolean isRealtime = false;
    private Queue<T> queue;
    private final Flux<T> realtimeFlux;
    private JoinFlux(Flux<T> realtimeFlux)
    {
        this.fluxProcessor = new SingleFluxProcessor<>(this::switchToRealtime);
        this.realtimeFlux = realtimeFlux.doOnNext(this::onRealtime).doOnError(err->this.fluxProcessor.error(err));
    }
    
    
    private synchronized void switchToRealtime()
    {
        T msg = null;
        while((msg = queue.poll()) != null)
        {
            this.fluxProcessor.send(msg);
        }
        isRealtime = true;
    }
    
    private synchronized void onRealtime(T msg)
    {
        if(isRealtime)
        {
            fluxProcessor.send(msg);
        }
        else
        {
            this.queue.add(msg);
        }
    }
    
    private synchronized void initRealtime(Subscription t) {
        if(this.queue != null)
        {
            return;
        }
        this.queue = new ConcurrentLinkedQueue<>();
        this.realtimeFlux.subscribe();
    }
    
    private Flux<T> flux()
    {
        return fluxProcessor.flux();
    }
    
     private void close() {
        this.queue.clear();
    }

    public static <T> Flux<T> create(Flux<T> historyFlux,Flux<T> realtimeFlux)
    {
        JoinFlux<T> joinFlux = new JoinFlux<>(realtimeFlux);
        return historyFlux.doOnSubscribe(s->joinFlux.initRealtime(s)).concatWith(joinFlux.flux())
                .doOnTerminate(()->joinFlux.close())
                .doOnCancel(()->joinFlux.close());
    }
    
    public static void main(String[] args) throws InterruptedException {
        Flux<Long> flx = Flux.fromIterable(Arrays.asList(1L,2L,2L,2L,2L,4L,5L)).concatWith(Flux.interval(Duration.ofSeconds(1))).groupBy(Function.identity()).flatMap(f->f.reduce((a,b)->Long.compare(a, b) > 0 ? a: b));
        flx.subscribe(System.out::println);
//        List<String> history = new CopyOnWriteArrayList<>();
//        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1));
//        Flux<String> realtime = flux.share().map(i->""+i);
//        realtime.doOnNext(s->System.out.println("addding : "+s)).doOnNext(i->history.add("histroy"+i)).subscribe();
//        Thread.sleep(10000);
//        JoinFlux.create(Flux.fromIterable(history), realtime).doOnNext(System.out::println).subscribe();
        Thread.sleep(100000000);
    }
    
}
