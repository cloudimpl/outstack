package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.commands.OrganizationLogoUploadRequest;
import com.cloudimpl.outstack.runtime.EnableFileUpload;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;

import java.util.Optional;

/**
 * Upload organization logo
 *
 * This is a test implementation for the organization log update
 *
 * @author roshanmadhushanka
 **/
@Slf4j
@EnableFileUpload(mimeTypes = {"image/jpeg"})
public class UploadOrganizationLogo extends EntityCommandHandler<Organization, OrganizationLogoUploadRequest, Organization> {

    @Override
    protected Organization execute(EntityContext<Organization> context, OrganizationLogoUploadRequest command) {
        log.info("Receiving files for the organization : {}", command.rootId());
        Optional<RootEntity> optionalRootEntity = context.asRootContext().getEntityById(command.rootId());

        if (optionalRootEntity.isEmpty()) {
            throw new ResourceNotFoundException("Organization {0} not exists", command.rootId());
        }

        command.getFiles().forEach(e -> {
            if (e instanceof FilePart) {
                FilePart filePart = (FilePart) e;
                log.info("Received : {}", filePart.filename());
            }
        });

        return new Organization("xxxx-logo-upload-example-org");
    }
}
