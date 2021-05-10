/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

/**
 *
 * @author nuwan
 */
public class QueryHelper {
    public static Query withRootId(Query query,String rootId)
    {
        query.setRootId(rootId);
        return query;
    }
    
    public static Query withTenantId(Query query,String tenantId)
    {
        query.setTenantId(tenantId);
        return query;
    }
    
    public static Query withId(Query query,String id)
    {
        query.setId(id);
        return query;
    }
    
    public static Query withPageable(Query query, Query.PagingRequest pageable)
    {
        query.setPageable(pageable);
        return query;
    }
}
