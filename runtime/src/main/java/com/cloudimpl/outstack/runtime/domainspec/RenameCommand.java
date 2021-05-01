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
public class RenameCommand extends Command{
    private final String entityId;
    
    public RenameCommand(Builder builder) {
        super(builder);
        this.entityId = builder.entityId;
    }

    public String getEntityId() {
        return entityId;
    }
    
    public static final class Builder extends Command.Builder
    {
        private String entityId;
        
        public Builder withEntityId(String entityId)
        {
            this.entityId = entityId;
            return this;
        }
        
        @Override
        public RenameCommand build()
        {
            return new RenameCommand(this);
        }
    }
    
}
