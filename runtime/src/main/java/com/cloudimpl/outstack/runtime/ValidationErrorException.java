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
public class ValidationErrorException extends RuntimeException{

    public ValidationErrorException(String msg) {
        super(msg);
    }
 
}
