/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.util;

import com.cloudimpl.outstack.app.ServiceMeta;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.core.CloudFunctionMeta;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.CommandHandler;
import com.cloudimpl.outstack.runtime.EnableFileUpload;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.Handler;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.EnablePublicAccess;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import com.cloudimpl.outstack.spring.component.SpringQueryService;
import com.cloudimpl.outstack.spring.component.SpringService;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author nuwan
 */
public class SpringUtil {

    public static ServiceMeta serviceProviderMeta(ResourceHelper resourceHelper, Class<? extends SpringService> funcType) {
        CloudFunction func = funcType.getAnnotation(CloudFunction.class);
        Objects.requireNonNull(func);
        Router router = funcType.getAnnotation(Router.class);
        Objects.requireNonNull(func);
        Util.classForName(funcType.getName()); //TODO check class loading issue
        Map<String, String> attr = new HashMap<>();
        SpringServiceDescriptor srvDesc = getServiceDescription(resourceHelper.getApiContext(),resourceHelper.getDomainOwner(),resourceHelper.getDomainContext(), func.name(), funcType);
        attr.put("serviceMeta", GsonCodec.encode(srvDesc));
        //attr.put("plural", value)
        return new ServiceMeta(funcType, new CloudFunctionMeta(srvDesc.getServiceName(), ""), router, attr);
    }

    public static ServiceMeta serviceQueryProviderMeta(ResourceHelper resourceHelper, Class<? extends SpringQueryService> funcType) {
        CloudFunction func = funcType.getAnnotation(CloudFunction.class);
        Objects.requireNonNull(func);
        Router router = funcType.getAnnotation(Router.class);
        Objects.requireNonNull(func);
        Util.classForName(funcType.getName()); //TODO check class loading issue
        Map<String, String> attr = new HashMap<>();
        SpringServiceDescriptor srvDesc = getQueryServiceDescription(resourceHelper.getApiContext(),resourceHelper.getDomainOwner(),resourceHelper.getDomainContext(), func.name(), funcType);
        attr.put("serviceQueryMeta", GsonCodec.encode(srvDesc));
        //attr.put("plural", value)
        return new ServiceMeta(funcType, new CloudFunctionMeta(srvDesc.getServiceName(), ""), router, attr);
    }

