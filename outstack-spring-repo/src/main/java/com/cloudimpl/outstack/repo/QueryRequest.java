package com.cloudimpl.outstack.repo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class QueryRequest {
    private String query = "";
    private String orderBy = "";
    private int pageSize = Integer.MAX_VALUE;
    private int pageNum = 0;
    private boolean mergeNonTenant = false;
    @Builder.Default
    private List<String> distinctFields = Collections.EMPTY_LIST;

    public QueryRequest(String query, String orderBy, Integer pageSize, Integer pageNum, Boolean mergeNonTenant, List<String> distinctFields) {
        this.query = query;
        this.orderBy = orderBy;
        this.pageSize = pageSize == null ? Integer.MAX_VALUE : pageSize;
        this.pageNum = pageNum == null ? 0 : pageNum;
        this.mergeNonTenant = mergeNonTenant != null && mergeNonTenant;
        this.distinctFields = distinctFields == null ? Collections.EMPTY_LIST : distinctFields;
    }

    public String getQuery() {
        return query == null ? "" : query;
    }

    public String getOrderBy() {
        return orderBy == null ? "" : orderBy;
    }

    public int getPageSize() {
        return pageSize == 0 ? Integer.MAX_VALUE : pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public boolean isMergeNonTenant() {
        return this.mergeNonTenant;
    }

    public Collection<String> getDistinctFields() {
        return this.distinctFields;
    }
}
