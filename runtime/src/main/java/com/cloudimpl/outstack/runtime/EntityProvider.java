/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <R>
 */
public interface EntityProvider<R extends RootEntity>{
    public <K extends Entity,C extends ChildEntity<R>> Optional<K> loadEntity(Class<R> rootType,String id,Class<C> childType,String childId,String tenantId);
}
