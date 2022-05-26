package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.node.ServiceException;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ReactiveService implements Function<CloudMessage, Publisher<Object>> {

    private final Map<String, Method> declaredMethods;

    protected final ValidatorFactory factory;
    protected final Validator validator;
    public ReactiveService() {
        factory = Validation.buildDefaultValidatorFactory();
        this.validator = this.factory.getValidator();
        declaredMethods = Arrays.stream(getClass().getDeclaredMethods())
                .collect(Collectors.toMap(method -> method.getName(), method -> method));
    }

    @SneakyThrows
    @Override
    public Publisher apply(CloudMessage cloudMessage) {
        try
        {
            final String methodName = cloudMessage.attr(CloudMessage.METHOD_STR);
            validator.validate(cloudMessage.data());
            return Mono.justOrEmpty(declaredMethods.get(methodName))
                    .switchIfEmpty(Mono.defer(() -> Mono.error(new ServiceException("Unknown Method"))))
                    .flatMapMany(method -> invokeMethod(method, cloudMessage.data()));
        }catch (Throwable thr)
        {
            return Mono.error(thr);
        }
    }

    private <T> void validateObject(T target) {
        Set<ConstraintViolation<T>> violations = this.validator.validate(target);
        if (!violations.isEmpty()) {
            ValidationErrorException error = new ValidationErrorException(violations.stream().findFirst().get().getMessage());
            throw error;
        }
        if(target.getClass().getAnnotation(TenantOnly.class) != null && target instanceof TenantAwareReactiveRequest)
        {
            if(TenantAwareReactiveRequest.class.cast(target).getTenantId() == null)
            {
                throw new ReactiveServiceException("tenantId is null");
            }
        }
    }

    @SneakyThrows
    private Publisher invokeMethod(Method method, Object object) {

        Object returnObject;

        if (Objects.isNull(object)) {
            returnObject = method.invoke(this);
        } else {
            returnObject = method.invoke(this, object);
        }

        if (returnObject instanceof Mono) {
            return (Mono) returnObject;
        } else if (returnObject instanceof Flux) {
            return (Flux) returnObject;
        } else {
            return Mono.just(returnObject);
        }
    }
}
