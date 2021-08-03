package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;

import java.util.Map;

public class CommandWrapperHelper {

    public static CommandWrapper withMapAttr(CommandWrapper cmd, Map<String, Object> mapAttr) {
        cmd.setMapAttr(mapAttr);
        return cmd;
    }

    public static CommandWrapper withContext(CommandWrapper cmd, String context) {
        cmd.setContext(context);
        return cmd;
    }

    public static CommandWrapper withTenantId(CommandWrapper cmd, String tenantId) {
        cmd.setTenantId(tenantId);
        return cmd;
    }
}
