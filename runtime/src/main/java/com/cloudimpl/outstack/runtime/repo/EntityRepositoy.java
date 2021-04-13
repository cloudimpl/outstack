/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;

/**
 *
 * @author nuwan
 */
public interface EntityRepositoy {
    <T extends Entity> T insert(T entity);
    <T extends Entity> T update(T entity);
    <T extends Entity> T deleteById(String entityType,String id);
    <T extends Entity> T deleteByEntityId(String entityType,String entityId);
}
