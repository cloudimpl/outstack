/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.AuthInput;
import com.cloudimpl.outstack.runtime.domainspec.CommandHelper;
import com.cloudimpl.outstack.runtime.domainspec.IQuery;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.QueryHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class QueryWrapper implements IQuery,AuthInput {

    private final String query;
    private final String rootId;
    private final String rootType;
    private final String childType;
    private final String id;
    private final String version;
    private final String payload;
    private Object grant;
    private String tenantId;
    private String context;
    private String domainOwner;
    private String domainContext;
    private Map<String, String> mapAttr;
    private final Query.PagingRequest pagingRequest;

    public QueryWrapper(Builder builder) {
        this.query = builder.query;
        this.rootType = builder.rootType;
        this.childType = builder.childType;
        this.rootId = builder.rootId;
        this.id = builder.id;
        this.version = builder.version;
        this.tenantId = builder.tenantId;
        this.payload = builder.payload == null ? "{}" : builder.payload;
        this.pagingRequest = builder.pageRequest;
        this.mapAttr = builder.mapAttr;
        this.context = builder.context;
        this.domainOwner = builder.domainOwner;
        this.domainContext = builder.domainContext;
    }

    protected void setMapAttr(Map<String, String> mapAttr) {
        this.mapAttr = mapAttr;
    }

    protected void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    protected void setContext(String context) {
        this.context = context;
    }

    @Override
    public <T extends Query> T unwrap(Class<T> type) {
        T query = GsonCodecRuntime.decode(type, payload);
        QueryHelper.withRootId(query, rootId);
        QueryHelper.withId(query, id);
        QueryHelper.withTenantId(query, tenantId);
        QueryHelper.withVersion(query, version);
        QueryHelper.withMapAttr(query, mapAttr);
        QueryHelper.withPageable(query, this.pagingRequest);
        return query;
    }

    @Override
    public String queryName() {
        return this.query;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String getRootId() {
        return rootId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRootType() {
        return rootType;
    }

    public void setGrant(Object grant) {
        this.grant = grant;
    }

    public <T> T getGrant() {
        return (T) grant;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getChildType() {
        return this.childType;
    }

    @Override
    public String getAction() {
        return this.query;
    }

    @Override
    public String getDomainContext() {
        return this.domainContext;
    }

    @Override
    public String getDomainOwner() {
        return this.domainOwner;
    }

    public static final class Builder {

        private String query;
        private String rootType;
        private String childType;
        private String rootId;
        private String id;
        private String tenantId;
        private String version;
        private String payload;
        private String context;
        private String domainOwner;
        private String domainContext;
        private Map<String, String> mapAttr;
        private Query.PagingRequest pageRequest;

        public Builder withQuery(String query) {
            this.query = query;
            return this;
        }

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder withRootType(String rootType) {
            this.rootType = rootType;
            return this;
        }

        public Builder withChildType(String childType){
             this.childType = childType;
             return this;
        }
        
        public Builder withPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withMapAttr(String key, String value) {
            if(mapAttr == null) {
                this.mapAttr = new HashMap<>();
            }
            this.mapAttr.put(key, value);
            return this;
        }

        public Builder withPageRequest(Query.PagingRequest pagingRequest) {
            this.pageRequest = pagingRequest;
            return this;
        }

        public QueryWrapper build() {
            return new QueryWrapper(this);
        }
    }
}
