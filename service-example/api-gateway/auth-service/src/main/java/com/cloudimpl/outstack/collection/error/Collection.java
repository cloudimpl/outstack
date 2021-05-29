package com.cloudimpl.outstack.collection.error;


public enum Collection implements com.cloudimpl.error.core.ErrorCode {
    ;
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
