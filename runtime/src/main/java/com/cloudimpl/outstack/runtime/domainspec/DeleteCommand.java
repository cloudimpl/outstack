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
public class DeleteCommand extends Command{
    private final String id;
    public DeleteCommand(Builder builder) {
        super(builder);
        this.id = builder.id;
    }

    public String getId() {
        return id;
    }
    
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder
    {
        private String id;
        
        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }
        
        @Override
        public DeleteCommand build()
        {
            return new DeleteCommand(this);
        }
    }
    
    public static void main(String[] args) {
       DeleteCommand delete =  DeleteCommand.builder().withCommand("DeleteUser").withRootId("xxxxx").withTenantId("xxxxx").build();
    }
}
