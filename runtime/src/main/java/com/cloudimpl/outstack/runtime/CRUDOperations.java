/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Entity;

/**
 *
 * @author nuwan
 */
public interface CRUDOperations {
    void create(Entity entity);
    void update(Entity entity);
    void delete(Entity entity);
    void rename(Entity oldEntity,Entity newEntity);
   
}