    public static SpringServiceDescriptor getServiceDescription(String appContext,String domainOwner,String domainContext, String serviceName, Class<? extends SpringService> serviceType) {
        Class<? extends RootEntity> rootType = Util.extractGenericParameter(serviceType, SpringService.class, 0);
        EntityMeta entityMeta = rootType.getAnnotation(EntityMeta.class);
        Collection<Class<? extends CommandHandler<?>>> handlers = SpringService.handlers(rootType);
        SpringServiceDescriptor desc = new SpringServiceDescriptor(appContext,domainOwner,domainContext,domainOwner+"/"+domainContext+"/"+entityMeta.version()+"/"+ serviceName, rootType.getSimpleName(), entityMeta.version(), entityMeta.plural(), Entity.checkTenantRequirement(rootType));
        handlers.stream().filter(h -> EntityCommandHandler.class.isAssignableFrom(h)).forEach(h -> {
            Class<? extends Entity> eType = Util.extractGenericParameter(h, EntityCommandHandler.class, 0);
            EntityMeta eMeta = eType.getAnnotation(EntityMeta.class);
            SpringServiceDescriptor.EntityDescriptor entityDesc = new SpringServiceDescriptor.EntityDescriptor(eType.getSimpleName(), eMeta.plural());

            com.cloudimpl.outstack.runtime.handler.Handler hnd = h.getAnnotation(com.cloudimpl.outstack.runtime.handler.Handler.class) ;
            boolean idRequired = hnd != null ? hnd.idRequired() : false;
            boolean fileUploadEnabled = false;
            Set<String> mimeTypes = Collections.emptySet();
            if (h.isAnnotationPresent(EnableFileUpload.class)) {
                fileUploadEnabled = true;
                EnableFileUpload fileUploadMetaData = h.getAnnotation(EnableFileUpload.class);
                mimeTypes = new HashSet<>(Arrays.asList(fileUploadMetaData.mimeTypes()));
            }

            // Verify public accessibility
            boolean isPubliclyAccessible = h.isAnnotationPresent(EnablePublicAccess.class);

            if (eType == rootType) {
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor(h.getSimpleName(),idRequired,
                        SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER, isPubliclyAccessible,
                        fileUploadEnabled, mimeTypes));
            } else {
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor(h.getSimpleName(),idRequired,
                        SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER, isPubliclyAccessible,
                        fileUploadEnabled, mimeTypes));
            }
        });

        handlers.stream().filter(h -> EntityEventHandler.class.isAssignableFrom(h)).forEach(h -> {
            System.out.println("load: " + h.getName());
            Class<? extends Entity> eType = Util.extractGenericParameter(h, EntityEventHandler.class, 0);
            EntityMeta eMeta = eType.getAnnotation(EntityMeta.class);
            com.cloudimpl.outstack.runtime.handler.Handler hnd = h.getAnnotation(com.cloudimpl.outstack.runtime.handler.Handler.class) ;
            boolean idRequired = hnd != null ? hnd.idRequired() : false;
            boolean isPubliclyAccessible = h.isAnnotationPresent(EnablePublicAccess.class);
            SpringServiceDescriptor.EntityDescriptor entityDesc = new SpringServiceDescriptor.EntityDescriptor(eType.getSimpleName(), eMeta.plural());
            if (eType == rootType) {
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor(h.getSimpleName(),idRequired,isPubliclyAccessible, SpringServiceDescriptor.ActionDescriptor.ActionType.EVENT_HANDLER));
            } else {
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor(h.getSimpleName(),idRequired,isPubliclyAccessible, SpringServiceDescriptor.ActionDescriptor.ActionType.EVENT_HANDLER));
            }
        });

        SpringService.cmdEntities(rootType).forEach(type -> {
            EntityMeta eMeta = type.getAnnotation(EntityMeta.class);
            SpringServiceDescriptor.EntityDescriptor entityDesc = new SpringServiceDescriptor.EntityDescriptor(type.getSimpleName(), eMeta.plural());
            if (type == rootType) {
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor("Delete" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER));
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor("Rename" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER));
            } else {
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor("Delete" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER));
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor("Rename" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER));
            }

        });
        return desc;
    }

    public static SpringServiceDescriptor getQueryServiceDescription(String appContext,String domainOwner,String domainContext, String serviceName, Class<? extends SpringQueryService> serviceType) {
        Class<? extends RootEntity> rootType = Util.extractGenericParameter(serviceType, SpringQueryService.class, 0);
        EntityMeta entityMeta = rootType.getAnnotation(EntityMeta.class);
        Collection<Class<? extends Handler<?>>> handlers = SpringQueryService.handlers(rootType);
        SpringServiceDescriptor desc = new SpringServiceDescriptor(appContext,domainOwner,domainContext,domainOwner+"/"+domainContext+"/"+ entityMeta.version()+"/"+ serviceName, rootType.getSimpleName(), entityMeta.version(), entityMeta.plural(), Entity.checkTenantRequirement(rootType));
        handlers.stream().filter(h -> EntityQueryHandler.class.isAssignableFrom(h)).forEach(h -> {
            Class<? extends Entity> eType = Util.extractGenericParameter(h, EntityQueryHandler.class, 0);
            com.cloudimpl.outstack.runtime.handler.Handler hnd = h.getAnnotation(com.cloudimpl.outstack.runtime.handler.Handler.class) ;
            boolean idRequired = hnd != null ? hnd.idRequired() : false;
            boolean isPubliclyAccessible = h.isAnnotationPresent(EnablePublicAccess.class);
            EntityMeta eMeta = eType.getAnnotation(EntityMeta.class);
            SpringServiceDescriptor.EntityDescriptor entityDesc = new SpringServiceDescriptor.EntityDescriptor(eType.getSimpleName(), eMeta.plural());
            if (eType == rootType) {
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor(h.getSimpleName(),idRequired,isPubliclyAccessible, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
            } else {
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor(h.getSimpleName(),idRequired,isPubliclyAccessible, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
            }
        });
        SpringQueryService.queryEntities(rootType).forEach(type -> {
            EntityMeta eMeta = type.getAnnotation(EntityMeta.class);
            SpringServiceDescriptor.EntityDescriptor entityDesc = new SpringServiceDescriptor.EntityDescriptor(type.getSimpleName(), eMeta.plural());
            if (type == rootType) {
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor("Get" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor("Get" + type.getSimpleName()+"Events",true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
                desc.putRootAction(new SpringServiceDescriptor.ActionDescriptor("List" + type.getSimpleName(),false,false, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
            } else {
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor("Get" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor("Get" + type.getSimpleName()+"Events",true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
                desc.putChildAction(entityDesc, new SpringServiceDescriptor.ActionDescriptor("List" + type.getSimpleName(),true,false, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER));
            }

        });
        return desc;
    }

    public static String toMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter
                    .printHexBinary(digest).toUpperCase();
        }catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }

    }
}
