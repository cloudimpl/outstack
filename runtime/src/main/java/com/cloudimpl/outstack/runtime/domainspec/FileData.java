package com.cloudimpl.outstack.runtime.domainspec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * File data
 *
 * @author roshanmadhushanka
 **/
public class FileData {

    private final String fileName;
    private final ByteArrayOutputStream outputStream;

    public FileData(String fileName, ByteArrayOutputStream outputStream) {
        this.fileName = fileName;
        this.outputStream = outputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        if (outputStream == null) {
            return ByteArrayInputStream.nullInputStream();
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public int getContentLength() {
        if (outputStream == null) {
            return 0;
        }
        return outputStream.toByteArray().length;
    }
}
