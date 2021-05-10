/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.domainspec.IQuery;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.QueryHelper;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class QueryWrapper implements IQuery {

    private final String query;
    private final String rootId;
    private final String id;
    private final String tenantId;
    private final String payload;
    private final Query.PagingRequest pagingRequest;
    public QueryWrapper(Builder builder) {
        this.query = builder.query;
        this.rootId = builder.rootId;
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.payload = builder.payload == null?"{}":builder.payload;
        this.pagingRequest = builder.pageRequest;
    }

    @Override
    public <T extends Query> T unwrap(Class<T> type) {
        T query = GsonCodec.decode(type, payload);
        QueryHelper.withRootId(query, rootId);
        QueryHelper.withId(query, id);
        QueryHelper.withTenantId(query, tenantId);
        QueryHelper.withPageable(query, this.pagingRequest);
        return query;
    }

    @Override
    public String queryName() {
        return this.query;
    }

    public Optional<String> getRootId() {
        return Optional.ofNullable(rootId);
    }

    public Optional<String> getId()
    {
        return Optional.ofNullable(id);
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder {

        private  String query;
        private  String rootId;
        private  String id;
        private  String tenantId;
        private  String payload;
        private Query.PagingRequest pageRequest;
        
        public Builder withQuery(String query)
        {
            this.query = query;
            return this;
        }
        
        public Builder withRootId(String rootId)
        {
            this.rootId = rootId;
            return this;
        }
        
        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }
        
        public Builder withTenantId(String tenantId)
        {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder withPayload(String payload)
        {
            this.payload = payload;
            return this;
        }
        
        public Builder withPageRequest(Query.PagingRequest pagingRequest)
        {
            this.pageRequest = pagingRequest;
            return this;
        }
        public QueryWrapper build()
        {
            return new QueryWrapper(this);
        }
    }
}
