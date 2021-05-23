package com.cloudimpl.outstack.outstack.spring.test.runtime;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.outstack.spring.test.ResourceLoader;
import com.cloudimpl.outstack.outstack.spring.test.data.EntityUtil;
import com.cloudimpl.outstack.outstack.spring.test.exception.TestRuntimeException;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;
import com.cloudimpl.outstack.spring.component.SpringService;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class TestContext<T extends RootEntity> {

    private static final ResourceLoader resourceLoader;

    static  {
        resourceLoader = new ResourceLoader();
    }

    private Class<? extends RootEntity> rootEntityType;
    private EntityCommandHandler<? extends Entity, ? extends Command, ?> entityCommandHandler;
    private Class<? extends Entity> entityType;
    private String entityVersion;
    private Exception contextException;
    private List<Event> events;
    private SpringService<? extends RootEntity> springService;

    public TestContext() {
        this.events = new ArrayList<>();
    }

    public void initialize(String commandHandlerTypeString, String entityTypeString) {
        validateExecution(commandHandlerTypeString, entityTypeString);

        Class<? extends EntityCommandHandler<? extends Entity, ? extends Command, ?>> commandHandlerType =
                resourceLoader.getMapCmdHandlers().get(commandHandlerTypeString);

        try {
            this.entityCommandHandler = commandHandlerType.getConstructor().newInstance();
        } catch (Exception e) {
            throw TestRuntimeException.getTestInitializationFailedException();
        }

        this.entityType = entityCommandHandler.getEntityType();
        this.entityVersion = EntityUtil.getVersion(entityType);

        if (RootEntity.isMyType(entityCommandHandler.getEntityType())) {
            RootEntity instance = (RootEntity) EntityUtil.getInstance(entityCommandHandler.getEntityType());
            this.rootEntityType = instance.getClass();
        } else {
            ChildEntity<?> childEntity = (ChildEntity<?>) EntityUtil.getInstance(entityCommandHandler.getEntityType());
            this.rootEntityType = childEntity.rootType();
        }

        ResourceHelper resourceHelper = new ResourceHelper("cloudimpl", "test", "api");
        EventRepositoryFactory eventRepositoryFactory = new MemEventRepositoryFactory(resourceHelper);
        this.springService = new SpringService<>(rootEntityType, eventRepositoryFactory);
        this.springService.getServiceProvider().registerCommandHandler(commandHandlerType);
        this.springService.getServiceProvider().registerDefaultCmdHandlersForEntity(entityType);
    }

    private void validateExecution(String commandHandlerType, String entityType) {
        if (!resourceLoader.getMapCmdHandlers().containsKey(commandHandlerType)) {
            throw TestRuntimeException.getCommandHandlerNotFoundException(commandHandlerType);
        }

        if (!resourceLoader.getMapEntities().containsKey(entityType)) {
            throw TestRuntimeException.getEntityNotFoundException(entityType);
        }
    }

    public Object executeCommand(ICommand command) {
        Object block = Mono.from(springService.getServiceProvider().apply(command))
                .doOnError(e -> {
                    this.contextException = (Exception) e;
                })
                .doOnNext(e -> {
                })
                .block();
        return block;
    }

    public String getEntityVersion() {
        return entityVersion;
    }

    public Exception getContextException() {
        return contextException;
    }

    public void setContextException(RuntimeException contextException) {
        this.contextException = contextException;
    }

    public<K extends Exception> boolean hasExceptionThrown(Class<K> exception) {
        if (exception == null) {
            return this.contextException != null;
        }

        return exception.isInstance(this.contextException);
    }

    public List<Event> getEvents() {
        return events;
    }
}
