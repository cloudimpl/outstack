package com.cloudimpl.outstack.outstack.spring.test.exception;

import java.lang.reflect.Field;

public class TestRuntimeException extends RuntimeException {

    public TestRuntimeException(String message) {
        super(message);
    }

    public static TestRuntimeException getInstantiationFailureException(Class<?> classType) {
        return new TestRuntimeException(String.format("Cannot instantiate : %s", classType));
    }

    public static TestRuntimeException getCannotSetValueToTheFieldException(Field field) {
        return new TestRuntimeException(String.format("Cannot set value to the field : %s", field.getName()));
    }

    public static TestRuntimeException getCannotGetValueFromTheFieldException(Field field) {
        return new TestRuntimeException(String.format("Cannot get value from the field : %s", field.getName()));
    }

    public static TestRuntimeException getCommandHandlerNotFoundException(String commandHandlerType) {
        return new TestRuntimeException(String.format("Command handler type [%s] not found", commandHandlerType));
    }

    public static TestRuntimeException getEntityNotFoundException(String entityType) {
        return new TestRuntimeException(String.format("Entity type [%s] not found", entityType));
    }

    public static TestRuntimeException getTestInitializationFailedException() {
        return new TestRuntimeException("Test initialization failed");
    }
}
