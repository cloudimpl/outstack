package com.cloudimpl.outstack.repo.postgres;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.repo.Entity;
import com.cloudimpl.outstack.repo.EntityUtil;
import com.cloudimpl.outstack.repo.RepoException;
import com.cloudimpl.outstack.repo.RepoUtil;
import com.cloudimpl.outstack.repo.core.ReactiveRepository;
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

//|tenantId,resourceType,parentTenantId,parentTid,id,tid,entity,createdDate,updatedDate
public class Postgres13ReactiveRepository extends Postgres13ReadOnlyReactiveRepository implements ReactiveRepository {

    private transient boolean init;


    private final Map<String,String> tenants = new ConcurrentHashMap<>();
    public Postgres13ReactiveRepository() {
        this.init = false;
    }

    @PostConstruct
    private void init() {
        initTables()
                .doOnError(thr->thr.printStackTrace())
                .subscribe(); //bootstrap tables
    }

    @Override
    protected  Mono<Postgres13ReactiveRepository> initTables() {
        if (init) {
            return Mono.just(this);
        }
        synchronized (this)
        {
            return createEntityTable(table.name())
                    .then(Mono.defer(() -> Mono.just(table.trackChanges()))
                            .filter(s -> s).flatMap(s -> createAuditTable(table.name())))
                    .doOnNext(s -> this.init = true);
        }
    }

    @Override
    public <T extends Entity> Mono<T> create(String tenantId, T entity) {
       return createTenantIfNotExist(tenantId).flatMap(it->it.executeMono(config.connectionFromPool(table.config(),tenantId),conn->create(conn,tenantId,entity)));
    }

    @Override
    public <T extends Entity> Mono<T> createOrUpdate(String tenantId,T entity){
        return  createTenantIfNotExist(tenantId).flatMap(it->it.executeMono(config.connectionFromPool(table.config(), tenantId),conn->createOrUpdate(conn,tenantId,entity)));
    }

    public Mono<Void> delete(String tenantId,Class<? extends Entity> resourceType,String id){
        return  createTenantIfNotExist(tenantId).flatMap(it->it.executeMono(config.connectionFromPool(table.config(), tenantId),conn->delete(conn,tenantId,resourceType,id))).then();
    }

    @Override
    public <T extends Entity> Mono<T> createChild(String parentTenantId,String parentTid,String tenantId,T child)
    {
        return createTenantIfNotExist(tenantId).flatMap(it->it.executeMono(config.connectionFromPool(table.config(), tenantId)
        ,conn->createChild(conn,parentTenantId,parentTid,tenantId,child)));
    }

