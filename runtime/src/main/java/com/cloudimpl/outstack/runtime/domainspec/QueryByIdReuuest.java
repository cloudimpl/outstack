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
public class QueryByIdReuuest extends Query{
    private final String id;
    public QueryByIdReuuest(Builder builder) {
        super(builder);
        this.id = builder.id;
    }

    public String id() {
        return id;
    }
    
    public static final class Builder extends Query.Builder
    {
        private String id;

        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }
        
        @Override
        public QueryByIdReuuest build() {
           return new QueryByIdReuuest(this);
        }
    }
    
}
