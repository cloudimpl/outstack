/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domain.v1;

/**
 *
 * @author nuwansa
 */
public class User implements RootEntity,ITenant{
    private final String userId;
    private final String tenantId;
    public User(String userId,String tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
    }
    
    @Override
    public String id() {
        return this.userId;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }
    
    public static boolean isRoot()
    {
        
    }
}
