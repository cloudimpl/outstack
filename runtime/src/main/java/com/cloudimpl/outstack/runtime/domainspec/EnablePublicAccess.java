package com.cloudimpl.outstack.runtime.domainspec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable public access
 *
 * @author roshanmadhushanka
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnablePublicAccess {
}
