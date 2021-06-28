/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nuwansa
 */
public abstract class Command implements Input, ICommand {

    private String _rootId;
    private String _id;
    private String _tenantId;
    private String _version;
    private final String _commandName;
    private List<Object> _files;
    private Map<String, String> _mapAttr;

    public Command(Builder builder) {
        this._rootId = builder.rootId;
        this._tenantId = builder.tenantId;
        this._id = builder.id;
        this._version = builder.version;
        this._commandName = builder.commandName;
        this._files = builder.files;
        this._mapAttr = builder.mapAttr;
    }

    public final String rootId() {
        return _rootId;
    }

    protected void setRootId(String rootId) {
        this._rootId = rootId;
    }

    protected void setId(String id) {
        this._id = id;
    }

    public final String id() {
        return this._id;
    }

    protected void setTenantId(String tenantId) {
        this._tenantId = tenantId;
    }

    protected void setVersion(String version) {
        this._version = version;
    }

    public List<Object> getFiles() {
        return _files;
    }

    protected void setFiles(List<Object> _files) {
        this._files = _files;
    }

    public Map<String, String> getMapAttr() {
        return _mapAttr;
    }

    protected void setMapAttr(Map<String, String> _mapAttr) {
        this._mapAttr = _mapAttr;
    }

    @Override
    public final String commandName() {
        return _commandName;
    }

    @Override
    public final <T extends Command> T unwrap(Class<T> type) {
        return (T) this;
    }

    @Override
    public final String tenantId() {
        return _tenantId;
    }

    @Override
    public final String version() {
        return _version;
    }

    public static class Builder {

        protected String rootId;
        protected String id;
        protected String tenantId;
        protected String version;
        protected String commandName;
        protected List<Object> files;
        private Map<String, String> mapAttr;

        public Builder withRootId(String rootId) {
            this.rootId = rootId;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder withCommandName(String commandName) {
            this.commandName = commandName;
            return this;
        }

        public Builder withFiles(List<Object> files) {
            this.files = files;
            return this;
        }

        public Builder withMapAttr(String key, String value) {
            if(mapAttr == null) {
                this.mapAttr = new HashMap<>();
            }
            this.mapAttr.put(key, value);
            return this;
        }

        public <T extends Command> T build() {
            return (T) new Command(this) {
            };
        }
    }
}
