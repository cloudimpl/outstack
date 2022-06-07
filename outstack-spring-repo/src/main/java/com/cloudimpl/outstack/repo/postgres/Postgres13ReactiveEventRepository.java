package com.cloudimpl.outstack.repo.postgres;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.repo.Entity;
import com.cloudimpl.outstack.repo.Event;
import com.cloudimpl.outstack.repo.EventUtil;
import com.cloudimpl.outstack.repo.RepoException;
import com.cloudimpl.outstack.repo.RepoUtil;
import com.cloudimpl.outstack.repo.core.ReactiveEventRepository;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Postgres13ReactiveEventRepository extends Postgres13ReadOnlyReactiveEventRepository implements ReactiveEventRepository {

    private final Map<String,String> tenants = new ConcurrentHashMap<>();

    @Override
    public <T extends Event> Mono<T> addEvent(String tenantId, T event) {
        return  createTenantIfNotExist(tenantId).flatMap(it->it.executeMono(config.connectionFromPool(table.config(), tenantId),conn->addEvent(conn,tenantId,event)));
    }

    private <T extends Event> Mono<T> addEvent(Connection connection,String tenantId,T event)
    {
        String json = GsonCodec.encode(event);
        String eventId = RepoUtil.createEventUUID();
        long time = System.currentTimeMillis();
        String tenantId2 = tenantId == null ? "default": tenantId;

        return Mono.just(connection) .flatMapMany(conn -> conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,eventId,event,createdTime,updatedTime) " +
                                "select $1,$2,3,$4::JSON,$5,$6 "+
                                "on conflict (tenantId,resourceType,eventId) do nothing returning eventId")
                        .bind("$1", tenantId2)
                        .bind("$2", event.getClass().getName())
                        .bind("$3",eventId)
                        .bind("$4",json)
                        .bind("$5",time)
                        .bind("$6", time)

                        .execute()).take(1).flatMap(it->it.map((row, meta) -> row.get("eventId", String.class)))
                .map(it->(T) EventUtil.with(event,tenantId,it,time,time)).next()
                .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("event already exist"))));
    }

    @Override
    public <T extends Event> Flux<T> addEvents(String tenantId, Collection<T> events) {
        return createTenantIfNotExist(tenantId).flatMapMany(it->it.executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> addEvents(connection,tenantId,events)));
    }


    private  <T extends Event> Flux<T> addEvents(Connection connection,String tenantId, Collection<T> events) {

        return Mono.just(connection) .flatMapMany(conn -> bindEventList(conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,eventId,event,createdTime,updatedTime) " +
                                "select $1,$2,3,$4::JSON,$5,$6 "+
                                "on conflict (tenantId,resourceType,eventId) do nothing returning eventId"),tenantId,events)
                        .execute())
                .next().flatMapMany(rs->Flux.fromIterable(events))
                .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("event already exist"))));
    }


    private <T extends Event> Statement bindEventList(Statement stmt,String tenantId,Collection<T> events)
    {
        long time = System.currentTimeMillis();
        String tenantId2 = tenantId == null ? "default": tenantId;
            for(T event : events)
            {
                String json = GsonCodec.encode(event);
                String eventId = RepoUtil.createEventUUID();
                stmt = stmt.bind("$1", tenantId2)
                    .bind("$2", event.getClass().getName())
                    .bind("$3",eventId)
                    .bind("$4",json)
                    .bind("$5",time)
                    .bind("$6", time).add();
                EventUtil.with(event,tenantId,eventId,time,time);
            }
            return stmt;
    }

    @Override
    public <T extends Event> Mono<T> updateEvent(String tenantId,String eventId, T event) {
        return createTenantIfNotExist(tenantId).flatMap(it->it.executeMono(config.connectionFromPool(table.config(), tenantId),conn->updateEvent(conn,tenantId,eventId,event)));
    }

    private <T extends Event> Mono<T> updateEvent(Connection connection,String tenantId,String eventId,T event)
    {
        String json = GsonCodec.encode(event);
        long time = System.currentTimeMillis();
        String tenantId2 = tenantId == null ? "default": tenantId;

        return Mono.just(connection) .flatMapMany(conn -> conn.createStatement("update " + table.name() + " set ,event = $1::JSON,updatedTime = $2 " +
                                "where tenantId = $3 and eventId = $4  and resourceType = $5 returning eventId")
                        .bind("$1", tenantId2)
                        .bind("$2", time)
                        .bind("$3",tenantId2)
                        .bind("$4",eventId)
                        .bind("$5",event.getClass().getName())
                        .execute()).take(1).flatMap(it->it.map((row, meta) -> row.get("eventId", String.class)))
                .map(it->(T) EventUtil.withUpdatedTime(EventUtil.withEventId(event,it),time)).next()
                .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("event already exist"))));
    }

    @Override
    public <T extends Event> Mono<T> deleteEvent(String tenantId,Class<T> eventType, String eventId) {
        return null;
    }

    private <T extends Event> Mono<T> deleteEvent(Connection connection,String tenantId,Class<T> eventType, String eventId){
        String tenantId2 = tenantId == null ? "default": tenantId;

        return Mono.just(connection) .flatMapMany(conn -> conn.createStatement("delete from " + table.name() +
                                "where tenantId = $1 and eventId = $2  and resourceType = $3 returning event,createdTime,updatedTime")
                        .bind("$1", tenantId2)
                        .bind("$2", eventId)
                        .bind("$3",eventType.getName())
                        .execute()).take(1)
                .flatMap(it->it.map((row, meta) -> createEvent(tenantId,eventId,eventType,row)))
                .next()
                .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("event already exist"))));
    }

    private <T extends Event> T createEvent(String tenantId, String eventId, Class<T> eventType, Row row)
    {
        T event  = GsonCodec.decode(eventType,row.get("event",Json.class).asString());
        EventUtil.with(event,tenantId,eventId,row.get("createdTime",Long.class),row.get("updatedTime",Long.class));
        return event;
    }

    private <T extends Entity> Mono<Postgres13ReactiveEventRepository> createEventTable(String tableName) {
        return executeMono(config.connectionFromPool(table.config(),null), conn -> Mono.just(conn)
                .flatMapMany(connection -> connection.createStatement("create table if not exists " + tableName + "(tenantId varchar not null,resourceType varchar not null," +
                        "eventId varchar not null," +
                        "event jsonb not null," +
                        "createdTime bigint not null," +
                        "updatedTime bigint not null," +
                        "primary key (tenantId,resourceType,eventId)) partition by LIST(tenantId)").execute())
                .next())
                .doOnNext(s -> System.out.println("table " + tableName + " created"))
                .then(Mono.just(this));
    }

    protected Mono<Postgres13ReactiveEventRepository> createTenantIfNotExist(String tenantId)
    {
        String _tenantId = tenantId == null ? "default":tenantId;
        return Mono.justOrEmpty(tenants.get(_tenantId)).map(s->this)
                .switchIfEmpty(Mono.defer(()->createTenantTable(_tenantId).doOnNext(it->tenants.put(_tenantId,_tenantId))));
    }

    private Mono<Postgres13ReactiveEventRepository> createTenantTable(String tenantId){
        return executeMono(config.connectionFromPool(table.config(),tenantId),connection -> Mono.just(connection)
                .flatMapMany(conn->conn.createStatement("create table if not exists " + table.name() + "_" + tenantId.replaceAll("-", "_") + " partition of " + table.name() + " for values in ('" + tenantId + "')").execute())
                .next())
                .doOnNext(s -> System.out.println("table " + table.name()+"_"+tenantId.replaceAll("-","_") + " created"))
                .then(Mono.just(this));
    }

    @Override
    protected Mono<Void> initTables() {
        return createEventTable(table.name()).then();
    }

}
