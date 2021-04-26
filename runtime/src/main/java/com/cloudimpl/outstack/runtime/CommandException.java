/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public class CommandException extends RuntimeException{

    public CommandException(String format,Object... args) {
        super(MessageFormat.format(format, args));
    }
    
}
