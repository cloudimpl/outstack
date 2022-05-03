package com.cloudimpl.outstack.spring.util;

import com.cloudimpl.outstack.runtime.domainspec.FileData;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.server.ServerErrorException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Base64;
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
                .map(fileData -> MimeType.valueOf(tika.detect(fileData.getMimeType())))
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
     * @return : Mono<FileData> File data
     */
    public static Mono<FileData> getFileData(FilePart filePart) {
        return transferByteArrayOutputStream(filePart)
                .map(byteArrayOutputStream -> {
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    return new FileData(filePart.filename(), Base64.getEncoder().encodeToString(bytes), tika.detect(bytes));
                });
    }

    private static Mono<ByteArrayOutputStream> transferByteArrayOutputStream(FilePart filePart) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(30*1024*1024);
        return filePart.content().doOnNext(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            try {
                byteArrayOutputStream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).doOnTerminate(() -> {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                throw new ServerErrorException("cannot read file stream", e);
            }
        }).then(Mono.just(byteArrayOutputStream));
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
