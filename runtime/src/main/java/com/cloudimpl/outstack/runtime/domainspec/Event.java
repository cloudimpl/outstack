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
import java.text.MessageFormat;

/**
 *
 * @author nuwan
 * @param <T>
 */
public abstract class Event<T extends Entity> implements IResource, Input {

    public enum Action {
        CREATE, UPDATE, DELETE, RENAME
    }
    private String _tenantId;
    private long _seqNum;
    private String _id;
    private String _rootId;
    private Action _action;
    private Meta _meta = new Meta();

    public Event() {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getOwner());
        EntityMetaDetail rootMeta = EntityMetaDetailCache.instance().getEntityMeta(this.getRootOwner());
        _meta.setIdIgnoreCase(meta.isIdIgnoreCase());
        _meta.setRootIdIgnoreCase(rootMeta.isIdIgnoreCase());
    }

    public void setTenantId(String tenantId) {
        this._tenantId = tenantId;
    }

    public void setSeqNum(long seq) {
        this._seqNum = seq;
    }

    public String persistedId() {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getOwner());
        return meta.isIdIgnoreCase() ? entityId().toLowerCase() : entityId();
    }

    public String rootPersistedId() {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(this.getRootOwner());
        return meta.isIdIgnoreCase() ? rootEntityId().toLowerCase() : rootEntityId();
    }

    public void setAction(Action action) {
        this._action = action;
    }

    public long getSeqNum() {
        return _seqNum;
    }

    public Action getAction() {
        return _action;
    }

    public Meta getMeta() {
        return _meta;
    }

    public boolean isConsumed() {
        return this._action != null;
    }

    public void setRootId(String rootId) {
        this._rootId = rootId;
    }

    public void setId(String id) {
        this._id = id;
    }

    @Override
    public final String tenantId() {
        return _tenantId;
    }

    public final String id() {
        return _id;
    }

    public final String rootId() {
        return _rootId;
    }

    public abstract Class<? extends Entity> getOwner();

    public abstract Class<? extends RootEntity> getRootOwner();

    public abstract String entityId();

    public abstract String rootEntityId();

    @Override
    public String getTRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}",
                        tenantId(), getMeta().getVersion(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",
                        getMeta().getVersion(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}/{6}/{7}",
                        tenantId(), getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());

            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}/{5}/{6}",
                        getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());

            }
        }
    }

    @Override
    public String getBRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}",
                        tenantId(), getMeta().getVersion(), getOwner().getSimpleName(), persistedId(), getClass().getSimpleName(), getSeqNum());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",
                        getMeta().getVersion(), getOwner().getSimpleName(), persistedId(), getClass().getSimpleName(), getSeqNum());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}/{6}/{7}",
                        tenantId(), getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), persistedId(), getClass().getSimpleName(), getSeqNum());

            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}/{5}/{6}",
                        getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), persistedId(), getClass().getSimpleName(), getSeqNum());

            }
        }
    }

    public String getEntityTRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}",
                        tenantId(), getMeta().getVersion(), getOwner().getSimpleName(), id());
            } else {
                return MessageFormat.format("{0}/{1}/{2}",
                        getMeta().getVersion(), getOwner().getSimpleName(), id());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}",
                        tenantId(), getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",
                        getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id());
            }
        }
    }

    public String getEntityRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}",
                        tenantId(), getMeta().getVersion(), getOwner().getSimpleName(), persistedId());
            } else {
                return MessageFormat.format("{0}/{1}/{2}",
                        getMeta().getVersion(), getOwner().getSimpleName(), persistedId());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}",
                        tenantId(), getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), persistedId());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",
                        getMeta().getVersion(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), persistedId());
            }
        }
    }

    public String getRootEntityTRN() {
        if (tenantId() != null) {
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}",
                    tenantId(), getMeta().getVersion(), getRootOwner().getSimpleName(), rootId());
        } else {
            return MessageFormat.format("{0}/{1}/{2}",
                    getMeta().getVersion(), getRootOwner().getSimpleName(), rootId());
        }
    }

    public String getRootEntityRN() {
        if (tenantId() != null) {
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}",
                    tenantId(), getMeta().getVersion(), getRootOwner().getSimpleName(), rootPersistedId());
        } else {
            return MessageFormat.format("{0}/{1}/{2}",
                    getMeta().getVersion(), getRootOwner().getSimpleName(), rootPersistedId());
        }
    }

    public boolean isRootEvent() {
        return getRootOwner() == getOwner();
    }

    @Override
    public String toString() {
        return GsonCodecRuntime.encode(this);
    }

    public static final class Meta {

        private long createdDate;
        private String version;
        private boolean idIgnoreCase;
        private boolean rootIdIgnoreCase;

        protected void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        protected void setVersion(String version) {
            this.version = version;
        }

        public String getCreatedDate() {
            return TimeUtils.toStringDateTime(TimeUtils.fromEpoch(createdDate));
        }

        public void setIdIgnoreCase(boolean ignoreCase) {
            this.idIgnoreCase = ignoreCase;
        }

        public void setRootIdIgnoreCase(boolean rootIgnoreCase) {
            this.rootIdIgnoreCase = rootIgnoreCase;
        }

        public long createdDate() {
            return createdDate;
        }

        public String getVersion() {
            return version;
        }
    }
}
