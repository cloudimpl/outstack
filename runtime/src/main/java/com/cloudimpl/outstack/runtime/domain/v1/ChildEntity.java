/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domain.v1;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public interface ChildEntity extends Entity {

    String rootId();

    <T extends RootEntity> Class<T> rootType();
    
      @Override
    default String getRn() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}",ITenant.class.cast(this).getTenantId(),rootType().getSimpleName(),rootId(),getClass().getSimpleName(),id());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}",rootType().getSimpleName(),rootId(),getClass().getSimpleName(), id());
        }
    }
}
