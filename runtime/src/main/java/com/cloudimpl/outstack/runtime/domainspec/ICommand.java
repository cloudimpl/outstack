/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Map;

/**
 *
 * @author nuwan
 */
public interface ICommand {
    String commandName();
    String version();
    <T extends Command> T unwrap(Class<T> type);
    Map<String, String> getMapAttr();
}
