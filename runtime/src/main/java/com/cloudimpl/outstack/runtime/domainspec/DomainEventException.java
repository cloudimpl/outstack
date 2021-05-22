/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public class DomainEventException extends RuntimeException{

    public enum ErrorCode
    {
        ENTITY_NOT_FOUND,
        ENTITY_EXIST,
        EXPECT_ENTITY_ID,
        EXPECT_TECHNICAL_ID,
        BASIC_VIOLATION,
        ENTITY_EVENT_RELATION_VIOLATION,
        TECHNICAL_ID_MISMATCHED,
        ENTITY_ID_MISMATCHED,
        INVALID_DOMAIN_EVENT,
        TENANT_ID_NOT_AVAILABLE,
        TENANT_ID_NOT_APPLICABLE,
        INVALID_ENTITY_TYPE,
        UNHANDLED_EVENT,
        INVALID_VERSION,
        NOT_A_REF_ID
    }
    
    private final ErrorCode errCode;
    public DomainEventException(ErrorCode code,String format,Object... args) {
        super(MessageFormat.format(format, args));
        this.errCode = code;
    }

    public ErrorCode getErrCode() {
        return errCode;
    }
    
    
}
