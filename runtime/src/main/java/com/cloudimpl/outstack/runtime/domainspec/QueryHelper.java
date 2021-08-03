/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Map;

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
    
    public static Query withVersion(Query query,String version)
    {
        query.setVersion(version);
        return query;
    }

    public static Query withContext(Query cmd, String context) {
        cmd.setContext(context);
        return cmd;
    }

    public static Query withMapAttr(Query cmd, Map<String, Object> mapAttr) {
        cmd.setMapAttr(mapAttr);
        return cmd;
    }
    
    public static Query withPageable(Query query, Query.PagingRequest pageable)
    {
        query.setPageable(pageable);
        return query;
    }
}
