package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.RepoUtil;
import com.cloudimpl.outstack.repo.SafeExecute;
import com.cloudimpl.outstack.repo.Table;
import com.cloudimpl.outstack.repo.core.geo.BaseRepository;
import com.cloudimpl.outstack.repo.postgres.Postgres13ReactiveRepository;
import com.cloudimpl.outstack.repo.postgres.ReactivePostgresConfig;
import io.r2dbc.spi.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Repository {

    @Autowired
    protected ReactivePostgresConfig config;

    @Autowired
    @Qualifier("ioSchedular")
    protected Scheduler ioScheduler;

    protected Table table;

    public Repository() {
        table = RepoUtil.getRepoMeta(this.getClass(),false);
    }

    public void setTable(Table table)
    {
        this.table = table;
    }

    @PostConstruct
    public void init() {
        initTables()
                .doOnError(thr->thr.printStackTrace())
                .subscribe(); //bootstrap tables
    }

    protected  abstract Mono<Void> initTables() ;

    protected <T> Mono<T> executeMono(Supplier<Mono<Connection>> supplier, Function<Connection, Mono<T>> function) {
        SafeExecute safeAction = new SafeExecute();
        return supplier.get()
                .flatMap(connection -> Mono.from(connection.setAutoCommit(false))
                        .thenReturn(connection))
                .flatMap(connection -> Mono.from(connection.beginTransaction()).thenReturn(connection))
                // .flatMap(connection -> Mono.from(connection.setAutoCommit(true))
                //          .flatMap(v -> initTables().map(repo -> v))
                .flatMap(connection -> function.apply(connection)
                        .flatMap(v -> Mono.from(connection.commitTransaction()).thenReturn(v))
                        .doOnError(err -> err.printStackTrace())
                        .doOnCancel(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe()))
                        .doOnTerminate(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe())))
                .publishOn(ioScheduler);
    }

    protected <T> Mono<T> executeTxMono(Supplier<Mono<Connection>> supplier, Function<Connection, Mono<T>>... functions) {
        SafeExecute safeAction = new SafeExecute();
        return supplier.get()
                .flatMap(connection -> Mono.from(connection.setAutoCommit(false)).map(v -> connection))
                .flatMap(connection -> Mono.from(connection.beginTransaction()).map(v -> connection))
                .flatMap(connection -> Flux.fromIterable(Arrays.asList(functions))
                        .flatMap(func -> func.apply(connection)).last()
                        .doOnError(err -> err.printStackTrace())
                        .doOnCancel(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe()))
                        .doOnTerminate(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe()))
                ).publishOn(ioScheduler);
    }

    protected <T> Flux<T> executeFlux(Supplier<Mono<Connection>> supplier, Function<Connection, Flux<T>> function) {
        SafeExecute safeAction = new SafeExecute();
        return supplier.get()
                .flatMapMany(connection -> function.apply(connection)
                        .doOnCancel(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe()))
                        .doOnTerminate(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe())))
                .publishOn(ioScheduler);
    }
}
