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

import com.cloudimpl.outstack.common.FluxProcessor;
import java.time.Duration;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class FluxTest {

    public static void main(String[] args) throws InterruptedException {
//        FluxProcessor<String> flux = new FluxProcessor<>("Test");
//        flux.flux("test").flatMap(s ->retryWrap(s),1).doOnNext(s->System.out.println("out : "+s)).subscribe();
//        flux.add("test1");
//        flux.add("test2");
//        flux.add("test3");
//        
//        
//        Mono.just("test").doOnNext(s->System.out.println("next: "+s)).filter(s->!s.equals("test")).doOnSuccess(s->System.out.println("xxxxx:"+s)).subscribe();
        Mono<String> m1 = Mono.delay(Duration.ofSeconds(1)).map(l -> "m1").doOnNext(System.out::println).doOnCancel(() -> System.out.println("m1 cancel"));
        Mono<String> m2 = Mono.delay(Duration.ofSeconds(2)).map(l -> "m2").doOnNext(System.out::println).doOnCancel(() -> System.out.println("m2 cancel"));;
        Mono<String> m3 = Mono.delay(Duration.ofSeconds(3)).map(l -> "m3").doOnNext(System.out::println).doOnCancel(() -> System.out.println("m3 cancel"));;
        Mono<String> m4 = Mono.delay(Duration.ofSeconds(4)).map(l -> "m4").doOnNext(System.out::println).doOnCancel(() -> System.out.println("m4 cancel"));

        Mono<String> all = m1.flatMap(m -> m2).flatMap(m -> m3).flatMap(m -> m4).doOnCancel(() -> System.out.println("final cancel"));;
        Disposable hnd = all.doOnNext(System.out::println).subscribe();
        Thread.sleep(5000);
        hnd.dispose();
        Thread.sleep(100000000);
    }
    static Set<String> set = new ConcurrentSkipListSet<>();

    private static Mono<String> retryWrap(String s) {
        return Mono.defer(() -> work(s)).doOnError(err -> System.out.println("errr: " + err.getMessage())).retry(10);
    }

    public static Mono<String> work(String s) {
        if (set.add(s)) {
            return Mono.delay(Duration.ofSeconds(1)).flatMap(l -> Mono.error(() -> new RuntimeException("work " + s + " error")));
        }
        return Mono.delay(Duration.ofSeconds(new Random(System.currentTimeMillis()).nextInt(10))).map(l -> s + "done");
    }
}
