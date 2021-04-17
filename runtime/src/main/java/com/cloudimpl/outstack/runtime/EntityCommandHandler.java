/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.Entity;

/**
 *
 * @author nuwansa
 * @param <E>
 * @param <C>
 */
public abstract class EntityCommandHandler<E extends Entity,C extends Command,R> extends CommandHandler<C, R>{

}
