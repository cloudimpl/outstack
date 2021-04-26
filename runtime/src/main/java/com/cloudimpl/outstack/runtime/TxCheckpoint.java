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
public class TxCheckpoint {
    private final String rootTrn;
    private long seq;

    public TxCheckpoint(String rootTrn) {
        this.rootTrn = rootTrn;
        this.seq = 0;
    }
    
    public void setSeq(long seq)
    {
        this.seq = seq;
    }

    public long getSeq() {
        return seq;
    }
    
    
}
