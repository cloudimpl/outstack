/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.util.TimeUtils;
import com.google.gson.JsonObject;

/**
 *
 * @author nuwan
 */
public abstract class Entity implements IResource {

    private String _id;
    private Meta _meta = new Meta();

    final void setTid(String id) {
        this._id = id;
    }

    public final String id() {
        return _id;
    }

    public abstract String entityId();

    public final boolean hasTenant() {
        return this instanceof ITenant;
    }

    public final boolean isRoot() {
        return this instanceof RootEntity;
    }

    protected abstract void apply(Event event);

    public void applyEvent(Event event) {
        if (event.getOwner() != this.getClass()) {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_DOMAIN_EVENT, "invalid domain event: " + event.getClass().getName());
        }
        apply(event);
    }

    public <T extends Entity> T cloneEntity() {
        String json = GsonCodec.encode(this);
        return GsonCodec.decode((Class<T>) this.getClass(), json);
    }

    public <T extends Entity> T rename(String newEntityId) {
        JsonObject json = GsonCodec.encodeToJson(this).getAsJsonObject();
        json.addProperty(idField(), newEntityId);
        return GsonCodec.decode((Class<T>) this.getClass(), json.toString());
    }

    public abstract String idField();

    public static void checkTenantEligibility(Class<? extends Entity> type, String tenantId) {
        if (EntityHelper.hasTenant(type) && tenantId == null) {
            throw new DomainEventException(DomainEventException.ErrorCode.TENANT_ID_NOT_AVAILABLE, "tenantId is null for entity creation");
        } else if (!EntityHelper.hasTenant(type) && tenantId != null) {
            throw new DomainEventException(DomainEventException.ErrorCode.TENANT_ID_NOT_APPLICABLE, "tenantId is not applicable for entity creation");
        }
    }

    public static boolean hasTenant(Class<? extends Entity> entityType) {
        return ITenant.class.isAssignableFrom(entityType);
    }

    public Meta getMeta() {
        return _meta;
    }

    @Override
    public String toString() {
        return GsonCodec.encode(this);
    }

    public static final class Meta {

        private long createdDate;
        private long updatedDate;

        protected void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        protected void setUpdatedDate(long updatedDate) {
            this.updatedDate = updatedDate;
        }

        
        public String getCreatedDate() {
            return TimeUtils.toStringDateTime(TimeUtils.fromEpoch(createdDate));
        }

        public String getUpdatedDate() {
            return TimeUtils.toStringDateTime(TimeUtils.fromEpoch(updatedDate));
        }

        public long createdDate()
        {
            return this.createdDate;
        }
        
        public long updatedDate()
        {
            return this.updatedDate;
        }
    }
}
