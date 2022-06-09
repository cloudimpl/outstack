package com.cloudimpl.outstack.repo;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Builder
@AllArgsConstructor
public class QueryRequest {
    private String query = "";
    private String orderBy = "";
    private int pageSize = Integer.MAX_VALUE;
    private int pageNum = 0;
    private boolean mergeNonTenant = false;
    private List<String> distinctFields = Collections.EMPTY_LIST;

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

    public Collection<String> getDistinctFields()
    {
        return this.distinctFields;
    }
}
