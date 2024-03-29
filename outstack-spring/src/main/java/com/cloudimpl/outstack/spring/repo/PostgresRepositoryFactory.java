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
package com.cloudimpl.outstack.spring.repo;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.core.ComponentProvider;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.rstack.dsl.restql.RestQLNode;
import com.cloudimpl.rstack.dsl.restql.RestQLParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.postgresql.util.PGobject;

/**
 *
 * @author nuwan
 */
@Slf4j
public class PostgresRepositoryFactory implements EventRepositoryFactory {

    private final ResourceHelper helper;
    private ComponentProvider.ProviderConfigs providerConfig;
    private HikariDataSource ds;

    private HikariConfig config = new HikariConfig();
    private Map<String, String> tenantMap = new ConcurrentHashMap<>();

    public PostgresRepositoryFactory(ResourceHelper helper, ComponentProvider.ProviderConfigs providerConfig) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        this.helper = helper;
        this.providerConfig = providerConfig;
        config.setJdbcUrl(providerConfig.getOption("jdbcUrl").get());
        config.setUsername(providerConfig.getOption("username").get());
        config.setPassword(providerConfig.getOption("password").get());
        System.out.println(providerConfig.getOption("jdbcUrl").get() + "- " + providerConfig.getOption("username").get() + "-" + providerConfig.getOption("password").get());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setAutoCommit(false);
        ds = new HikariDataSource(config);
    }

    private Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    private void createTenantIfNotExist(String tableName, String tenantId) {
        tenantMap.computeIfAbsent(tableName + ":" + tenantId, tid -> createTenantTable(tableName, tenantId));
    }

    public void execute(List<Function<Connection, Integer>> consumer) {
        try (Connection conn = getConnection()) {
            for (Function<Connection, Integer> command : consumer) {
                int ret = command.apply(conn);
                if (ret == 0) {
                    conn.rollback();
                    throw new PostgresException(new ConcurrentException("concurrent modification", null));
                }
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    public int createEntityTable(Connection conn, String tableName) {
        try (PreparedStatement stmt = conn.prepareStatement("create table if not exists " + tableName + " (tenantId varchar,brn varchar ,rootEntityType varchar ,rootId varchar , entityType varchar,entityId varchar,json json,lastseq bigint,timestamp bigint,primary key(tenantId,brn)) partition by LIST(tenantId)")) {
            boolean ok = stmt.execute();
            conn.commit();
            log.info("creating entity table {} executed , ret {}", tableName, ok);
            return 1;
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    public int createEventTable(Connection conn, String tableName) {
        tableName = tableName + "Events";
        try (PreparedStatement stmt = conn.prepareStatement("create table if not exists " + tableName + " (tenantId varchar,trn varchar,eventOwner varchar,eventOwnerId varchar,eventType varchar,eventSeq bigint,json json,timestamp bigint,primary key(tenantId,trn,eventSeq)) partition by LIST(tenantId)")) {
            boolean ok = stmt.execute();
            conn.commit();
            log.info("creating events table {} executed , ret {}", tableName, ok);
            return 1;
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    public <T> T executeQuery(Function<Connection, T> queryHandler) {
        try (Connection conn = getConnection()) {
            return queryHandler.apply(conn);
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    private String createTenantTable(String table, String tenantId) {
        log.info("creating tenant {} for table {} if not exist", tenantId, table);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("create table if not exists " + table + "_" + tenantId.replaceAll("-", "_") + " partition of " + table + " for values in ('" + tenantId + "')")) {
            boolean ok = stmt.execute();
            conn.commit();
            log.info("creating tenant {} for table {} if not exist completed. {}", tenantId, table, ok);
            return tenantId;
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int insertEntity(Connection conn, String tableName, String tenantId, String rootEntityType, String rootId, String brn, String entityType, String entityId, String json, long lastSeq) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("insert into " + tableName + " (tenantId,brn,rootEntityType,rootId,entityType,entityId,json,lastseq,timestamp) values(?,?,?,?,?,?,?,?,?)")) {
            stmt.setString(1, tenantId);
            stmt.setString(2, rootEntityType);
            stmt.setString(3, rootId);
            stmt.setString(4, brn);
            stmt.setString(5, entityType);
            stmt.setString(6, entityId);
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            pGobject.setValue(json);
            stmt.setObject(7, pGobject);
            stmt.setLong(8, lastSeq);
            stmt.setLong(9, System.currentTimeMillis());
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int deleteChildEntityByRootId(Connection conn, String tableName, String tenantId, String rootEntityType, String rootId) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("delete from " + tableName + " where tenantId = ? and rootEntityType = ? and rootId = ?")) {
            stmt.setString(1, tenantId);
            stmt.setString(2, rootEntityType);
            stmt.setString(3, rootId);
            stmt.executeUpdate();
            return 1;
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int deleteEventsByRootId(Connection conn, String tableName, String tenantId, String rootId) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("delete from " + tableName + " where tenantId = ? and json->>'_rootId' = ?")) {
            stmt.setString(1, tenantId);
            stmt.setString(2, rootId);
            stmt.executeUpdate();
            return 1;
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int insertCheckpoint(Connection conn, String tableName, String tenantId, String brn, String entityType, String entityId, String json, long lastSeq) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("insert into " + tableName + " (tenantId,brn,entityType,entityId,json,lastseq,timestamp) values(?,?,?,?,?,?,?) on conflict (tenantId,brn) do update set json = ? , timestamp  = ? , lastseq = ?")) {
            stmt.setString(1, tenantId);
            stmt.setString(2, brn);
            stmt.setString(3, entityType);
            stmt.setString(4, entityId);
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            pGobject.setValue(json);
            stmt.setObject(5, pGobject);
            stmt.setLong(6, lastSeq);
            stmt.setLong(7, System.currentTimeMillis());
            stmt.setObject(8, pGobject);
            stmt.setLong(9, System.currentTimeMillis());
            stmt.setLong(10, lastSeq);
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int insertEvent(Connection conn, String tableName, String tenantId, String trn, String eventOwner, String eventOwnerId, String eventType, long eventSeq, String json) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("insert into " + tableName + " (tenantId,trn,eventOwner,eventOwnerId,eventType,eventSeq,json,timestamp) values(?,?,?,?,?,?,?,?) ")) {
            stmt.setString(1, tenantId);
            stmt.setString(2, trn);
            stmt.setString(3, eventOwner);
            stmt.setString(4, eventOwnerId);
            stmt.setString(5, eventType);
            stmt.setLong(6, eventSeq);
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            pGobject.setValue(json);
            stmt.setObject(7, pGobject);
            stmt.setLong(8, System.currentTimeMillis());
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int deleteEventsByTrn(Connection conn, String tableName, String tenantId, String trn, String eventOwner, String eventOwnerId) {
        createTenantIfNotExist(tableName, tenantId);
        String sql = "delete from " + tableName + " where tenantId = ? and trn = ? and eventOwner = ? and eventOwnerId = ?";
        log.info("deleteEventsByTrn : " + sql);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenantId);
            stmt.setString(2, trn);
            stmt.setString(3, eventOwner);
            stmt.setString(4, eventOwnerId);
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected com.cloudimpl.outstack.runtime.ResultSet<String> getEvents(Connection conn, String tableName, String tenantId, String trn, String eventOwner, String eventOwnerId, Query.PagingRequest paging) {
        createTenantIfNotExist(tableName, tenantId);
        String filterSql = null;
        String orderBySql = null;
        String filter = paging.getSearchFilter();
        String orderBy = paging.getOrderBy();
        String includeEventOwners = paging.getParams().get("includeOwners");
        if(includeEventOwners == null){
            includeEventOwners = eventOwner;
        }else {
            eventOwnerId = "*";
        }

        if (filter != null) {
            RestQLNode qlNode = RestQLParser.parse(filter);
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            filterSql = sqlNode.eval(qlNode);
        }

        if (orderBy != null) {
            RestQLNode qlNode = RestQLParser.parseOrderBy(orderBy);
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            orderBySql = sqlNode.eval(qlNode);
        } else {
            orderBy = "eventSeq desc";
        }

        long total = getTotalEventsCount(conn, tableName, tenantId, trn, includeEventOwners, eventOwnerId, filter);
        String sqlQuery = "select json from " + tableName + " where trn = ? and tenantId = ? and eventOwner = ANY(?) " + (eventOwnerId.equals("*")? "" : " and eventOwnerId = ? ") + (filterSql != null ? " and " + filterSql : "") + (orderBySql != null ? " order By " + orderBySql : "") + (orderBy != null ? " limit " + paging.pageSize() + " offset " + (paging.pageNum() * paging.pageSize()) : "");
        log.info("getEvents : " + sqlQuery);
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setString(1, trn);
            stmt.setString(2, tenantId);
            stmt.setArray(3, conn.createArrayOf("text", Arrays.stream(includeEventOwners.split(",")).toArray()));
            if(!eventOwnerId.equals("*")) {
                stmt.setString(4, eventOwnerId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("fetch size : " + rs.getFetchSize());
                List<String> list = new LinkedList<>();
                while (rs.next()) {
                    list.add(rs.getString("json"));
                }
                return new com.cloudimpl.outstack.runtime.ResultSet<>(total, (int) Math.ceil(((double) total) / paging.pageSize()), paging.pageNum(), list);
            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected long getTotalEventsCount(Connection conn, String tableName, String tenantId, String trn, String eventOwner, String eventOwnerId, String filter) {
        createTenantIfNotExist(tableName, tenantId);
        String filterSql = null;
        if (filter != null) {
            RestQLNode qlNode = RestQLParser.parse(filter);
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            filterSql = sqlNode.eval(qlNode);
        }
        String sqlQuery = "select count(*) as totalCount  from " + tableName + " where trn = ? and tenantId = ? and eventOwner = ANY(?) " + (eventOwnerId.equals("*")? "" : " and eventOwnerId = ? ") + (filterSql != null ? " and " + filterSql : "");
        try (PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            stmt.setString(1, trn);
            stmt.setString(2, tenantId);
            stmt.setArray(3, conn.createArrayOf("text", Arrays.stream(eventOwner.split(",")).toArray()));
            if(!eventOwnerId.equals("*")) {
                stmt.setString(4, eventOwnerId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("fetch size : " + rs.getFetchSize());
                if (rs.next()) {
                    return rs.getLong("totalCount");
                } else {
                    return 0;
                }
            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int updateEntity(Connection conn, String tableName, String tenantId, String brn, String entityType, String entityId, String json, long lastSeq, long updatedSeq) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("update " + tableName + " set brn = ?, json = ? , lastseq = ?  ,timestamp = ?  where tenantId = ? and lastSeq = ? and entityType = ? and entityId = ?")) {
            stmt.setString(1, brn);
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            pGobject.setValue(json);
            stmt.setObject(2, pGobject);
            stmt.setLong(3, updatedSeq);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setString(5, tenantId);
            stmt.setLong(6, lastSeq);
            stmt.setString(7, entityType);
            stmt.setString(8, entityId);
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected int deleteEntity(Connection conn, String tableName, String tenantId, String brn, String entityId) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("delete from " + tableName + " where tenantId = ? and brn = ? and entityId = ?")) {
            stmt.setString(1, tenantId);
            stmt.setString(2, brn);
            stmt.setString(3, entityId);
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected Optional<String> getEntityByBrn(Connection conn, String tableName, String brn, String tenantId) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("select json from " + tableName + " where brn = ? and tenantId = ?")) {
            stmt.setString(1, brn);
            stmt.setString(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("json"));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected Optional<String> getEntityByBrn(Connection conn, String tableName, String brn, String tenantId, boolean isIgnoreCase) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = isIgnoreCase ? conn.prepareStatement("select json from " + tableName + " where LOWER(brn) = LOWER(?) and tenantId = ?") :
                conn.prepareStatement("select json from " + tableName + " where brn = ? and tenantId = ?")) {
            stmt.setString(1, brn);
            stmt.setString(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("json"));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected Optional<String> getEntityByTrn(Connection conn, String tableName, String entityType, String id, String tenantId) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("select json from " + tableName + " where entityType = ? and entityId = ? and tenantId = ?")) {
            stmt.setString(1, entityType);
            stmt.setString(2, id);
            stmt.setString(3, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("json"));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected com.cloudimpl.outstack.runtime.ResultSet<String> getRootEntityByType(Connection conn, String tableName, String rootEntityType, List<String> tenantIds, String filter, String orderBy, int pageNum, int pageSize) {
        orderBy = orderBy == null ? "_meta.createdDate" : orderBy;
        String filterSql = null;
        String orderBySql = null;
        if (filter != null) {
            RestQLNode qlNode = RestQLParser.parse(filter);
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            filterSql = sqlNode.eval(qlNode);
        }

        if (orderBy != null) {
            RestQLNode qlNode = RestQLParser.parseOrderBy(orderBy);
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            orderBySql = sqlNode.eval(qlNode);
        }

        tenantIds.forEach(tenantId -> createTenantIfNotExist(tableName, tenantId));
        long total = getRootEntityByTypeCount(conn, tableName, rootEntityType, tenantIds, filter);
        String tenantQuery = "(" + tenantIds.stream().map(t -> "tenantId = ?").collect(Collectors.joining(" or ")) + ")";
        String sql = "select json from " + tableName + " where rootEntityType = ?  and entityType = ? and " + tenantQuery + (filterSql != null ? " and " + filterSql : "") + (orderBySql != null ? " order By " + orderBySql : "") + (orderBy != null ? " limit " + pageSize + " offset " + (pageNum * pageSize) : "");
        log.info("getRootEntityByType : " + sql);
        List<String> list = new LinkedList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rootEntityType);
            stmt.setString(2, rootEntityType);
            int i = 3;
            for (String tenantId : tenantIds) {
                stmt.setString(i++, tenantId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("json"));
                }
            }
            return new com.cloudimpl.outstack.runtime.ResultSet<>(total, (int) Math.ceil(((double) total) / pageSize), pageNum, list);
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected long getRootEntityByTypeCount(Connection conn, String tableName, String rootEntityType, List<String> tenantIds, String filter) {

        String filterSql = null;
        if (filter != null) {
            RestQLNode qlNode = RestQLParser.parse(filter); 
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            filterSql = sqlNode.eval(qlNode);
        }
        tenantIds.forEach(tenantId -> createTenantIfNotExist(tableName, tenantId));
        String tenantQuery = "(" + tenantIds.stream().map(t -> "tenantId = ?").collect(Collectors.joining(" or ")) + ")";
        String sql = "select count(*) as totalCount from " + tableName + " where rootEntityType = ?  and entityType = ? and " + tenantQuery + (filterSql != null ? "and " + filterSql : "");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rootEntityType);
            stmt.setString(2, rootEntityType);
            int i = 3;
            for (String tenantId : tenantIds) {
                stmt.setString(i++, tenantId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("totalCount");
                } else {
                    return 0;
                }
            }
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected long getChildEntityByTypeCount(Connection conn, String tableName, String rootEntityType, String rootId, String entityType, List<String> tenantIds, String filter) {
        tenantIds.forEach(tenantId -> createTenantIfNotExist(tableName, tenantId));
        String filterSql = null;

        if (filter != null) {
            RestQLNode qlNode = RestQLParser.parse(filter); 
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            filterSql = sqlNode.eval(qlNode);
        }
        String tenantQuery = "(" + tenantIds.stream().map(t -> "tenantId = ?").collect(Collectors.joining(" or ")) + ")";
        String sql = "select count(*) as TotalCount from " + tableName + " where rootEntityType = ? and rootId = ? and entityType = ?  and " + tenantQuery + (filterSql != null ? "and " + filterSql : "");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rootEntityType);
            stmt.setString(2, rootId);
            stmt.setString(3, entityType);
            int i = 4;
            for (String tenantId : tenantIds) {
                stmt.setString(i++, tenantId);
            }
            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getLong("TotalCount");
                } else {
                    return 0;
                }

            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected com.cloudimpl.outstack.runtime.ResultSet<String> getChildEntityByType(Connection conn, String tableName, String rootEntityType, String rootId, String entityType, List<String> tenantIds, String filter, String orderBy, int pageNum, int pageSize) {
        orderBy = orderBy == null ? "_meta.createdDate" : orderBy;
        tenantIds.forEach(tenantId -> createTenantIfNotExist(tableName, tenantId));
        String tenantQuery = "(" + tenantIds.stream().map(t -> "tenantId = ?").collect(Collectors.joining(" or ")) + ")";
        String filterSql = null;
        String orderBySql = null;
        if (filter != null) {
            RestQLNode qlNode = RestQLParser.parse(filter); 
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            filterSql = sqlNode.eval(qlNode);
        }

        if (orderBy != null) {
            RestQLNode qlNode = RestQLParser.parseOrderBy(orderBy);
            PostgresSqlNode sqlNode = new PostgresSqlNode();
            orderBySql = sqlNode.eval(qlNode);
        }
        long total = getChildEntityByTypeCount(conn, tableName, rootEntityType, rootId, entityType, tenantIds, filter);
        String sql = "select json from " + tableName + " where rootEntityType = ? and rootId = ? and entityType = ?  and " + tenantQuery + (filterSql != null ? "and " + filterSql : "") + (orderBySql != null ? " order By " + orderBySql : "") + (orderBy != null ? " limit " + pageSize + " offset " + (pageNum * pageSize) : "");
        log.info("getChildEntityByType : " + sql);
        List<String> list = new LinkedList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rootEntityType);
            stmt.setString(2, rootId);
            stmt.setString(3, entityType);
            int i = 4;
            for (String tenantId : tenantIds) {
                stmt.setString(i++, tenantId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("json"));
                }
                return new com.cloudimpl.outstack.runtime.ResultSet<>(total, (int) Math.ceil(((double) total) / pageSize), pageNum, list);
            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    protected boolean isIdExist(Connection conn, String tableName, String id, String tenantId) {
        createTenantIfNotExist(tableName, tenantId);
        try (PreparedStatement stmt = conn.prepareStatement("select 1 from " + tableName + " where entityId = ? and tenantId = ? limit 1")) {
            stmt.setString(1, id);
            stmt.setString(2, tenantId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    @Override
    public <T extends RootEntity> EventRepositoy<T> createOrGetRepository(Class<T> rootType) {
        return (EventRepositoy<T>) mapRepos.computeIfAbsent(rootType, type -> new PostgresEventRepository<>(this, (Class<T>) type, this.helper, this.providerConfig));
    }

    protected java.sql.ResultSet executeRawQuery(Connection conn, String query) {

        log.info("raw query : " + query);
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException ex) {
            throw new PostgresException(ex);
        }
    }

    public SqlResultSet executeCustomRawQuery(Function<Connection, java.sql.ResultSet> queryHandler) {
        Connection conn = getConnection();
        return new SqlResultSet(conn, queryHandler.apply(conn));
    }
}
