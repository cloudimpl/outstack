/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;

/**
 *
 * @author nuwan
 */
public class CommandWrapper implements ICommand{
    private final String command;
    private final String payload;

    public CommandWrapper(String command, String payload) {
        this.command = command;
        this.payload = payload;
    }

    public <T extends Command> T unwrap(Class<T> type)
    {
        return GsonCodec.decode(type, payload);
    }

    @Override
    public String commandName() {
        return command;
    }
}
