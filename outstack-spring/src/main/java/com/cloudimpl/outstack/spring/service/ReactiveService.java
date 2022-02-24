package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.node.ServiceException;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ReactiveService implements Function<CloudMessage, Publisher<Object>> {

    private final Map<String, Method> declaredMethods;

    public ReactiveService() {
        declaredMethods = Arrays.stream(getClass().getDeclaredMethods())
                .collect(Collectors.toMap(method -> method.getName(), method -> method));
    }

    @SneakyThrows
    @Override
    public Publisher apply(CloudMessage cloudMessage) {
        final String methodName = cloudMessage.attr(CloudMessage.METHOD_STR);
        return Mono.justOrEmpty(declaredMethods.get(methodName))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ServiceException("Unknown Method"))))
                .flatMapMany(method -> invokeMethod(method, cloudMessage.data()));
    }

    @SneakyThrows
    private Publisher invokeMethod(Method method, Object object) {
        Object returnObject = method.invoke(this, object);
        if (returnObject instanceof Mono) {
            return (Mono) returnObject;
        } else if (returnObject instanceof Flux) {
            return (Flux) returnObject;
        } else {
            return Mono.just(returnObject);
        }
    }
}
