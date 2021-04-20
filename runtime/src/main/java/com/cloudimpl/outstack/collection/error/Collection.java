package com.cloudimpl.outstack.collection.error;


public enum Collection implements com.cloudimpl.error.core.ErrorCode {
    RELECTION_EXCEPTION(1000,"reflection error"),
    ROOT_DOESNT_EXIST(1001,"[entity]:[id] doesn't exist"),
    INVALID_OWNER(1002,"event [event] apply failed for entity [entity], invalid owner [owner]");
    
    private final int errorNo ;
    private final String format ;

    @Override
    public int getErrorNo() {
        return this.errorNo ;
    }

    @Override
    public String getFormat() {
        return this.format ;
    }

    Collection(int errorNo, String errorFormat) {
        this.errorNo = errorNo;
        this.format = errorFormat;
    }
}
