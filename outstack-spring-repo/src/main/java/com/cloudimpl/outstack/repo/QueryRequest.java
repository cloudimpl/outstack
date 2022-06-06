package com.cloudimpl.outstack.repo;

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

    public String getQuery()
    {
        return query == null ? "":query;
    }

    public String getOrderBy()
    {
        return orderBy == null ? "":orderBy;
    }

    public int getPageSize()
    {
        return pageSize == 0 ? Integer.MAX_VALUE : pageSize;
    }

    public int getPageNum()
    {
        return pageNum;
    }

    public boolean isMergeNonTenant()
    {
        return this.mergeNonTenant;
    }
}
