package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.domain.example.commands.TenantLogUploadRequest;
import com.cloudimpl.outstack.runtime.EnableFileUpload;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;

import java.util.Optional;

/**
 * @author roshanmadhushanka
 **/
@Slf4j
@EnableFileUpload(mimeTypes = {"image/jpeg"})
public class UploadTenantLogo extends EntityCommandHandler<Tenant, TenantLogUploadRequest, Tenant> {

    @Override
    protected Tenant execute(EntityContext<Tenant> context, TenantLogUploadRequest command) {
        log.info("Receiving files for the tenant : {}", command.id());
        Optional<ChildEntity<RootEntity>> optionalChildEntity = context.asChildContext().getEntityById(command.id());

        if (optionalChildEntity.isEmpty()) {
            throw new ResourceNotFoundException("Tenant {0} not exists", command.rootId());
        }

        command.getFiles().forEach(e -> {
            if (e instanceof FilePart) {
                FilePart filePart = (FilePart) e;
                log.info("Received : {}", filePart.filename());
            }
        });
        return new Tenant("xxxx-logo-upload-example-ten");
    }
}
