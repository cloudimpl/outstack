/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;

/**
 *
 * @author nuwan
 */
public class EntitySnapshot {
    private final Entity entity;
    private final long seq;
    private boolean isDirty;
    public EntitySnapshot(Entity entity, long seq,boolean dirty) {
        this.entity = entity;
        this.seq = seq;
        this.isDirty = dirty;
    }

    public long getSeq() {
        return seq;
    }

    public <T extends Entity> T getEntity() {
        return (T) entity;
    }
    
    public void setDirty(boolean dirty)
    {
        this.isDirty = dirty;
    }

    public boolean isDirty() {
        return isDirty;
    }
    
    
}
