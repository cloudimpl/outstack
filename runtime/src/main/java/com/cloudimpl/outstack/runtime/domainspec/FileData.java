package com.cloudimpl.outstack.runtime.domainspec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * File data
 *
 * @author roshanmadhushanka
 **/
public class FileData {

    private final String fileName;
    private final String content;
    private final String mimeType;

    public FileData(String fileName, String content, String mimeType) {
        this.fileName = fileName;
        this.content = content;
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentBase64() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        if (content == null) {
            return ByteArrayInputStream.nullInputStream();
        }
        return new ByteArrayInputStream(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)));
    }

    public int getContentLength() {
        if (content == null) {
            return 0;
        }
        return Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)).length;
    }
}
