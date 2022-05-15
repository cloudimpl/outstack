package com.cloudimpl.outstack.repo;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.repo.core.ReadOnlyReactiveRepository;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.util.Util;
import com.cloudimpl.rstack.dsl.restql.RestQLParser;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PostgresReadOnlyReactiveRepository implements ReadOnlyReactiveRepository {
    @Autowired
    protected ReactivePostgresConfig config;

    @Autowired
    @Qualifier("ioSchedular")
    protected Scheduler ioScheduler;

    protected  Table table;

    public PostgresReadOnlyReactiveRepository() {
        table = RepoUtil.getRepoMeta(this.getClass(),false);
    }

    protected void setTable(Table table)
    {
        this.table = table;
    }

    @Override
    public <T extends Entity> Mono<T> queryById(String tenantId, Class<T> resourceType, String id) {
        return executeMono(config.connectionFromPool(table.config(),tenantId), connection -> queryById(connection, tenantId, resourceType, id));
    }

    @Override
    public <T extends Entity> Flux<T> queryByType(String tenantId, Class<T> resourceType, QueryRequest request) {
        QueryRequest request2 = QueryRequest.builder()
                .query(request.getQuery().isEmpty() ? "_resourceType = '" + resourceType.getName() + "'" : " and _resourceType = '" + resourceType.getName() + "'")
                .orderBy(request.getOrderBy())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .mergeNonTenant(request.isMergeNonTenant())
                .build();
        return query(tenantId, request2);
    }

    @Override
    public <T extends Entity> Mono<ResultSet<T>> queryByTypeWithPagination(String tenantId, Class<T> resourceType, QueryRequest request) {
        QueryRequest request2 = QueryRequest.builder()
                .query(request.getQuery().isEmpty() ? "_resourceType = '" + resourceType.getName() + "'" : " and _resourceType = '" + resourceType.getName() + "'")
                .orderBy(request.getOrderBy())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .mergeNonTenant(request.isMergeNonTenant())
                .build();
        return queryWithPagination(tenantId, request2);
    }

    @Override
    public <T extends Entity> Flux<T> query(String tenantId, QueryRequest request) {
        return executeFlux(config.connectionFromPool(table.config(),tenantId), connection -> query(connection, tenantId, request));
    }

    @Override
    public <T extends Entity> Mono<ResultSet<T>> queryWithPagination(String tenantId, QueryRequest req) {
        return executeMono(config.connectionFromPool(table.config(),tenantId), connection -> queryWithPagination(connection, tenantId, req));
    }

    @Override
    public <T extends Entity> Flux<T> findChilds(String tenantId,String tid,String childTenantId,QueryRequest request)
    {
        return executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> findChilds(connection,tenantId,tid,childTenantId,request));
    }

    @Override
    public <T extends Entity> Flux<T> findChildsByType(String tenantId,String tid,String childTenantId,Class<T> resourceType,QueryRequest request)
    {
        QueryRequest request2 = QueryRequest.builder()
                .query(request.getQuery().isEmpty() ? "_resourceType = '" + resourceType.getName() + "'" : " and _resourceType = '" + resourceType.getName() + "'")
                .orderBy(request.getOrderBy())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .mergeNonTenant(request.isMergeNonTenant())
                .build();
        return findChilds(tenantId,tid,childTenantId,request2);
    }

    @Override
    public <T extends Entity> Flux<T> findChilds(String tenantId,String tid,QueryRequest request)
    {
        return findChilds(tenantId,tid,tenantId,request);
    }

    @Override
    public <T extends Entity> Flux<T> findChildsByType(String tenantId,String tid,Class<T> resourceType,QueryRequest request)
    {
        return findChildsByType(tenantId,tid,tenantId,resourceType,request);
    }

    private <T extends Entity> Flux<T> query(Connection connection, String tenantId, QueryRequest request) {
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "" : sqlNode.eval(RestQLParser.parse(request.getQuery()));
        String orderBy = request.getOrderBy().isEmpty() ? "createdTime" : (new PostgresSqlNode().eval(RestQLParser.parseOrderBy(request.getOrderBy())));

        String tenantId2 = tenantId == null ? "default" : tenantId;
        String sqlPart1 = "select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() + " where (tenantId = $1 or tenantId = $2)" + (!whereClause.isEmpty() ? " and (" + whereClause + ")" : "");
        String sql = sqlPart1 + " order by " + orderBy + " limit " + request.getPageSize() + " offset " + (request.getPageNum() * request.getPageSize());
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1", tenantId2)
                        .bind("$2", request.isMergeNonTenant() ? "default" : tenantId2)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEntity(row)));
    }

    private <T extends Entity> Flux<T> findChilds(Connection connection,String tenantId,String tid,String childTenantId,QueryRequest request){
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "tenantId = $1 and parentTenantId = $2 and parentTid = $3" : sqlNode.eval(RestQLParser.parse(request.getQuery() + " and ( _tenantId = $1 and _parentTenantId = $2 and _parentTid = $3)"));
        String orderBy = request.getOrderBy().isEmpty() ? "createdTime" : (new PostgresSqlNode().eval(RestQLParser.parseOrderBy(request.getOrderBy())));
        String tenantId2 = tenantId == null ? "default" : tenantId;
        String childTenantId2 = childTenantId == null ? "default" : childTenantId;
        String sqlPart1 = "select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() + " where " + whereClause;
        String sql = sqlPart1 + " order by " + orderBy + " limit " + request.getPageSize() + " offset " + (request.getPageNum() * request.getPageSize());
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1",childTenantId2)
                        .bind("$2", tenantId2)
                        .bind("$3", tid)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEntity(row)));
    }

    private <T extends Entity> Mono<ResultSet<T>> queryWithPagination(Connection connection, String tenantId, QueryRequest request) {
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "" : sqlNode.eval(RestQLParser.parse(request.getQuery()));
        return getCount(connection, tenantId, whereClause, request.isMergeNonTenant()).flatMap(total -> query(connection, tenantId, request).collectList().map(list -> toResultSet(total, list, request)));
    }

    private <T extends Entity> ResultSet<T> toResultSet(long total, List<Entity> list, QueryRequest req) {
        return new com.cloudimpl.outstack.runtime.ResultSet(total, (int) Math.ceil(((double) total) / req.getPageSize()), req.getPageNum(), list);
    }

    public Mono<Long> getCount(Connection connection, String tenantId, String whereClause, boolean mergeNonTenant) {
        String tenantId2 = tenantId == null ? "default" : tenantId;
        String sql = "select count(*) as total from " + table.name() + " where (tenantId = $1 or tenantId = $2) " + (!whereClause.isEmpty() ? " and (" + whereClause + ")" : "");
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1", tenantId2)
                        .bind("$2", mergeNonTenant ? "default" : tenantId2)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (Long) row.get("total", Long.class))).next();
    }

    protected <T extends Entity> Mono<T> queryById(Connection connection, String tenantId, Class<T> resourceType, String id) {
        if (id.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() + " where tenantId = $1 and resourceType = $2 and tid = $3")
                    .bind("$1", tenantId == null ? "default" : tenantId)
                    .bind("$2", resourceType.getName())
                    .bind("$3", id)
                    .execute()).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) createEntity(row))).next();

        } else {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("select  tid,resourceType,entity from " + table.name() + " where tenantId = $1 and resourceType = $2 and id = $3")
                    .bind("$1", tenantId == null ? "default" : tenantId)
                    .bind("$2", resourceType.getName())
                    .bind("$3", id)
                    .execute()).take(1).flatMap(it -> it.map((row, meta) -> (T)createEntity(row))).next();

        }
    }

    protected <T extends Entity> T createEntity(Row row) {
        T entity = ((T) GsonCodec.decode(Util.classForName(row.get("resourceType", String.class)), row.get("entity", Json.class).asString()))
                .withTid(row.get("tid", String.class));

        entity.getMeta()
                .withCreatedTime(row.get("createdTime", Long.class))
                .withUpdatedTime(row.get("updatedTime", Long.class))
                .withTenantId(row.get("tenantId", String.class));
        return entity;
    }
//    protected <T extends Entity> Flux<T> queryByType(Connection connection,String tenantId,Class<T> resourceType){
//        return Mono.just(connection).flatMapMany(conn -> conn.createStatement("select  tid,resourceType,entity from " + table.name() + " where tenantId = $1 and resourceType = $2")
//                .bind("$1", tenantId == null ? "default" : tenantId)
//                .bind("$2", resourceType.getName())
//                .execute()).flatMap(it -> it.map((row, meta) -> ((T) GsonCodec.decode(Util.classForName(row.get("resourceType", String.class)), row.get("entity", Json.class).asString())).withTid(row.get("tid", String.class))));
//    }

    protected <T extends PostgresReadOnlyReactiveRepository> Mono<T> initTables() {
        return Mono.just((T) this);
    }

    protected <T extends PostgresReadOnlyReactiveRepository> Mono<T> createTenantIfNotExist(String tenantId) {
        return Mono.just((T) this);
    }

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
