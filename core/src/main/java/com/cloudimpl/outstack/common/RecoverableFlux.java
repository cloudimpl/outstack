/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.common;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * @author nuwansa
 */
public class RecoverableFlux<T> {

  private final Flux<T> realtimeStream;
  private final FluxProcessor<T> flux;
  private Queue<T> queue;
  private volatile boolean recoveryDone = false;
  private volatile boolean subscriberAvailable = false;
  private Disposable realtimeHnd;

  private RecoverableFlux(Flux<T> realtimeStream) {
    this.realtimeStream = realtimeStream.doOnNext(this::onRealTimeData).doOnError(this::onError);
    this.queue = new ConcurrentLinkedQueue<>();
    this.flux = new FluxProcessor<>("reliableFlux", this::switchToRealtime);
    this.realtimeHnd = null;
  }

  private synchronized void onRealTimeData(T data) {
    if (!recoveryDone) {
      queue.add(data);
    } else {
      flux.add(data);
    }
  }

  private synchronized void switchToRealtime(FluxSink<T> emitter) {
    System.out.println("switch to realtime");
    T item;
    while ((item = queue.poll()) != null) {
      emitter.next(item);
    }
    recoveryDone = true;
    queue = null;
  }

  private synchronized void initRealtime() {
    System.out.println("init realtime");
    if (subscriberAvailable) {
      throw new RuntimeException("only one subscriber supported");
    }
    subscriberAvailable = true;
    this.realtimeHnd = this.realtimeStream.subscribe();
  }

  private void close() {
    if (queue != null)
      queue.clear();

    subscriberAvailable = false;
    if (this.realtimeHnd != null)
      this.realtimeHnd.dispose();
  }

  private void onError(Throwable thr) {
    flux.error(thr);
  }

  public static <T> Flux<T> create(Flux<T> recoveryChannel, Flux<T> realtimeStream) {
    RecoverableFlux<T> rel = new RecoverableFlux<>(realtimeStream);
    return recoveryChannel.doOnSubscribe(s -> rel.initRealtime()).concatWith(rel.flux.flux("recoverFlux"))
      .doOnCancel(rel::close)
      .doOnTerminate(rel::close);
  }

  public static void main(String[] args) throws InterruptedException {
    FluxProcessor processor = new FluxProcessor("test");
    Flux f = RecoverableFlux.create(Flux.just("history"),processor.flux("realtime"));
    f.doOnNext(System.out::println).subscribe();

    Thread.sleep(10000);
    processor.add("sfasf");
    f.doOnNext(System.out::println).subscribe();
  }
}