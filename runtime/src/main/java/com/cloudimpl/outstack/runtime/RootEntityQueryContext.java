/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author nuwan
 * @param <T>
 */
public interface RootEntityQueryContext<T extends RootEntity> extends EntityQueryContext<T> {

    <C extends ChildEntity<T>> Optional<C> getChildByEntityId(Class<C> childType, String id);

    <C extends ChildEntity<T>> Optional<C> getChildById(Class<C> childType, String id);

    <C extends ChildEntity<T>> Collection<C> getAllChildsByType(Class<C> childType);
    
    Optional<T> getRoot();
}
