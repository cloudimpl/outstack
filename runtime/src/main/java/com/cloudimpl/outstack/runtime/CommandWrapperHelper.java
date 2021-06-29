package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;

import java.util.Map;

public class CommandWrapperHelper {

    public static CommandWrapper withMapAttr(CommandWrapper cmd, Map<String, String> mapAttr) {
        cmd.setMapAttr(mapAttr);
        return cmd;
    }
}
