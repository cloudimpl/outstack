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
 * @param <T>
 */
public abstract class ChildEntity<T extends RootEntity> extends Entity {

    private String _rootTid;

    public void setRootTid(String _rootTid) {
        this._rootTid = _rootTid;
    }

    public String rootTid() {
        return this._rootTid;
    }

    public abstract String rootId();

    public abstract Class<T> rootType();

    @Override
    public String getTRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", ITenant.class.cast(this).getTenantId(), rootType().getSimpleName(), rootTid(), getClass().getSimpleName(), tid());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}", rootType().getSimpleName(), rootTid(), getClass().getSimpleName(), tid());
        }
    }
    
    @Override
    public String getRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", ITenant.class.cast(this).getTenantId(), rootType().getSimpleName(),rootTid(), getClass().getSimpleName(), id());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}", rootType().getSimpleName(), rootTid(), getClass().getSimpleName(), id());
        }
    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> String makeRN(Class<R> rootType,String rootTid,Class<T> childType,String entityId, String tenantId)
    {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}", tenantId, rootType.getSimpleName(), rootTid,childType.getSimpleName(),entityId);
        } else {
            return MessageFormat.format("{1}/{2}/{3}/{4}", rootType.getSimpleName(), rootTid,childType.getSimpleName(),entityId);
        }
    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> String makeTRN(Class<R> rootType,String rootTid,Class<T> childType,String entityTid, String tenantTid)
    {
        if (tenantTid != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}", tenantTid, rootType.getSimpleName(), rootTid,childType.getSimpleName(),entityTid);
        } else {
            return MessageFormat.format("{1}/{2}/{3}/{4}", rootType.getSimpleName(), rootTid,childType.getSimpleName(),entityTid);
        }
    }
    
    public static final ChildEntity DELETED = new ChildEntity() {
        @Override
        public String rootId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Class rootType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String id() {
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
