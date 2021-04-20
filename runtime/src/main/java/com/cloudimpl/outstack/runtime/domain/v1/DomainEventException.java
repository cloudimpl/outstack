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
 */
public class DomainEventException extends RuntimeException{

    public DomainEventException(String format,Object... args) {
        super(MessageFormat.format(format, args));
    }
}
