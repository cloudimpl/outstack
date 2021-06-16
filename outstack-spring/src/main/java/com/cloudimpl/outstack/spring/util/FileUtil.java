package com.cloudimpl.outstack.spring.util;

import com.cloudimpl.outstack.runtime.ValidationErrorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.server.ServerErrorException;

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

    public static void validateMimeType(List<FilePart> fileParts, Set<String> acceptedMimetypes) {
        if (CollectionUtils.isEmpty(fileParts)) {
            return;
        }

        if (CollectionUtils.isEmpty(acceptedMimetypes)) {
            acceptedMimetypes = Collections.emptySet();
        }

        final Set<MimeType> acceptedMimeTypeSet = acceptedMimetypes.stream()
                .filter(org.apache.tika.mime.MimeType::isValid)
                .map(MimeType::valueOf)
                .collect(Collectors.toSet());

        boolean containsInvalidMimeType = fileParts.stream()
                .map(FileUtil::getInputStream)
                .map(inputStream -> {
                    try {
                        return MimeType.valueOf(tika.detect(inputStream));
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

    private static InputStream getInputStream(FilePart filePart) {
        if (filePart == null) {
            return InputStream.nullInputStream();
        }
        return filePart.content()
                .map(DataBuffer::asInputStream)
                .reduce(SequenceInputStream::new)
                .block();
    }
}
