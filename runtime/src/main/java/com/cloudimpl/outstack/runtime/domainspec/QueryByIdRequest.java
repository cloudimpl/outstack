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
public class QueryByIdRequest extends Query{
    public QueryByIdRequest(Builder builder) {
        super(builder);
    }

    
    
    public static final class Builder extends Query.Builder
    {
        @Override
        public QueryByIdRequest build() {
           return new QueryByIdRequest(this);
        }
    }
    
}
