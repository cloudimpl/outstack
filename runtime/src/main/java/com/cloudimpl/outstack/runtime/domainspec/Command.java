/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Objects;

/**
 *
 * @author nuwansa
 */
public abstract class Command implements Input {

    private final String rootId;
    private final String tenantId;
    private final String command;

    public Command(Builder builder) {
        Objects.requireNonNull(builder.command);
        this.rootId = builder.rootId;
        this.tenantId = builder.tenantId;
        this.command = builder.command;
    }

    public String rootId() {
        return rootId;
    }

    public String commandName() {
        return command;
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected String rootId;
        protected String tenantId;
        protected String command;

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder withCommand(String command) {
            this.command = command;
            return this;
        }

        public <T extends Command> T build() {
            Command cmd = new Command(this) {
            };
            return (T) cmd;
        }
    }
}
