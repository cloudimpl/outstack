/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.runtime.iam;

/**
 *
 * @author nuwan
 */
public class ActionDescriptor {

    public enum ActionScope {
        ALL,
        PREFIX_MATCH,
        EXACT_NAME
    }

    private final String name;
    private final ActionScope actionScope;

    public ActionDescriptor(String name, ActionScope actionScope) {
        this.name = name;
        this.actionScope = actionScope;
    }

    public ActionScope getActionScope() {
        return actionScope;
    }

    public String getName() {
        return name;
    }

    public boolean isActionMatched(String action) {
        switch (actionScope) {
            case ALL: {
                return true;
            }
            case PREFIX_MATCH: {
                return action.equalsIgnoreCase(name);
            }
            case EXACT_NAME: {
                return action.equalsIgnoreCase(name);
            }
            default: {
                return false;
            }
        }
    }

}