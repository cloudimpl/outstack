/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import com.cloudimpl.outstack.runtime.util.Util;
import java.text.MessageFormat;
import java.util.Objects;

/**
 *
 * @author nuwan
 */
public abstract class RootEntity extends Entity {

    @Override
    public final String getBRN() {
        switch (getTenantRequirement()) {
            case TENANT_REQUIRED: {
                return makeRN(this.getClass(), getMeta().getVersion(), id(), getTenantId());
            }
            default: {
                return makeRN(this.getClass(), getMeta().getVersion(), id(), getTenantId());
            }
        }
    }

    @Override
    public final String getTRN() {
        switch (getTenantRequirement()) {
            case TENANT_REQUIRED: {
                Objects.requireNonNull(ITenant.class.cast(this).getTenantId());
                return makeTRN(this.getClass(), getMeta().getVersion(), id(), getTenantId());
            }
            default: {
                return makeTRN(this.getClass(), getMeta().getVersion(), id(), getTenantId());
            }
        }
    }

    public <T extends ChildEntity> T createChildEntity(Class<T> type, String entityId, String id) {
        T t;
        switch (getTenantRequirement()) {
            case TENANT_REQUIRED: {
                Objects.requireNonNull(ITenant.class.cast(this).getTenantId());
                t = Util.createObject(type, new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(entityId, ITenant.class.cast(this).getTenantId()));
                break;
            }
            case TENANT_OPTIONAL: {
                t = Util.createObject(type, new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(entityId, getTenantId()));
                break;
            }
            default: {
                t = Util.createObject(type, new Util.VarArg<>(String.class), new Util.VarArg<>(entityId));
            }
        }
        EntityHelper.updateId(t, id);
        EntityHelper.updateRootId(t, id());
        return t;
    }

    public static <T extends RootEntity> T create(Class<T> type, String entityId, String tenantId, String tid) {
        T root;
        if (hasTenant(type)) {
            root = Util.createObject(type, new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(entityId, tenantId));
        } else {
            root = Util.createObject(type, new Util.VarArg<>(String.class), new Util.VarArg<>(entityId));
        }
        EntityHelper.updateId(root, tid);
        return root;
    }

    public static boolean isMyType(Class<? extends Entity> type) {
        return RootEntity.class.isAssignableFrom(type);
    }

    public static String makeRN(Class<? extends RootEntity> type, String version, String entityId, String tenantId) {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}", tenantId, version, type.getSimpleName(), entityId);
        } else {
            return MessageFormat.format("{0}/{1}/{2}", version, type.getSimpleName(), entityId);
        }
    }

    public static String makeTRN(Class<? extends RootEntity> type, String version, String id, String tenantId) {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}", tenantId, version, type.getSimpleName(), id);
        } else {
            return MessageFormat.format("{0}/{1}/{2}", version, type.getSimpleName(), id);
        }
    }
}
