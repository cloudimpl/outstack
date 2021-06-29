package com.cloudimpl.outstack.spring.util;

import com.cloudimpl.outstack.runtime.domainspec.FileData;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.server.ServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * File util
 *
 * @author roshanmadhushanka
 **/
@Slf4j
public class FileUtil {

    private static final Tika tika;

    static {
        tika = new Tika();
    }

    public static void validateMimeType(List<FileData> fileDataList, Set<String> acceptedMimetypes) {
        if (CollectionUtils.isEmpty(fileDataList)) {
            return;
        }

        if (CollectionUtils.isEmpty(acceptedMimetypes)) {
            acceptedMimetypes = Collections.emptySet();
        }

        final Set<MimeType> acceptedMimeTypeSet = acceptedMimetypes.stream()
                .filter(org.apache.tika.mime.MimeType::isValid)
                .map(MimeType::valueOf)
                .collect(Collectors.toSet());

        boolean containsInvalidMimeType = fileDataList.stream()
                .map(fileData -> {
                    try {
                        return MimeType.valueOf(tika.detect(fileData.getInputStream()));
                    } catch (IOException e) {
                        throw new ServerErrorException("mimetype detection failure", e);
                    }
                })
                .anyMatch(e -> !acceptedMimeTypeSet.contains(e));
        if (containsInvalidMimeType) {
            throw new ValidationErrorException(String.format("unsupported mimetypes detected. accepts {%s} only",
                    acceptedMimeTypeSet));
        }
    }

    /**
     * Get file data from file part
     *
     * @param filePart : [FilePart] File part
     * @return : [FileData] File data
     */
    public static FileData getFileData(FilePart filePart) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            getInputStream(filePart).transferTo(byteArrayOutputStream);
        } catch (IOException e) {
            throw new ServerErrorException("cannot read file stream", e);
        }
        return new FileData(filePart.filename(), byteArrayOutputStream);
    }

    /**
     * Get input stream from file part
     *
     * @param filePart : [FilePart] File part
     * @return : [InputStream] Input stream
     */
    private static InputStream getInputStream(FilePart filePart) {
        if (filePart == null) {
            return InputStream.nullInputStream();
        }
        return filePart.content()
                .map(DataBuffer::asInputStream)
                .reduce(SequenceInputStream::new)
                .block();
    }

    /**
     * Get input stream from byte array output stream
     *
     * @param byteArrayOutputStream : [ByteArrayOutputStream] Byte array output stream
     * @return : [InputStream] Input stream
     */
    public static InputStream getInputStream(ByteArrayOutputStream byteArrayOutputStream) {
        if (byteArrayOutputStream == null || byteArrayOutputStream.size() == 0) {
            return InputStream.nullInputStream();
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
