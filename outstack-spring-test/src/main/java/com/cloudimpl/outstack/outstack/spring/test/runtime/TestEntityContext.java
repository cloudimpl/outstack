package com.cloudimpl.outstack.outstack.spring.test.runtime;

import com.cloudimpl.outstack.outstack.spring.test.ResourceLoader;
import com.cloudimpl.outstack.outstack.spring.test.exception.TestRuntimeException;

public class TestEntityContext {

    private static final ResourceLoader resourceLoader;

    static  {
        resourceLoader = new ResourceLoader();
    }

    public void execute(String commandHandlerType, String entityType) {
        validateExecution(commandHandlerType, entityType);
    }

    private void validateExecution(String commandHandlerType, String entityType) {
        if (!resourceLoader.getMapCmdHandlers().containsKey(commandHandlerType)) {
            throw TestRuntimeException.getCommandHandlerNotFoundException(commandHandlerType);
        }

        if (!resourceLoader.getMapEntities().containsKey(entityType)) {
            throw TestRuntimeException.getEntityNotFoundException(entityType);
        }
    }

}
