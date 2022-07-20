package com.cloudimpl.outstack.repo.postgres;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.repo.Entity;
import com.cloudimpl.outstack.repo.EntityUtil;
import com.cloudimpl.outstack.repo.QueryRequest;
import com.cloudimpl.outstack.repo.RepoException;
import com.cloudimpl.outstack.repo.core.ReadOnlyReactiveRepository;
import com.cloudimpl.outstack.repo.core.Repository;
import com.cloudimpl.outstack.repo.core.geo.GeoUtil;
import com.cloudimpl.outstack.repo.core.geo.Point;
import com.cloudimpl.outstack.repo.core.geo.Polygon;
import com.cloudimpl.outstack.repo.core.geo.ReactiveGeoQueryRepository;
import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.util.Util;
import com.cloudimpl.rstack.dsl.restql.RestQLParser;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class Postgres13ReadOnlyReactiveRepository extends Repository implements ReadOnlyReactiveRepository, ReactiveGeoQueryRepository {

    @Override
    public <T extends Entity> Mono<T> queryById(String tenantId, Class<T> resourceType, String id) {
        return executeMono(config.connectionFromPool(table.config(),tenantId), connection -> queryById(connection, tenantId, resourceType, id));
    }

    @Override
    public <T extends Entity> Mono<T> queryById(String tenantId, String tid) {
        return executeMono(config.connectionFromPool(table.config(),tenantId), connection -> queryById(connection, tenantId, tid));
    }

    @Override
    public <T extends Entity> Flux<T> queryByType(String tenantId, Class<T> resourceType, QueryRequest request) {
        QueryRequest request2 = QueryRequest.builder()
                .query(request.getQuery().isEmpty() ? "_resourceType = '" + resourceType.getName() + "'" :request.getQuery() + " and _resourceType = '" + resourceType.getName() + "'")
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
                .query(request.getQuery().isEmpty() ? "_resourceType = '" + resourceType.getName() + "'" :request.getQuery() + " and _resourceType = '" + resourceType.getName() + "'")
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

    @Override
    public <T extends Entity> Flux<T> findParentGraphSearch(String tenantId,Class<? extends Entity> resourceType, String childId, QueryRequest request) {
        return executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> findParentGraphSearch(connection,tenantId,resourceType,childId,request));
    }

    @Override
    public <T extends Entity> Flux<T> findChildGraphSearch(String tenantId, Class<? extends Entity> resourceType, String parentId, QueryRequest request) {
        return executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> findChildGraphSearch(connection,tenantId,resourceType,parentId,request));
    }

    private <T extends Entity> Flux<T> findChildGraphSearch(Connection connection,String tenantId, Class<? extends Entity> resourceType, String parentId, QueryRequest request) {
        String sql = "with recursive node_cte(tenantId,resourceType,parentTenantId,parentTid,id,tid,entity,createdTime,updatedTime) as (" +
                "select tn.tenantId,tn.resourceType,tn.parentTenantId,tn.parentTid,tn.id,tn.tid,tn.entity,tn.createdTime,tn.updatedTime" +
                " from "+table.name()+" as tn where tn.tenantId = $1 and tn."+((parentId.startsWith("id-"))?"tid":"id") + " = $2 and resourceType = $3" +
                " union all " +
                "select c.tenantId,c.resourceType,c.parentTenantId,c.parentTid,c.id,c.tid,c.entity,c.createdTime,c.updatedTime from node_cte as p , "+table.name()+" as c " +
                " where p.tid = c.parentTid )";
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "" : sqlNode.eval(RestQLParser.parse(request.getQuery()));
        String orderBy = request.getOrderBy().isEmpty() ? "createdTime" : (new PostgresSqlNode().eval(RestQLParser.parseOrderBy(request.getOrderBy())));
        String tenantId2 = tenantId == null ? "default" : tenantId;

        String filter = "not(n.tenantId = $4 and n.resourceType = $5 and n."+(parentId.startsWith("id-")?"tid":"id") + " = $6)";
        sql += " select * from node_cte as n where "+ (whereClause.isEmpty()? filter : whereClause + " and ("+filter+")") ;
        sql += " order by "+orderBy + " limit "+request.getPageSize() + " offset "+ (request.getPageNum() * request.getPageSize());

        String sql2 = sql;
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql2)
                        .bind("$1", tenantId2)
                        .bind("$2", parentId)
                        .bind("$3",resourceType.getName())
                        .bind("$4", tenantId2)
                        .bind("$5", resourceType.getName())
                        .bind("$6",parentId)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEntity(row)));
    }

    private <T extends Entity> Flux<T> findParentGraphSearch(Connection connection, String tenantId,Class<? extends Entity> resourceType, String childId, QueryRequest request) {
        String sql = "with recursive node_cte(tenantId,resourceType,parentTenantId,parentTid,id,tid,entity,createdTime,updatedTime) as (" +
                "select tn.tenantId,tn.resourceType,tn.parentTenantId,tn.parentTid,tn.id,tn.tid,tn.entity,tn.createdTime,tn.updatedTime" +
                " from "+table.name()+" as tn where tn.tenantId = $1 and tn."+((childId.startsWith("id-"))?"tid":"id") + " = $2 and resourceType = $3" +
                " union all " +
                "select c.tenantId,c.resourceType,c.parentTenantId,c.parentTid,c.id,c.tid,c.entity,c.createdTime,c.updatedTime from node_cte as p , "+table.name()+" as c " +
                " where p.parentTid = c.tid )";
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "" : sqlNode.eval(RestQLParser.parse(request.getQuery()));
        String orderBy = request.getOrderBy().isEmpty() ? "createdTime" : (new PostgresSqlNode().eval(RestQLParser.parseOrderBy(request.getOrderBy())));
        String tenantId2 = tenantId == null ? "default" : tenantId;

        String filter = "not(n.tenantId = $4 and n.resourceType = $5 and n."+(childId.startsWith("id-")?"tid":"id") + " = $6)";
        sql += " select * from node_cte as n where "+ (whereClause.isEmpty()? filter : whereClause + " and ("+filter+")") ;
        sql += " order by "+orderBy + " limit "+request.getPageSize() + " offset "+ (request.getPageNum() * request.getPageSize());

        String sql2 = sql;
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql2)
                        .bind("$1", tenantId2)
                        .bind("$2", childId)
                        .bind("$3",resourceType.getName())
                        .bind("$4", tenantId2)
                        .bind("$5", resourceType.getName())
                        .bind("$6",childId)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEntity(row)));
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

    @Override
    public <T extends Entity> Mono<ResultSet> doPagination(Flux<T> flux, QueryRequest req)
    {
        return flux.collectList()
                .map(list->new ResultSet((long)list.size(),(int) Math.ceil(((double) list.size()) / req.getPageSize()),req.getPageNum(),list.stream().skip(req.getPageNum() * req.getPageSize()).limit(req.getPageSize()).collect(Collectors.toList())));
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
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() + " where tenantId = $1 and resourceType = $2 and id = $3")
                    .bind("$1", tenantId == null ? "default" : tenantId)
                    .bind("$2", resourceType.getName())
                    .bind("$3", id)
                    .execute()).take(1).flatMap(it -> it.map((row, meta) -> (T)createEntity(row))).next();

        }
    }

    protected <T extends Entity> Mono<T> queryById(Connection connection, String tenantId, String tid) {
        if (tid.startsWith("id-")) {
            return Mono.just(connection).flatMapMany(conn -> conn.createStatement("select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() + " where tenantId = $1 and tid = $2")
                            .bind("$1", tenantId == null ? "default" : tenantId)
                            .bind("$2", tid)
                            .execute()).take(1)
                    .flatMap(it -> it.map((row, meta) -> (T) createEntity(row))).next();

        } else {
            return Mono.error(new RepoException("technical id needed"));
        }
    }

    protected <T extends Entity> T createEntity(Row row) {
        T entity = ((T) GsonCodec.decode(Util.classForName(row.get("resourceType", String.class)), row.get("entity", Json.class).asString()));

        EntityUtil.withTid(entity,row.get("tid", String.class));
        EntityUtil.withCreatedTime(entity.getMeta(), row.get("createdTime", Long.class));
        EntityUtil.withUpdatedTime(entity.getMeta(),row.get("updatedTime", Long.class));
        EntityUtil.withTenantId(entity.getMeta(),row.get("tenantId", String.class));
        return entity;
    }
//    protected <T extends Entity> Flux<T> queryByType(Connection connection,String tenantId,Class<T> resourceType){
//        return Mono.just(connection).flatMapMany(conn -> conn.createStatement("select  tid,resourceType,entity from " + table.name() + " where tenantId = $1 and resourceType = $2")
//                .bind("$1", tenantId == null ? "default" : tenantId)
//                .bind("$2", resourceType.getName())
//                .execute()).flatMap(it -> it.map((row, meta) -> ((T) GsonCodec.decode(Util.classForName(row.get("resourceType", String.class)), row.get("entity", Json.class).asString())).withTid(row.get("tid", String.class))));
//    }


    @Override
    protected Mono<Void> initTables() {
        return Mono.just(this).then();
    }

    @Override
    public <T> Flux<T> findEntitiesWithinBoundaryWithType(String tenantId, Class<T> type, Polygon polygon, QueryRequest request) {
        QueryRequest request2 = QueryRequest.builder().query(request.getQuery().isEmpty()?" _resourceType = '"+type.getName():request.getQuery() + "' and _resourceType = '"+type.getName()+"'")
                .orderBy(request.getOrderBy())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .build();
        return findEntitiesWithinBoundary(tenantId,polygon,request2);
    }

    @Override
    public <T> Flux<T> findEntitiesWithinBoundary(String tenantId, Polygon polygon, QueryRequest request) {
        return executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> findEntitiesWithinBoundary(connection,tenantId,polygon
        ,request));
    }

    private <T> Flux<T> findEntitiesWithinBoundary(Connection connection,String tenantId,Polygon polygon,QueryRequest request){
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "" : sqlNode.eval(RestQLParser.parse(request.getQuery()));
        String orderBy = request.getOrderBy().isEmpty() ? "createdTime" : (new PostgresSqlNode().eval(RestQLParser.parseOrderBy(request.getOrderBy())));
        String tenantId2 = tenantId == null ? "default" : tenantId;

        String sql = "select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() +
                "where ST_within(geom::geometry, ST_GeomFromText($1)) and tenantId = $2"+(whereClause.isEmpty()?"":" and ".concat(whereClause))
        +" order by " + orderBy + " limit " + request.getPageSize() + " offset " + (request.getPageNum() * request.getPageSize());
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1", GeoUtil.convertToGeo(polygon))
                        .bind("$2", tenantId2)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEntity(row)));
    }

    @Override
    public <T> Flux<T> findClosestBoundEntitiesForPoint(String tenantId, Point point, QueryRequest request){
        return executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> findClosestBoundEntitiesForPoint(connection,tenantId,point
                ,request));
    }

    private <T> Flux<T> findClosestBoundEntitiesForPoint(Connection connection,String tenantId, Point point, QueryRequest request) {
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = request.getQuery().isEmpty() ? "" : sqlNode.eval(RestQLParser.parse(request.getQuery()));
        String orderBy = request.getOrderBy().isEmpty() ? "ST_AREA(geom) , createdTime" : "ST_AREA(geom) , ".concat(new PostgresSqlNode().eval(RestQLParser.parseOrderBy(request.getOrderBy())));
        String tenantId2 = tenantId == null ? "default" : tenantId;

        String sql = "select  tenantId,createdTime,updatedTime,tid,resourceType,entity from " + table.name() +
                "where ST_within(ST_GeomFromText($1), geom::geometry) and tenantId = $2"+(whereClause.isEmpty()?"":" and ".concat(whereClause))
                +" order by " + orderBy + " limit " + request.getPageSize() + " offset " + (request.getPageNum() * request.getPageSize());
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1", GeoUtil.convertToGeo(point))
                        .bind("$2", tenantId2)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEntity(row)));
    }

    @Override
    public <T> Flux<T> findClosestBoundEntitiesForPointWithType(String tenantId, Class<T> type, Point point, QueryRequest request) {
        QueryRequest request2 = QueryRequest.builder().query(request.getQuery().isEmpty()?" _resourceType = '"+type.getName()+"'": request.getQuery().concat(" and _resourceType = '"+type.getName()+"'"))
                .orderBy(request.getOrderBy())
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .build();
        return findClosestBoundEntitiesForPoint(tenantId,point,request2);
    }
}
