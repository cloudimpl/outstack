/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public class RepositoryException extends RuntimeException{

    public RepositoryException(String format,Object... args) {
        super(MessageFormat.format(format, args));
    }

    public RepositoryException(Throwable thr) {
        super(thr);
    }
    
    
    
}
