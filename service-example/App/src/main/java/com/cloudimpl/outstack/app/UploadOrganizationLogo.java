package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.commands.OrganizationLogoUploadRequest;
import com.cloudimpl.outstack.runtime.EnableFileUpload;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;

/**
 * Upload organization logo
 *
 * This is a test implementation for the organization log update
 *
 * @author roshanmadhushanka
 **/
@Slf4j
@EnableFileUpload(mimeTypes = "image/*")
public class UploadOrganizationLogo extends EntityCommandHandler<Organization, OrganizationLogoUploadRequest, Organization> {

    @Override
    protected Organization execute(EntityContext<Organization> context, OrganizationLogoUploadRequest command) {
        log.info("Receiving files for the organization : {}", command.rootId());
        command.getFiles().forEach(e -> {
            if (e instanceof FilePart) {
                FilePart filePart = (FilePart) e;
                log.info("Received : {}", filePart.filename());
            }
        });

        Organization organization = new Organization("xxxx-logo-upload-example-org");
        return organization;
    }
}
