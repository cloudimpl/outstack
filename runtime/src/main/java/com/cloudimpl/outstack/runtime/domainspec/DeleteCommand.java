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
    
    public DeleteCommand(Builder builder) {
        super(builder);
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder extends Command.Builder
    {
        @Override
        public DeleteCommand build()
        {
            return new DeleteCommand(this);
        }
    }
    
}
