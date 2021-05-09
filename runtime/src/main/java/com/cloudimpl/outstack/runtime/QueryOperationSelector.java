/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
public interface QueryOperationSelector {
    <R extends RootEntity> QueryOperations<R> select(Class<R> type);
}
