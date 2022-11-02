package com.cloudimpl.outstack.repo.postgres;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.repo.Entity;
import com.cloudimpl.outstack.repo.EntityUtil;
import com.cloudimpl.outstack.repo.QueryRequest;
import com.cloudimpl.outstack.repo.RepoException;
import com.cloudimpl.outstack.repo.RepoUtil;
import com.cloudimpl.outstack.repo.core.ReactiveRepository;
import com.cloudimpl.outstack.repo.core.geo.GeoData;
import com.cloudimpl.outstack.repo.core.geo.GeoMetry;
import com.cloudimpl.outstack.repo.core.geo.GeoUtil;
import com.cloudimpl.outstack.repo.core.geo.Point;
import com.cloudimpl.outstack.repo.core.geo.Polygon;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

//|tenantId,resourceType,parentTenantId,parentTid,id,tid,entity,createdDate,updatedDate
public class Postgres13ReactiveRepository extends Postgres13ReadOnlyReactiveRepository implements ReactiveRepository {

    private final Map<String, String> tenants = new ConcurrentHashMap<>();

    public Postgres13ReactiveRepository() {

    }

    @Override
    public <T extends Entity> Mono<T> create(String tenantId, T entity) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> create(conn, tenantId, entity)));
    }

    @Override
    public <T extends Entity> Mono<T> createOrUpdate(String tenantId, T entity) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> createOrUpdate(conn, tenantId, entity)));
    }

    @Override
    public <T extends Entity> Flux<T> createOrUpdate(String tenantId, Collection<T> entities) {
        return createTenantIfNotExist(tenantId).flatMapMany(it -> it.executeFlux(config.connectionFromPool(table.config(), tenantId), conn -> Flux.fromIterable(entities).flatMap(e -> createOrUpdate(conn, tenantId, e))));
    }

    @Override
    public <T extends Entity> Mono<T> delete(String tenantId, Class<T> resourceType, String id) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> delete(conn, tenantId, resourceType, id)));
    }

    @Override
    public <T extends Entity> Mono<T> delete(String tenantId, String tid) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> delete(conn, tenantId, tid)));
    }

    @Override
    public <T extends Entity> Mono<T> createChild(String parentTenantId, String parentTid, String tenantId, T child) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId)
                , conn -> createChild(conn, parentTenantId, parentTid, tenantId, child)));
    }

    @Override
    public <T extends Entity> Mono<T> update(String tenantId, T entity, String id) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> update(conn, tenantId, entity, id)));
    }

    @Override
    public <T extends Entity> Mono<T> updateChild(String parentTid, String tenantId, T child, String id) {
        return createTenantIfNotExist(tenantId)
                .flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> updateChild(conn, parentTid, tenantId, child, id)));
    }

    @Override
    public <T extends Entity> Mono<T> convertToChild(String tenantId, String parentTid, String id, Class<T> resourceType) {
        return createTenantIfNotExist(tenantId).flatMap(it -> it.executeMono(config.connectionFromPool(table.config(), tenantId), conn -> convertToChild(conn, tenantId, parentTid, id, resourceType)));
    }

    private void checkGeoEntity(Object child) {
        if (child instanceof GeoData) {
            if (!table.enableGeo()) {
                throw new RepoException("GeoEntity not supported for non geo tables");
            }
        }
    }

    private <T extends Entity> Mono<T> createChild(Connection connection, String parentTenantId, String parentTid, String tenantId, T child) {
        Objects.requireNonNull(parentTid);
        checkGeoEntity(child);
        validateObject(child);
        String json = GsonCodec.encode(child);
        String tid = RepoUtil.createUUID();
        long time = System.currentTimeMillis();
        String parentTenantId2 = parentTenantId == null ? "default" : parentTenantId;
        String tenantId2 = tenantId == null ? "default" : tenantId;
        boolean geoApplicable = table.enableGeo() && child instanceof GeoData && GeoData.class.cast(child).getGeom() != null;

        String sql = "insert into " + table.name() + " as tab(tenantId,resourceType,parentTenantId,parentTid,uniqueId,id,tid,entity,createdTime,updatedTime" + (geoApplicable ? ",geom" : "") + ") " +
                "select $1,$2,$3,$4,$5,$6,$7,$8::JSON,$9,$10" + (geoApplicable ? ",$13 " : " ") +
                "where exists (select tid from " + table.name() + " where tenantId = $11 and tid = $12) " +
                "on conflict (tenantId,resourceType,uniqueId) do nothing returning tid ";

        return Mono.just(connection).flatMapMany(conn -> {
                    Statement stmt = conn.createStatement(sql)
                            .bind("$1", tenantId2)
                            .bind("$2", child.getClass().getName())
                            .bind("$3", parentTenantId2)
                            .bind("$4", parentTid)
                            .bind("$5", parentTid + "/" + child.id())
                            .bind("$6", child.id())
                            .bind("$7", tid)
                            .bind("$8", json)
                            .bind("$9", time)
                            .bind("$10", time)
                            .bind("$11", parentTenantId2)
                            .bind("$12", parentTid);
                    if (geoApplicable) {
                        GeoMetry geo = GeoData.class.cast(child).getGeom();
                        stmt.bind("$13", GeoUtil.convertToGeo(geo));
                    }
                    return stmt.execute();

                }).take(1).flatMap(it -> it.map((row, meta) -> row.get("tid", String.class)))
                .map(it -> (T) EntityUtil.with(child, it, tenantId, time, time)).next()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity already exist or parent not found"))));
    }


    private <T extends Entity> Mono<T> create(Connection connection, String tenantId, T entity) {
        checkGeoEntity(entity);
        validateObject(entity);
        String json = GsonCodec.encode(entity);
        String tid = RepoUtil.createUUID();
        long time = System.currentTimeMillis();
        boolean geoApplicable = table.enableGeo() && entity instanceof GeoData && GeoData.class.cast(entity).getGeom() != null;

        return Mono.just(connection).flatMapMany(conn -> {
                            Statement stmt = conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,uniqueId,id,tid,entity,createdTime,updatedTime" + (geoApplicable ? ",geom" : "") + ") values($1,$2,$3,$4,$5,$6::JSON,$7,$8" + (geoApplicable ? ",$9" : "") + ") on conflict (tenantId,resourceType,uniqueId) do nothing returning tid")
                                    .bind("$1", tenantId == null ? "default" : tenantId)
                                    .bind("$2", entity.getClass().getName())
                                    .bind("$3", entity.id())
                                    .bind("$4", entity.id())
                                    .bind("$5", tid)
                                    .bind("$6", json)
                                    .bind("$7", time)
                                    .bind("$8", time);
                            if (geoApplicable) {
                                GeoMetry geo = GeoData.class.cast(entity).getGeom();
                                stmt.bind("$9", GeoUtil.convertToGeo(geo));
                            }
                            return stmt.execute();
                        }
                ).take(1).flatMap(it -> it.map((row, meta) -> row.get("tid", String.class)))
                .map(it -> (T) EntityUtil.with(entity, it, tenantId, time, time)).next()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity already exist"))));
    }

    private <T extends Entity> Mono<T> createOrUpdate(Connection connection, String tenantId, T entity) {
        checkGeoEntity(entity);
        validateObject(entity);
        String json = GsonCodec.encode(entity);
        String tid = RepoUtil.createUUID();
        long time = System.currentTimeMillis();
        boolean geoApplicable = table.enableGeo() && entity instanceof GeoData && GeoData.class.cast(entity).getGeom() != null;
        return Mono.just(connection).flatMapMany(conn -> {
                    Statement stmt = conn.createStatement("insert into " + table.name() + " as tab(tenantId,resourceType,uniqueId,id,tid,entity,createdTime,updatedTime" + (geoApplicable ? ",geom" : "") + ") values($1,$2,$3,$4,$5,$6::JSON,$7,$8" + (geoApplicable ? ",$11" : "") + ") on conflict (tenantId,resourceType,uniqueId) do update set " +
                                    "entity = $9::JSON , updatedTime = $10" + (geoApplicable ? " , geom = $12" : "") + " returning tid,createdTime,updatedTime")
                            .bind("$1", tenantId == null ? "default" : tenantId)
                            .bind("$2", entity.getClass().getName())
                            .bind("$3", entity.id())
                            .bind("$4", entity.id())
                            .bind("$5", tid)
                            .bind("$6", json)
                            .bind("$7", time)
                            .bind("$8", time)
                            .bind("$9", json)
                            .bind("$10", time);
                    if (geoApplicable) {
                        GeoMetry geo = GeoData.class.cast(entity).getGeom();
                        stmt.bind("$11", GeoUtil.convertToGeo(geo));
                        stmt.bind("$12", GeoUtil.convertToGeo(geo));
                    }
                    return stmt.execute();
                }).take(1)
                .flatMap(it -> it.map((row, meta) -> {
                    return EntityUtil.with(entity
                            , row.get("tid", String.class), tenantId
                            , row.get("createdTime", Long.class)
                            , row.get("updatedTime", Long.class)
                    );
                }))
                .map(it -> (T) entity).next();
    }

    private <T extends Entity> Mono<T> delete(Connection connection, String tenantId, Class<T> resourceType, String id) {
        if (id.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("delete from " + table.name() + " as x where x.tenantId = $1 and x.resourceType = $2 and x.tid = $3 " +
                                    " and not exists( select 1 from " + table.name() + " as k where k.parentTenantId = x.tenantId and parentTid = x.tid )" +
                                    "returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                            .bind("$1", tenantId == null ? "default" : tenantId)
                            .bind("$2", resourceType.getName())
                            .bind("$3", id)
                            .execute()).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist or child entity attached to it"))));
        } else {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("delete from " + table.name() + " as x where x.tenantId = $1 and x.resourceType = $2 and x.id = $3 " +
                                    " and not exists (select 1 from " + table.name() + " as k where k.parentTenantId = x.tenantId and k.parentTid = x.tid )" +
                                    "returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                            .bind("$1", tenantId == null ? "default" : tenantId)
                            .bind("$2", resourceType.getName())
                            .bind("$3", id)
                            .execute()).take(1).flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist or child entity attached to it"))));
        }

    }

    private <T extends Entity> Mono<T> delete(Connection connection, String tenantId, String id) {
        if (id.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("delete from " + table.name() + " as x where x.tenantId = $1 and x.tid = $2 " +
                                    " and not exists( select 1 from " + table.name() + " as k where k.parentTenantId = x.tenantId and parentTid = x.tid )" +
                                    "returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                            .bind("$1", tenantId == null ? "default" : tenantId)
                            .bind("$2", id)
                            .execute()).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist or child entity attached to it"))));
        } else {
            return Mono.error(new RepoException("technical id needed"));
        }

    }

    private <T extends Entity> Mono<T> update(Connection connection, String tenantId, T entity, String id) {
        checkGeoEntity(entity);
        validateObject(entity);
        String json = GsonCodec.encode(entity);
        long time = System.currentTimeMillis();
        boolean geoApplicable = table.enableGeo() && entity instanceof GeoData && GeoData.class.cast(entity).getGeom() != null;
        if (id.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> {
                        Statement stmt = conn.createStatement("update " + table.name() + " set uniqueid=$1, id=$2, entity = $3::JSON , updatedTime = $4 " +
                                        (geoApplicable ? " , geom = $8" : "") +
                                        "where parenttid is null and tid= $5 and tenantId= $6 and resourceType=$7 returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                                .bind("$1", entity.id())
                                .bind("$2", entity.id())
                                .bind("$3", json)
                                .bind("$4", time)
                                .bind("$5", id)
                                .bind("$6", tenantId == null ? "default" : tenantId)
                                .bind("$7", entity.getClass().getName());
                        if (geoApplicable) {
                            GeoMetry geo = GeoData.class.cast(entity).getGeom();
                            stmt.bind("$8", GeoUtil.convertToGeo(geo));
                        }
                        return stmt.execute();
                    }).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist"))));

        } else {
            return Mono.just(connection).flatMapMany(conn -> {
                        Statement stmt = conn.createStatement("update " + table.name() + " set entity = $1::JSON , updatedTime = $2 " +
                                        (geoApplicable ? " , geom = $6" : "") +
                                        "where tid= $3 and tenantId= $4 and resourceType=$5 returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                                .bind("$1", json)
                                .bind("$2", time)
                                .bind("$3", id)
                                .bind("$4", tenantId == null ? "default" : tenantId)
                                .bind("$5", entity.getClass().getName());
                        if (geoApplicable) {
                            GeoMetry geo = GeoData.class.cast(entity).getGeom();
                            stmt.bind("$6", GeoUtil.convertToGeo(geo));
                        }
                        return stmt.execute();
                    }).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist"))));
        }

    }

    private <T extends Entity> Mono<T> convertToChild(Connection connection, String tenantId, String parentTid, String id, Class<T> resourceType) {
        long time = System.currentTimeMillis();
        if (id.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> {
                        Statement stmt = conn.createStatement("update " + table.name() + " set parenttid=$1, uniqueid=$2, updatedTime = $3 " +
                                        "where parenttid is null and tid= $4 and tenantId= $5 and resourceType=$6 returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                                .bind("$1", parentTid)
                                .bind("$2", parentTid + "/" + id)
                                .bind("$3", time)
                                .bind("$4", id)
                                .bind("$5", tenantId == null ? "default" : tenantId)
                                .bind("$6", resourceType.getName());
                        return stmt.execute();
                    }).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist"))));

        } else {
            return Mono.just(connection).flatMapMany(conn -> {
                        Statement stmt = conn.createStatement("update " + table.name() + " set parenttid = $1, uniqueid=$2, updatedTime = $3 " +
                                        "where parenttid is null and id= $4 and tenantId= $5 and resourceType=$6 returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                                .bind("$1", parentTid)
                                .bind("$2", parentTid + "/" + id)
                                .bind("$3", time)
                                .bind("$4", id)
                                .bind("$5", tenantId == null ? "default" : tenantId)
                                .bind("$6", resourceType.getName());
                        return stmt.execute();
                    }).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist"))));
        }

    }

    private <T extends Entity> Mono<T> updateChild(Connection connection, String parentTid, String tenantId, T child, String id) {
        Objects.requireNonNull(parentTid);
        checkGeoEntity(child);
        validateObject(child);
        String json = GsonCodec.encode(child);
        long time = System.currentTimeMillis();
        boolean geoApplicable = table.enableGeo() && child instanceof GeoData && GeoData.class.cast(child).getGeom() != null;
        if (id.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> {
                        Statement stmt = conn.createStatement("update " + table.name() + " set uniqueid=$1, id=$2, entity = $3::JSON , updatedTime = $4 " +
                                        (geoApplicable ? " , geom = $8" : "") +
                                        "where parenttid is not null and tid= $5 and tenantId= $6 and resourceType=$7 returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                                .bind("$1",parentTid + "/" + child.id())
                                .bind("$2", child.id())
                                .bind("$3", json)
                                .bind("$4", time)
                                .bind("$5", id)
                                .bind("$6", tenantId == null ? "default" : tenantId)
                                .bind("$7", child.getClass().getName());
                        if (geoApplicable) {
                            GeoMetry geo = GeoData.class.cast(child).getGeom();
                            stmt.bind("$8", GeoUtil.convertToGeo(geo));
                        }
                        return stmt.execute();
                    }).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist"))));

        } else {
            return Mono.just(connection).flatMapMany(conn -> {
                        Statement stmt = conn.createStatement("update " + table.name() + " set entity = $1::JSON , updatedTime = $2 " +
                                        (geoApplicable ? " , geom = $6" : "") +
                                        "where id= $3 and tenantId= $4 and resourceType=$5 returning tenantId,createdTime,updatedTime,tid,resourceType,entity")
                                .bind("$1", json)
                                .bind("$2", time)
                                .bind("$3", id)
                                .bind("$4", tenantId == null ? "default" : tenantId)
                                .bind("$5", child.getClass().getName());
                        if (geoApplicable) {
                            GeoMetry geo = GeoData.class.cast(child).getGeom();
                            stmt.bind("$6", GeoUtil.convertToGeo(geo));
                        }
                        return stmt.execute();
                    }).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) (createEntity(row)))).next()
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new RepoException("entity not exist"))));
        }
    }

    protected Mono<Postgres13ReactiveRepository> createTenantIfNotExist(String tenantId) {
        String _tenantId = tenantId == null ? "default" : tenantId;
        return Mono.justOrEmpty(tenants.get(_tenantId)).map(s -> this)
                .switchIfEmpty(Mono.defer(() -> createTenantTable(_tenantId).doOnNext(it -> tenants.put(_tenantId, _tenantId))));
    }

    private Mono<Postgres13ReactiveRepository> createTenantTable(String tenantId) {
        return executeMono(config.connectionFromPool(table.config(), tenantId), connection -> Mono.just(connection)
                .flatMapMany(conn -> conn.createStatement("create table if not exists " + table.name() + "_" + tenantId.replaceAll("-", "_") + " partition of " + table.name() + " for values in ('" + tenantId + "')").execute())
                .next())
                .doOnNext(s -> System.out.println("table " + table.name() + "_" + tenantId.replaceAll("-", "_") + " created"))
                .then(Mono.just(this));
    }

    private <T extends Entity> Mono<Postgres13ReactiveRepository> createEntityTable(String tableName) {
        String sql = "create table if not exists " + tableName + "(tenantId varchar not null,resourceType varchar not null," +
                "parentTenantId varchar," +
                "parentTid varchar," +
                "uniqueId varchar not null," +
                "id varchar not null," +
                "tid varchar not null," +
                "entity jsonb not null," +
                "createdTime bigint not null," +
                "updatedTime bigint not null,";
        if (table.enableGeo()) {
            sql += "geom geometry,";
        }
        sql += "primary key (tenantId,resourceType,uniqueId)) partition by LIST(tenantId)";

        String sql2 = sql;
        return executeMono(config.connectionFromPool(table.config(), null), conn -> Mono.just(conn)
                .flatMapMany(connection -> connection.createStatement(sql2).execute())
                .next())
                .doOnNext(s -> System.out.println("table " + tableName + " created"))
                .then(Mono.just(this));
    }

    @Override
    protected Mono<Void> initTables() {

        return createEntityTable(table.name()).then();
    }

//    private <T extends Entity> Mono<Postgres13ReactiveRepository> createAuditTable(String tableName) {
//        return executeMono(config.connectionFromPool(table.config(),null), conn -> Mono.just(conn)
//                .flatMapMany(connection -> connection.createStatement("create table if not exists " + tableName + "_Audit" + "(tenantId varchar not null,resourceType varchar not null," +
//                        "parentTenantId varchar,"+
//                        "parentTid varchar," +
//                        "id varchar not null," +
//                        "tid varchar not null," +
//                        "entity jsonb not null," +
//                        "createdDate bigint not null," +
//                        "updatedDate bigint not null," +
//                        "primary key (tenantId,resourceType,id)) partition by LIST(tenantId)").execute())
//                .next())
//                .doOnNext(s -> System.out.println("table " + tableName + "_Audit created"))
//                .then(Mono.just(this));
//    }
}
