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
        if (hasTenant()) { //tenant/1234/User/1/Device/12"
            Objects.requireNonNull(ITenant.class.cast(this).getTenantId());
            return makeRN(this.getClass(), entityId(),ITenant.class.cast(this).getTenantId());
        } else {
            return makeRN(this.getClass(), entityId(), null);
        }
    }
    
    @Override
    public final String getTRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            Objects.requireNonNull(ITenant.class.cast(this).getTenantId());
            return makeTRN(this.getClass(), id(), ITenant.class.cast(this).getTenantId());
        } else {
            return makeTRN(this.getClass(),id(),null);
        }
    }
    
    public <T extends ChildEntity> T createChildEntity(Class<T> type, String entityId, String id) {
        T t;
        if (hasTenant()) {
            t = Util.createObject(type, new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(entityId, ITenant.class.cast(this).getTenantId()));
        } else {
            t = Util.createObject(type, new Util.VarArg<>(String.class), new Util.VarArg<>(entityId));
        }
        EntityHelper.updateId(t, id);
        EntityHelper.updateRootId(t, id());
        return t;
    }
    
    public static <T extends RootEntity> T create(Class<T> type, String entityId, String tenantId, String tid) {
        T root;
        if (hasTenant(type)) {
            root =  Util.createObject(type, new Util.VarArg<>(String.class,String.class), new Util.VarArg<>(entityId,tenantId));
        }
        else
        {
            root =  Util.createObject(type, new Util.VarArg<>(String.class), new Util.VarArg<>(entityId));
        }
        EntityHelper.updateId(root, tid);
        return root;
    }
    
    public static boolean isMyType(Class<? extends Entity> type) {
        return RootEntity.class.isAssignableFrom(type);
    }
    
    public static String makeRN(Class<? extends RootEntity> type, String entityId, String tenantId) {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}", tenantId, type.getSimpleName(), entityId);
        } else {
            return MessageFormat.format("{0}/{1}", type.getSimpleName(), entityId);
        }
    }
    
    public static String makeTRN(Class<? extends RootEntity> type, String id, String tenantId) {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}", tenantId, type.getSimpleName(), id);
        } else {
            return MessageFormat.format("{0}/{1}", type.getSimpleName(), id);
        }
    }
    
    public static final RootEntity DELETED = new RootEntity() {
        @Override
        public String entityId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected void apply(Event event) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String idField() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
}
