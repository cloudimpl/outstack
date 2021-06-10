/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import com.cloudimpl.outstack.runtime.EntityMetaDetailCache;
import com.cloudimpl.outstack.runtime.EntityMetaDetail;
import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.util.TimeUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonObject;

/**
 *
 * @author nuwan
 */
public abstract class Entity implements IResource {

    @JsonProperty
    private String _id;
    protected final Meta _meta = new Meta();
    public Entity() {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getClass());
        this._meta.setVersion(meta.getVersion());
        this._meta.setIdIgnoreCase(meta.isIdIgnoreCase());
        this._meta.setRootIdIgnoreCase(meta.isIdIgnoreCase());
    }

    final void setTid(String id) {
        this._id = id;
    }

    public String persistedId() {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getClass());
        return meta.isIdIgnoreCase()?entityId().toLowerCase():entityId();
    }

    public final String id() {
        return _id;
    }

    public abstract String entityId();

    @JsonProperty
    public final TenantRequirement getTenantRequirement() {
        if (ITenant.class.isInstance(this)) {
            return TenantRequirement.REQUIRED;
        } else if (ITenantOptional.class.isInstance(this)) {
            return TenantRequirement.OPTIONAL;
        }
        return TenantRequirement.NONE;
    }

    public String getTenantId() {
        return null;
    }

    public final boolean isRoot() {
        return this instanceof RootEntity;
    }

    protected abstract void apply(Event event);

    public final void applyEvent(Event event) {
        if (event.getOwner() != this.getClass()) {
            throw new DomainEventException(DomainEventException.ErrorCode.INVALID_DOMAIN_EVENT, "invalid domain event: " + event.getClass().getName());
        }
        apply(event);
    }

    public <T extends Entity> T cloneEntity() {
        String json = GsonCodecRuntime.encode(this);
        return GsonCodecRuntime.decode((Class<T>) this.getClass(), json);
    }

    public <T extends Entity> T rename(String newEntityId) {
        JsonObject json = GsonCodecRuntime.encodeToJson(this).getAsJsonObject();
        json.addProperty(idField(), newEntityId);
        return GsonCodecRuntime.decode((Class<T>) this.getClass(), json.toString());
    }

    public abstract String idField();

    public static void checkTenantEligibility(Class<? extends Entity> type, String tenantId) {
        if (EntityHelper.hasTenant(type) && tenantId == null) {
            throw new DomainEventException(DomainEventException.ErrorCode.TENANT_ID_NOT_AVAILABLE, "tenantId is null for entity creation");
        } else if ((!EntityHelper.hasTenant(type) && !EntityHelper.hasOptionalTenant(type)) && tenantId != null) {
            throw new DomainEventException(DomainEventException.ErrorCode.TENANT_ID_NOT_APPLICABLE, "tenantId is not applicable for entity creation");
        }
    }

    public static TenantRequirement checkTenantRequirement(Class<? extends Entity> type) {
        if (ITenant.class.isAssignableFrom(type)) {
            return TenantRequirement.REQUIRED;
        } else if (ITenantOptional.class.isAssignableFrom(type)) {
            return TenantRequirement.OPTIONAL;
        } else {
            return TenantRequirement.NONE;
        }
    }

    public static boolean hasTenant(Class<? extends Entity> entityType) {
        return ITenant.class.isAssignableFrom(entityType);
    }

    public static String getVersion(Class<? extends Entity> entityType) {
        return entityType.getAnnotation(EntityMeta.class).version();
    }

    public final Meta getMeta() {
        return _meta;
    }

    @Override
    public String toString() {
        return GsonCodecRuntime.encode(this);
    }

    public static final class Meta {

        private long createdDate;
        private long updatedDate;
        private String version;
        private boolean idIgnoreCase;
        private boolean rootIdIgnoreCase;

        protected void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        protected void setUpdatedDate(long updatedDate) {
            this.updatedDate = updatedDate;
        }

        protected void setVersion(String version) {
            this.version = version;
        }

        public String getCreatedDate() {
            return TimeUtils.toStringDateTime(TimeUtils.fromEpoch(createdDate));
        }

        public String getUpdatedDate() {
            return TimeUtils.toStringDateTime(TimeUtils.fromEpoch(updatedDate));
        }

        public void setIdIgnoreCase(boolean idIgnoreCase) {
            this.idIgnoreCase = idIgnoreCase;
        }

        public void setRootIdIgnoreCase(boolean rootIdIgnoreCase) {
            this.rootIdIgnoreCase = rootIdIgnoreCase;
        }
        
        public String getVersion() {
            return version;
        }

        public long createdDate() {
            return this.createdDate;
        }

        public long updatedDate() {
            return this.updatedDate;
        }
    }
}
