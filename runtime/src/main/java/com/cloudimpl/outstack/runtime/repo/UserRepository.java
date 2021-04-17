/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;
import com.cloudimpl.outstack.runtime.domain.v1.Event;
import com.cloudimpl.outstack.runtime.domain.v1.RootEntity;

/**
 *
 * @author nuwansa
 */
public class UserRepository {
    private final Class<? extends RootEntity> rootType;

    public UserRepository(Class<? extends RootEntity> rootType) {
        this.rootType = rootType;
    }
    
    public <E extends Event,T extends Entity> T publish(E event)
    {
        return null;
    }
    
    public 
}
