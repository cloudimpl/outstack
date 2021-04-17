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
public abstract class Event implements IResource {

    private String _tenantId;
    private long _seqNum;

    public void setTenantId(String tenantId) {
        this._tenantId = tenantId;
    }

    public void setSeqNum(long seq) {
        this._seqNum = seq;
    }

    public long getSeqNum() {
        return _seqNum;
    }

    public String getTenantId() {
        return _tenantId;
    }

    public abstract Class<? extends Entity> getOwner();

    public abstract Class<? extends RootEntity> getRootOwner();

    public abstract String entityId();

    public abstract String rootEntityId();

    @Override
    public String getRn() {
        if (RootEntity.isMyType(getOwner())) {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        getTenantId(), getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());
            }
        } else {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}/{6}",
                        getTenantId(), getRootOwner().getSimpleName(), rootEntityId(), getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());

            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}/{5}",
                        getRootOwner().getSimpleName(), rootEntityId(), getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());

            }
        }
    }

    public String getEntityRn() {
        if (RootEntity.isMyType(getOwner())) {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}",
                        getTenantId(), getOwner().getSimpleName(), entityId());
            } else {
                return MessageFormat.format("{1}/{2}",
                        getOwner().getSimpleName(), entityId());
            }
        } else {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        getTenantId(), getRootOwner().getSimpleName(), rootEntityId(), getOwner().getSimpleName(), entityId());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getRootOwner().getSimpleName(), rootEntityId(), getOwner().getSimpleName(), entityId());
            }
        }
    }

    public String getRootEntityRn() {
        if (getTenantId() != null) {
            return MessageFormat.format("tenant/{0}/{1}/{2}",
                    getTenantId(), getRootOwner().getSimpleName(), rootEntityId());
        } else {
            return MessageFormat.format("{1}/{2}",
                    getRootOwner().getSimpleName(), rootEntityId());
        }
    }

    public boolean isRootEvent() {
        return getRootOwner() == getOwner();
    }

}
