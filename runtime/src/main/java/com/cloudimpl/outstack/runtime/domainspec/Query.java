/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public abstract class Query implements IQuery {

    private String _rootId;
    private String _tenantId;
    private String _id;
    private String _version;
    private PagingRequest _pagingReq;
    private Map<String, String> _mapAttr;
    private String _context;
    private String _queryName;

    public Query(Builder builder) {
        this._rootId = builder.rootId;
        this._tenantId = builder.tenantId;
        this._id = builder.id;
        this._pagingReq = builder.pagingReq;
        this._version = builder.version;
        this._mapAttr = builder.mapAttr;
        this._context = builder.context;
        this._queryName = builder.queryName;
    }

    public final String tenantId() {
        return _tenantId;
    }

    protected void setRootId(String rootId) {
        this._rootId = rootId;
    }

    protected void setTenantId(String tenantId) {
        this._tenantId = tenantId;
    }

    protected void setPageable(PagingRequest pageable) {
        this._pagingReq = pageable;
    }

    protected void setId(String id) {
        this._id = id;
    }

    public void setMapAttr(Map<String, String> mapAttr) {
        this._mapAttr = mapAttr;
    }

    public String getRootId() {
        return _rootId;
    }

    public String getTenantId() {
        return _tenantId;
    }

    public String getId() {
        return _id;
    }

    public String getContext() {
        return _context;
    }

    public Map<String, String> getMapAttr() {
        return _mapAttr;
    }

    public void setContext(String context) {
        this._context = context;
    }

    @Override
    public <T extends Query> T unwrap(Class<T> type) {
        return (T) this;
    }

    @Override
    public String queryName() {
        return this._queryName;
    }

    @Override
    public String version(){
        return _version;
    }
    
    public final String rootId() {
        return this._rootId;
    }

    public final String id() {
        return this._id;
    }

    public PagingRequest getPagingReq() {
        return _pagingReq;
    }

    protected void setVersion(String version) {
       this._version = version;
    }

    public abstract static class Builder {

        private String rootId;
        private String tenantId;
        private String id;
        private String version;
        private Map<String, String> mapAttr;
        private String context;
        private String queryName;
        private PagingRequest pagingReq;

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withTenantId(String tenatId) {
            this.tenantId = tenatId;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withMapAttr(Map<String, String> mapAttr){
            this.mapAttr = mapAttr;
            return this;
        }

        public Builder withVersion(String version){
            this.version = version;
            return this;
        }

        public Builder withContext(String context){
            this.context = context;
            return this;
        }

        public Builder withQueryName(String queryName) {
            this.queryName = queryName;
            return this;
        }
        
        public Builder withPagingReq(PagingRequest pagingReq) {
            this.pagingReq = pagingReq;
            return this;
        }

        public abstract <T extends Query> T build();

    }

    public static class PagingRequest {

        public static final PagingRequest EMPTY = new PagingRequest(0, Integer.MAX_VALUE, Collections.EMPTY_LIST, Collections.EMPTY_MAP,null,null);
        private final int pageNum;
        private final int pageSize;
        private final List<Order> orders;
        private final Map<String,String> params;
        private final String search;
        private final String orderBy;
        public PagingRequest(int pageNum, int pageSize, List<Order> orders,Map<String,String> params)
        {
            this(pageNum, pageSize, orders, params, null,null);
        }
        
        public PagingRequest(int pageNum, int pageSize, List<Order> orders,Map<String,String> params,String searchFilter,String orderBy) {
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.orders = Collections.unmodifiableList(orders);
            this.params = Collections.unmodifiableMap(params);
            this.search = searchFilter;
            this.orderBy = orderBy;
        }

        public List<Order> orders() {
            return this.orders;
        }

        public int pageNum() {
            return this.pageNum;
        }

        public int pageSize() {
            return this.pageSize;
        }

        public String getSearchFilter() {
            return search;
        }

        public String getOrderBy() {
            return orderBy;
        }
        
        public Map<String,String> getParams()
        {
            return params;
        }
    }

    public static class Order {

        private final String name;
        private final Direction direction;

        public Order(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        public String getName() {
            return name;
        }

        public Direction getDirection() {
            return direction;
        }

    }

    public static enum Direction {
        ASC,
        DESC;
    }
}
