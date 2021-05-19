package com.cloudimpl.outstack.outstack.spring.test.runtime;

import com.cloudimpl.outstack.outstack.spring.test.ResourceLoader;
import com.cloudimpl.outstack.outstack.spring.test.exception.TestRuntimeException;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EventRepository;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;

import java.util.ArrayList;
import java.util.List;

public class TestContext<T extends RootEntity> {

    private static final ResourceLoader resourceLoader;

    static  {
        resourceLoader = new ResourceLoader();
    }

    private final Class<T> rootEntityType;
    private EntityCommandHandler<? extends Entity, ? extends Command, ?> entityCommandHandler;
    private EventRepository<T> repository;
    private MockEntityContextProvider<T> entityContextProvider;
    private RuntimeException runtimeException;
    private List<Event> events;

    public TestContext(Class<T> rootEntityType) {
        this.rootEntityType = rootEntityType;
        this.events = new ArrayList<>();
    }

    public void initialize(String commandHandlerTypeString, String entityTypeString) {
        validateExecution(commandHandlerTypeString, entityTypeString);

        Class<? extends EntityCommandHandler<? extends Entity, ? extends Command, ?>> commandHandlerType = resourceLoader.getMapCmdHandlers().get(commandHandlerTypeString);
        try {
            this.entityCommandHandler = commandHandlerType.getConstructor().newInstance();
        } catch (Exception e) {
            throw TestRuntimeException.getTestInitializationFailedException();
        }

        ResourceHelper resourceHelper = new ResourceHelper("cloudimpl", "test", "api");
        EventRepositoryFactory eventRepositoryFactory = new MemEventRepositoryFactory(resourceHelper);
        this.repository = eventRepositoryFactory.createRepository(rootEntityType);
        this.entityContextProvider = new MockEntityContextProvider<T>(rootEntityType,
                repository::loadEntityWithClone, repository::generateTid, repository, eventRepositoryFactory::createRepository);
    }

    private void validateExecution(String commandHandlerType, String entityType) {
        if (!resourceLoader.getMapCmdHandlers().containsKey(commandHandlerType)) {
            throw TestRuntimeException.getCommandHandlerNotFoundException(commandHandlerType);
        }

        if (!resourceLoader.getMapEntities().containsKey(entityType)) {
            throw TestRuntimeException.getEntityNotFoundException(entityType);
        }
    }

    public MockEntityContextProvider<T> getEntityContextProvider() {
        return entityContextProvider;
    }

    public void executeCommand(EntityCommandHandler<? extends Entity, ? extends Command, ? extends Entity> commandHandler,
                               ICommand command) {
        EntityContext<? extends Entity> emit = commandHandler.emit(entityContextProvider, command);
        events.addAll(emit.getEvents());
        repository.saveTx(entityContextProvider.getTransaction());
    }

    public boolean isExistRootEntity(String rootId) {
        return repository.getRootById(rootEntityType, rootId, null).isPresent();
    }

    public boolean isExistChildEntity(String rootId, String childId) {
        return repository.getRootById(rootEntityType, rootId, childId).isPresent();
    }

    public RuntimeException getRuntimeException() {
        return runtimeException;
    }

    public void setRuntimeException(RuntimeException runtimeException) {
        this.runtimeException = runtimeException;
    }

    public<K extends RuntimeException> boolean hasExceptionThrown(Class<K> exception) {
        if (exception == null) {
            return runtimeException != null;
        }

        return exception.isInstance(runtimeException);
    }

    public List<Event> getEvents() {
        return events;
    }
}
