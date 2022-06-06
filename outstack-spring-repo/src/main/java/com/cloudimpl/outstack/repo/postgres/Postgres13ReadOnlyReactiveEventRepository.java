package com.cloudimpl.outstack.repo.postgres;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.repo.Event;
import com.cloudimpl.outstack.repo.EventUtil;
import com.cloudimpl.outstack.repo.QueryRequest;
import com.cloudimpl.outstack.repo.core.ReadOnlyReactiveEventRepository;
import com.cloudimpl.outstack.repo.core.Repository;
import com.cloudimpl.outstack.runtime.util.Util;
import com.cloudimpl.rstack.dsl.restql.RestQLParser;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Postgres13ReadOnlyReactiveEventRepository extends Repository implements ReadOnlyReactiveEventRepository {

    @Override
    public <T extends Event> Flux<T> queryEvents(String tenantId,QueryRequest queryRequest) {
        return executeFlux(config.connectionFromPool(table.config(),tenantId),connection -> queryEvents(connection,tenantId,queryRequest));
    }

    private <T extends Event> Flux<T> queryEvents(Connection connection,String tenantId,QueryRequest queryRequest)
    {
        String tenantId2 = tenantId == null ? "default" : tenantId;
        PostgresSqlNode sqlNode = new PostgresSqlNode();
        String whereClause = queryRequest.getQuery().isEmpty() ? "tenantId = $1" : sqlNode.eval(RestQLParser.parse(queryRequest.getQuery() + " and tenantId = $1"));
        String orderBy = queryRequest.getOrderBy().isEmpty() ? "createdTime" : (new PostgresSqlNode().eval(RestQLParser.parseOrderBy(queryRequest.getOrderBy())));


        String sql = "select * from "+table.name() + "where "+whereClause + " order by "+orderBy + " limit "+
                queryRequest.getPageSize() + " offset "+ (queryRequest.getPageNum() * queryRequest.getPageSize());

        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1",tenantId2)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEvent(row)));
    }

    @Override
    public <T extends Event> Flux<T> queryEvents(String tenantId,Class<T> eventType, QueryRequest queryRequest) {
        QueryRequest queryRequest2 = QueryRequest.builder().query(queryRequest.getQuery().isEmpty() ? "_resourceType = "+eventType.getName() : queryRequest.getQuery() + " and _resourceType = "+eventType.getName())
                .orderBy(queryRequest.getOrderBy())
                .pageNum(queryRequest.getPageNum())
                .pageSize(queryRequest.getPageSize()).build();
          return queryEvents(tenantId,queryRequest);
    }

    @Override
    public <T extends Event> Mono<T> queryEventById(String tenantId,Class<T> eventType, String id) {
        return executeMono(config.connectionFromPool(table.config(),tenantId),connection -> queryEventById(connection,tenantId,eventType,id));
    }

    private <T extends Event> Mono<T> queryEventById(Connection connection,String tenantId,Class<T> eventType,String id){
        String tenantId2 = tenantId == null ? "default" : tenantId;

        String sql = " select * from "+table.name() + " where tenantId = $1 and resourceType = $2 and eventId = $3";
        return Mono.just(connection).flatMapMany(conn -> conn.createStatement(sql)
                        .bind("$1",tenantId2)
                        .bind("$2",eventType.getName())
                        .bind("$3",id)
                        .execute())
                .flatMap(it -> it.map((row, meta) -> (T) createEvent(row))).next();
    }

    @Override
    protected Mono<Void> initTables() {
        return Mono.just(this).then();
    }

    private <T extends Event> T createEvent(Row row)
    {
        T event = GsonCodec.decode(Util.classForName(row.get("resourceType",String.class)),row.get("event", Json.class).asString());
        String tenantId =row.get("tenantId",String.class);
        return EventUtil.with(event,tenantId.equals("default")? null : tenantId,row.get("eventId",String.class),row.get("createdTime",Long.class)
        ,row.get("updatedTime",Long.class));
    }
}
