/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domain.v1;

/**
 *
 * @author nuwan
 */
public interface Entity {

    String id();

    default boolean hasTenant() {
        return this instanceof Tenant;
    }
}
