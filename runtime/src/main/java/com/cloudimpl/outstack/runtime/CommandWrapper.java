/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.CommandHelper;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class CommandWrapper implements ICommand {

    private final String command;
    private final String rootId;
    private final String id;
    private final String tenantId;
    private final String payload;
    private final String version;
    public CommandWrapper(Builder builder) {
        this.command = builder.command;
        this.rootId = builder.rootId;
        this.tenantId = builder.tenantId;
        this.payload = builder.payload == null?"{}":builder.payload;
        this.id = builder.id;
        this.version = builder.version;
    }

    @Override
    public final <T extends Command> T unwrap(Class<T> type) {
        T cmd = GsonCodecRuntime.decode(type, payload);
        CommandHelper.withRootId(cmd, rootId);
        CommandHelper.withTenantId(cmd, tenantId);
        CommandHelper.withId(cmd, id);
        CommandHelper.withVersion(cmd, version);
        return cmd;
    }

    @Override
    public final String commandName() {
        return command;
    }
    
    @Override
    public final String version(){
        return version;
    }
    
    public final Optional<String> getRootId() {
        return Optional.ofNullable(rootId);
    }

    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder {

        private  String command;
        private  String rootId;
        private  String id;
        private  String tenantId;
        private  String payload;
        private String version;
        
        public Builder withCommand(String command)
        {
            this.command = command;
            return this;
        }
        
        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }
        
        public Builder withVersion(String version)
        {
            this.version = version;
            return this;
        }
        
        public Builder withRootId(String rootId)
        {
            this.rootId = rootId;
            return this;
        }
        
        public Builder withTenantId(String tenantId)
        {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder withPayload(String payload)
        {
            this.payload = payload;
            return this;
        }
        
        public Builder withObject(Object payload)
        {
            this.payload = GsonCodecRuntime.encode(payload);
            return this;
        }
        public CommandWrapper build()
        {
            return new CommandWrapper(this);
        }
    }
}
