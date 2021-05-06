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
 */
public interface QueryOperations {
    <T extends RootEntity> Optional<T> getRootById(String rn);
    <R extends RootEntity,T extends ChildEntity<R>> Optional<T> getChildById(String rn);
    <R extends RootEntity,T extends ChildEntity<R>> Collection<T> getAllChildByType(String rootTrn,Class<T> childType);
    
}
