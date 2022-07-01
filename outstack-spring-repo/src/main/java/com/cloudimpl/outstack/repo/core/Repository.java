package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.RepoUtil;
import com.cloudimpl.outstack.repo.SafeExecute;
import com.cloudimpl.outstack.repo.Table;
import com.cloudimpl.outstack.repo.core.geo.BaseRepository;
import com.cloudimpl.outstack.repo.postgres.Postgres13ReactiveRepository;
import com.cloudimpl.outstack.repo.postgres.ReactivePostgresConfig;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import io.r2dbc.spi.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Repository {

    @Autowired
    protected ReactivePostgresConfig config;

    @Autowired
    @Qualifier("ioSchedular")
    protected Scheduler ioScheduler;

    protected Table table;

    protected final ValidatorFactory factory;
    protected final Validator validator;

    public Repository() {
        table = RepoUtil.getRepoMeta(this.getClass(),false);
        factory = Validation.buildDefaultValidatorFactory();
        this.validator = this.factory.getValidator();
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
        try
        {
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
        }catch (Throwable thr)
        {
            return Mono.error(thr);
        }

    }

    protected <T> Mono<T> executeTxMono(Supplier<Mono<Connection>> supplier, Function<Connection, Mono<T>>... functions) {
        try{
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
        }catch (Throwable thr)
        {
            return Mono.error(thr);
        }

    }

    protected <T> Flux<T> executeFlux(Supplier<Mono<Connection>> supplier, Function<Connection, Flux<T>> function) {
        try
        {
            SafeExecute safeAction = new SafeExecute();
            return supplier.get()
                    .flatMap(connection -> Mono.from(connection.setAutoCommit(false))
                            .thenReturn(connection))
                    .flatMap(connection -> Mono.from(connection.beginTransaction()).thenReturn(connection))
                    .flatMapMany(connection -> function.apply(connection)
                            .doOnComplete(() -> Mono.from(connection.commitTransaction()).subscribe())
                            .doOnCancel(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe()))
                            .doOnTerminate(() -> safeAction.execute(() -> Mono.from(connection.close()).subscribe())))
                    .publishOn(ioScheduler);
        }catch (Throwable thr)
        {
            return Flux.error(thr);
        }

    }

    protected <T> void validateObject(T target) {
        Set<ConstraintViolation<T>> violations = this.validator.validate(target);
        if (!violations.isEmpty()) {
            ValidationErrorException error = new ValidationErrorException(violations.stream().findFirst().get().getMessage());
            throw error;
        }
    }
}
