/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.List;
import java.util.Map;

/**
 * @author nuwan
 */
public class CommandHelper {
    public static Command withRootId(Command cmd, String rootId) {
        cmd.setRootId(rootId);
        return cmd;
    }

    public static Command withTenantId(Command cmd, String tenantId) {
        cmd.setTenantId(tenantId);
        return cmd;
    }

    public static Command withId(Command cmd, String id) {
        cmd.setId(id);
        return cmd;
    }

    public static Command withVersion(Command cmd, String version) {
        cmd.setVersion(version);
        return cmd;
    }

    public static Command withFiles(Command cmd, List<Object> files) {
        cmd.setFiles(files);
        return cmd;
    }

    public static Command withMapAttr(Command cmd, Map<String, String> mapAttr) {
        cmd.setMapAttr(mapAttr);
        return cmd;
    }
}
