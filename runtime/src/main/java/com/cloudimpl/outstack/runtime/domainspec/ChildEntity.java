/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.text.MessageFormat;
import java.util.Objects;

/**
 *
 * @author nuwan
 * @param <T>
 */
public abstract class ChildEntity<T extends RootEntity> extends Entity {

    private String _rootId;

    public final void setRootId(String rootId) {
        this._rootId = rootId;
    }

    public String rootId() {
        return this._rootId;
    }

    public abstract Class<T> rootType();

    @Override
    public String getTRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            Objects.requireNonNull(ITenant.class.cast(this).getTenantId());
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", ITenant.class.cast(this).getTenantId(),getMeta().getVersion(), rootType().getSimpleName(), rootId(), getClass().getSimpleName(), id());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",getMeta().getVersion(), rootType().getSimpleName(), rootId(), getClass().getSimpleName(), id());
        }
    }
    
    @Override
    public String getBRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            Objects.requireNonNull(ITenant.class.cast(this).getTenantId());
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", ITenant.class.cast(this).getTenantId(),getMeta().getVersion(), rootType().getSimpleName(),rootId(), getClass().getSimpleName(), entityId());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",getMeta().getVersion(), rootType().getSimpleName(), rootId(), getClass().getSimpleName(), entityId());
        }
    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> String makeRN(Class<R> rootType,String version,String rootId,Class<T> childType,String entityId, String tenantId)
    {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", tenantId,version, rootType.getSimpleName(), rootId,childType.getSimpleName(),entityId);
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",version, rootType.getSimpleName(), rootId,childType.getSimpleName(),entityId);
        }
    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> String makeTRN(Class<R> rootType,String version,String rootId,Class<T> childType,String id, String tenantId)
    {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", tenantId,version, rootType.getSimpleName(), rootId,childType.getSimpleName(),id);
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}/{4}",version, rootType.getSimpleName(), rootId,childType.getSimpleName(),id);
        }
    }
    
//    public static final ChildEntity DELETED = new ChildEntity() {
//
//        @Override
//        public Class rootType() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public String entityId() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        protected void apply(Event event) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public String idField() {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//    };
}
