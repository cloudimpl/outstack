/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

/**
 *
 * @author nuwan
 */
public class EntityCheckpoint {
    private final String rootTrn;
    private long seq;

    public EntityCheckpoint(String rootTrn) {
        this.rootTrn = rootTrn;
        this.seq = 0;
    }
    
    public EntityCheckpoint setSeq(long seq)
    {
        this.seq = seq;
        return this;
    }

    public long getSeq() {
        return seq;
    }

    public String getRootTrn() {
        return rootTrn;
    }
    
    
}
