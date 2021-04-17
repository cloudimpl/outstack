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
public interface RootEntity extends Entity {

    @Override
    String id();

    @Override
    default String getRn() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}", ITenant.class.cast(this).getTenantId(), this.getClass().getSimpleName(), id());
        } else {
            return MessageFormat.format("{1}/{2}", this.getClass().getSimpleName(), id());
        }
    }

    public static boolean isMyType(Class<? extends Entity> type) {
        return RootEntity.class.isAssignableFrom(type);
    }
}
