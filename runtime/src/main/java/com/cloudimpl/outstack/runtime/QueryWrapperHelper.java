package com.cloudimpl.outstack.runtime;

import java.util.Map;

public class QueryWrapperHelper {

    public static QueryWrapper withMapAttr(QueryWrapper cmd, Map<String, String> mapAttr) {
        cmd.setMapAttr(mapAttr);
        return cmd;
    }

    public static QueryWrapper withContext(QueryWrapper cmd, String context) {
        cmd.setContext(context);
        return cmd;
    }

    public static QueryWrapper withTenantId(QueryWrapper cmd, String tenantId) {
        cmd.setTenantId(tenantId);
        return cmd;
    }
}