    private <T extends Entity> Mono<T> createChild(Connection connection,String parentTenantId,String parentTid,String tenantId,T child)
    {
        Objects.requireNonNull(parentTid);
        String json = GsonCodec.encode(child);
        String tid = RepoUtil.createUUID();
        long time = System.currentTimeMillis();
        String parentTenantId2 = parentTenantId == null ? "default" : parentTenantId;
        String tenantId2 = tenantId == null ? "default": tenantId;

        return Mono.just(connection) .flatMapMany(conn -> conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,parentTenantId,parentTid,id,tid,entity,createdTime,updatedTime) " +
                        "select $1,$2,$3,$4,$5,$6,$7::JSON,$8,$9 "+
                        "where exists (select tid from " +table.name()+" where tenantId = $10 and tid = $11) "+
                                "on conflict (tenantId,resourceType,id) do nothing returning tid ")
                        .bind("$1", tenantId2)
                        .bind("$2", child.getClass().getName())
                        .bind("$3",parentTenantId2)
                        .bind("$4",parentTid)
                        .bind("$5", child.id())
                        .bind("$6", tid)
                        .bind("$7", json)
                        .bind("$8", time)
                        .bind("$9", time)
                        .bind("$10",parentTenantId2)
                        .bind("$11",parentTid)
                        .execute()).take(1).flatMap(it->it.map((row, meta) -> row.get("tid", String.class)))
                .map(it->(T) EntityUtil.with(child,it,tenantId,time,time)).next()
                .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("entity already exist or parent not found"))));
    }

    private <T extends Entity> Mono<T> create(Connection connection,String tenantId,T entity) {
        String json = GsonCodec.encode(entity);
        String tid = RepoUtil.createUUID();
        long time = System.currentTimeMillis();
        return Mono.just(connection) .flatMapMany(conn -> conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,id,tid,entity,createdTime,updatedTime) values($1,$2,$3,$4,$5::JSON,$6,$7) on conflict (tenantId,resourceType,id) do nothing returning tid")
                        .bind("$1", tenantId == null ? "default":tenantId)
                        .bind("$2", entity.getClass().getName())
                        .bind("$3", entity.id())
                        .bind("$4", tid)
                        .bind("$5", json)
                        .bind("$6", time)
                        .bind("$7", time)
                        .execute()).take(1).flatMap(it->it.map((row, meta) -> row.get("tid", String.class)))
                .map(it->(T)EntityUtil.with(entity,it,tenantId,time,time)).next()
                .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("entity already exist"))));
    }

    private <T extends Entity> Mono<T> createOrUpdate(Connection connection,String tenantId,T entity)
    {
        String json = GsonCodec.encode(entity);
        String tid = RepoUtil.createUUID();
        long time = System.currentTimeMillis();
        return Mono.just(connection) .flatMapMany(conn -> conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,id,tid,entity,createdTime,updatedTime) values($1,$2,$3,$4,$5::JSON,$6,$7) on conflict (tenantId,resourceType,id) do update set " +
                        "entity = $8::JSON , updatedTime = $9 returning tid,createdTime,updatedTime")
                .bind("$1", tenantId == null ? "default":tenantId)
                .bind("$2", entity.getClass().getName())
                .bind("$3", entity.id())
                .bind("$4", tid)
                .bind("$5", json)
                .bind("$6", time)
                .bind("$7", time)
                .bind("$8", json)
                .bind("$9", time)
                .execute()).take(1)
                .flatMap(it->it.map((row, meta) -> EntityUtil.with(entity
                        ,row.get("tid", String.class),tenantId
                        ,row.get("createdTime",Long.class)
                        ,row.get("updatedTime",Long.class)
                        )))
                .map(it->(T)entity).next();
    }

    private <T extends Entity> Mono<T> delete(Connection connection,String tenantId,Class<T> resourceType,String id)
    {
        if(id.startsWith("id-"))
        {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("delete from " + table.name() + " as x where x.tenantId = $1 and x.resourceType = $2 and x.tid = $3 " +
                                    " and not exists( select 1 from "+table.name() + " as k where k.parentTenantId = x.tenantId and parentTid = x.tid )"+
                                    "returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                    .bind("$1",tenantId == null ? "default":tenantId)
                    .bind("$2",resourceType.getName())
                    .bind("$3",id)
                    .execute()).take(1)
                    .flatMap(it->it.map((row, meta) ->(T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("entity not exist or child entity attached to it"))));
        }else
        {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("delete from " + table.name() + " as x where x.tenantId = $1 and x.resourceType = $2 and x.id = $3 " +
                            " and not exists (select 1 from "+table.name() + " as k where k.parentTenantId = x.tenantId and parentTid = x.tid )"+
                            "returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                    .bind("$1",tenantId == null ? "default":tenantId)
                    .bind("$2",resourceType.getName())
                    .bind("$3",id)
                    .execute()).take(1).flatMap(it->it.map((row, meta) ->(T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(()->Mono.error(new RepoException("entity not exist or child entity attached to it"))));
        }

    }

    private <T extends Entity> Mono<T> update(Connection connection,String tenantId,T entity)
    {
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement("update " + table.name() + " set tenantId = $1 and resourceType = $3 and id = $2 returning tenantId,createdTime,updatedTime,tid,entity")
                .bind("$1",tenantId == null ? "default":tenantId)
                .bind("$2",entity.getClass().getName())
                .bind("$3",entity.id())
                .execute()).take(1).flatMap(it->it.map((row, meta) ->(T) (createEntity(row))))
                .next();
    }

    @Override
    protected Mono<Postgres13ReactiveRepository> createTenantIfNotExist(String tenantId)
    {
        String _tenantId = tenantId == null ? "default":tenantId;
         return Mono.justOrEmpty(tenants.get(_tenantId)).map(s->this)
                 .switchIfEmpty(Mono.defer(()->createTenantTable(_tenantId).doOnNext(it->tenants.put(_tenantId,_tenantId))));
    }

    private Mono<Postgres13ReactiveRepository> createTenantTable(String tenantId){
        return executeMono(config.connectionFromPool(table.config(),tenantId),connection -> Mono.just(connection)
                .flatMapMany(conn->conn.createStatement("create table if not exists " + table.name() + "_" + tenantId.replaceAll("-", "_") + " partition of " + table.name() + " for values in ('" + tenantId + "')").execute())
                .next())
                .doOnNext(s -> System.out.println("table " + table.name()+"_"+tenantId.replaceAll("-","_") + " created"))
                .then(Mono.just(this));
    }

    private <T extends Entity> Mono<Postgres13ReactiveRepository> createEntityTable(String tableName) {
        return executeMono(config.connectionFromPool(table.config(),null), conn -> Mono.just(conn)
                .flatMapMany(connection -> connection.createStatement("create table if not exists " + tableName + "(tenantId varchar not null,resourceType varchar not null," +
                        "parentTenantId varchar," +
                        "parentTid varchar," +
                        "id varchar not null," +
                        "tid varchar not null," +
                        "entity jsonb not null," +
                        "createdTime bigint not null," +
                        "updatedTime bigint not null," +
                        "primary key (tenantId,resourceType,id)) partition by LIST(tenantId)").execute())
                .next())
                .doOnNext(s -> System.out.println("table " + tableName + " created"))
                .then(Mono.just(this));
    }

    private <T extends Entity> Mono<Postgres13ReactiveRepository> createAuditTable(String tableName) {
        return executeMono(config.connectionFromPool(table.config(),null), conn -> Mono.just(conn)
                .flatMapMany(connection -> connection.createStatement("create table if not exists " + tableName + "_Audit" + "(tenantId varchar not null,resourceType varchar not null," +
                        "parentTenantId varchar,"+
                        "parentTid varchar," +
                        "id varchar not null," +
                        "tid varchar not null," +
                        "entity jsonb not null," +
                        "createdDate bigint not null," +
                        "updatedDate bigint not null," +
                        "primary key (tenantId,resourceType,id)) partition by LIST(tenantId)").execute())
                .next())
                .doOnNext(s -> System.out.println("table " + tableName + "_Audit created"))
                .then(Mono.just(this));
    }
}
