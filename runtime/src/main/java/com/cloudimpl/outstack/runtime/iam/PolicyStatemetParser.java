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

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class PolicyStatemetParser {

    public static Pattern RESOURCE_NAME_PATTERN = Pattern.compile("[a-zA-Z_$][a-zA-Z\\d_$]*"); //("[a-zA-Z]+[_[a-zA-Z0-9]]+");
    public static Pattern RESOURCE_ID_PATTERN = Pattern.compile("[a-zA-Z0-9_$][a-zA-Z0-9_-]*|\\{[a-zA-Z0-9_$][a-zA-Z0-9._-]*\\}");

    public static PolicyStatementDescriptor parseStatement(PolicyStatement stmt) {
        if (stmt.getActions() == null) {
            throw new PolicyStatementException("policy statement effectType is null");
        }
        validateResourceName(stmt.getSid(), "statmentID");
        Collection<ActionDescriptor> actions = stmt.getActions().stream().map(action -> parseAction(action)).collect(Collectors.toList());
        if (actions.isEmpty()) {
            throw new PolicyStatementException("policy statement actions are empty");
        }
        Collection<ResourceDescriptor> resources = stmt.getResources().stream().map(resource -> parse(resource)).collect(Collectors.toList());
        if (resources.isEmpty()) {
            throw new PolicyStatementException("policy statement resources are empty");
        }
        return new PolicyStatementDescriptor(stmt.getSid(), stmt.getEffect(), actions, resources);
    }

    public static ResourceDescriptor parse(String resourceDesc) {
        String[] parts = Arrays.asList(resourceDesc.split("/")).stream()
                .map(s -> s.trim())
                .toArray(String[]::new);
        if (parts[0].equalsIgnoreCase("tenant")) {
            return parseTenantResource(parts, resourceDesc);
        } else {
            return parseNonTenantResource(parts, resourceDesc);
        }
    }

    public static ActionDescriptor parseAction(String action) {
        action = action.trim();
        if (action.equals("*")) {
            return new ActionDescriptor(action, ActionDescriptor.ActionScope.ALL);
        } else if (action.endsWith("*")) {
            String prefix = action.substring(0, action.lastIndexOf("*") - 1);
            validateResourceName(prefix, "Action");
            return new ActionDescriptor(prefix, ActionDescriptor.ActionScope.PREFIX_MATCH);

        } else {
            validateResourceName(action, "Action");
            return new ActionDescriptor(action, ActionDescriptor.ActionScope.EXACT_NAME);
        }
    }

    public static ResourceDescriptor parseTenantResource(String[] parts, String resourceDesc) {
        checkMinCount(5, parts.length, "invalid tenant resource pattern. {0}", resourceDesc);
        checkWord("tenant", parts[0], "invalid keyword. 'tenant' keyword expected");
        ResourceDescriptor.TenantScope tenantScope;// = ResourceDescriptor.TenantScope.NONE;
        if (parts[1].equals("*")) {
            tenantScope = ResourceDescriptor.TenantScope.ALL;
        } else {
            validateResourceId(parts[1], "tenantId");
            tenantScope = ResourceDescriptor.TenantScope.TENANT_ID;
        }

        ResourceDescriptor.Builder builder = ResourceDescriptor.builder();
        builder.withTenantId(parts[1]).withTenantScope(tenantScope);
        validateResourceName(parts[2], "version");
        builder.withVersion(parts[2]);
        validateResourceName(parts[3], "RootEntity");
        builder.withRootType(parts[3]);

        ResourceDescriptor.ResourceScope resourceScope;// = ResourceDescriptor.ResourceScope.NONE;
        if (parts[4].equals("*")) {
            resourceScope = ResourceDescriptor.ResourceScope.ALL_ROOT_ID_ONLY;
        } else if (parts[4].equals("**")) {
            resourceScope = ResourceDescriptor.ResourceScope.ALL;
        } else {
            resourceScope = ResourceDescriptor.ResourceScope.ROOT_ID_ONLY;
            validateResourceId(parts[4], "RootId");
        }
        builder.withRootId(parts[4]).withResourceScope(resourceScope);

        if (parts.length == 5) {
            return builder.build();
        }

        if (resourceScope == ResourceDescriptor.ResourceScope.ALL) {
            throw new PolicyStatementException("invalid tenant resource pattern. {0}", resourceDesc);
        }
        checkCount(7, parts.length, "invalid tenant resource pattern. {0}", resourceDesc);
        validateResourceName(parts[5], "ChildType");
        builder.withChildType(parts[5]);

        if (parts[6].equals("*")) {
            if (parts[4].equals("*")) {
                resourceScope = ResourceDescriptor.ResourceScope.ALL_ROOT_ID_CHILD_TYPE_ONLY;
            } else {
                resourceScope = ResourceDescriptor.ResourceScope.ROOT_ID_CHILD_TYPE_ONLY;
            }
        } else {
            validateResourceId(parts[6], "ChildId");
            if (parts[4].equals("*")) {
                resourceScope = ResourceDescriptor.ResourceScope.ALL_ROOT_ID_CHILD_ID_ONLY;
            } else {
                resourceScope = ResourceDescriptor.ResourceScope.ROOT_ID_CHILD_ID_ONLY;
            }
        }
        builder.withChildId(parts[6]);
        builder.withResourceScope(resourceScope);
        return builder.build();
    }

    public static ResourceDescriptor parseNonTenantResource(String[] parts, String resourceDesc) {

        checkMinCount(3, parts.length, "invalid resource pattern. {0}", resourceDesc);
        ResourceDescriptor.Builder builder = ResourceDescriptor.builder();
        validateResourceName(parts[0], "version");
        validateResourceName(parts[1], "RootEntity");

        builder.withVersion(parts[0]);
        builder.withRootType(parts[1]);
        ResourceDescriptor.TenantScope tenantScope = ResourceDescriptor.TenantScope.NONE;
        ResourceDescriptor.ResourceScope resourceScope = ResourceDescriptor.ResourceScope.NONE;
        builder.withTenantScope(tenantScope);
        if (parts[2].equals("*")) {
            resourceScope = ResourceDescriptor.ResourceScope.ALL_ROOT_ID_ONLY;
        } else if (parts[2].equals("**")) {
            resourceScope = ResourceDescriptor.ResourceScope.ALL;
        } else {
            resourceScope = ResourceDescriptor.ResourceScope.ROOT_ID_ONLY;
            validateResourceId(parts[2], "RootId");
        }
        builder.withResourceScope(resourceScope);
        builder.withRootId(parts[2]);
        if (parts.length == 3) {
            return builder.build();
        }

        if (resourceScope == ResourceDescriptor.ResourceScope.ALL) {
            throw new PolicyStatementException("invalid tenant resource pattern. {0}", resourceDesc);
        }
        checkCount(5, parts.length, "invalid tenant resource pattern. {0}", resourceDesc);
        validateResourceName(parts[3], "ChildType");
        builder.withChildType(parts[3]);

        if (parts[4].equals("*")) {
            if (parts[2].equals("*")) {
                resourceScope = ResourceDescriptor.ResourceScope.ALL_ROOT_ID_CHILD_TYPE_ONLY;
            } else {
                resourceScope = ResourceDescriptor.ResourceScope.ROOT_ID_CHILD_TYPE_ONLY;
            }
        } else {
            validateResourceId(parts[4], "ChildId");
            if (parts[4].equals("*")) {
                resourceScope = ResourceDescriptor.ResourceScope.ALL_ROOT_ID_CHILD_ID_ONLY;
            } else {
                resourceScope = ResourceDescriptor.ResourceScope.ROOT_ID_CHILD_ID_ONLY;
            }
        }
        builder.withChildId(parts[4]);
        builder.withResourceScope(resourceScope);
        return builder.build();
    }

    private static void validateResourceName(String resource, String section) {
        if (!RESOURCE_NAME_PATTERN.matcher(resource).matches()) {
            throw new PolicyStatementException("invalid characters in the {0} : {1}", section, resource);
        }
    }

    private static void validateResourceId(String resource, String section) {
        if (!RESOURCE_ID_PATTERN.matcher(resource).matches()) {
            throw new PolicyStatementException("invalid characters in the {0} : {1}", section, resource);
        }
    }

    private static void checkMinCount(int count, int given, String format, Object... args) {
        if (count > given) {
            throw new PolicyStatementException(format, args);
        }
    }

    private static void checkCount(int count, int given, String format, Object... args) {
        if (count != given) {
            throw new PolicyStatementException(format, args);
        }
    }

    private static void checkWord(String word, String given, String format, Object... args) {
        if (!word.equalsIgnoreCase(given)) {
            throw new PolicyStatementException(format, args);
        }
    }

    public static void main(String[] args) {
        ResourceDescriptor desc = PolicyStatemetParser.parse("tenant/id-1234/v_1/Organization/id_2142423");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("tenant/id_1234/v_1/Organization/*");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("tenant/id_1234/v_1/Organization/**");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("tenant/id_1234/v_1/Organization/*/Tenant/*");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("tenant/id-1234/v_1/Organization/*/Tenant/id_3522");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("tenant/id_1234/v_1/Organization/id_6764374/Tenant/id_3522");
        System.out.println(desc);

        desc = PolicyStatemetParser.parse("tenant/{token.id}/v_1/Organization/id_6764374/Tenant/id_3522");
        System.out.println(desc);

        System.out.println("------------------------------------");

        desc = PolicyStatemetParser.parse("v_1/Organization/id_2142423");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("v_1/Organization/*");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("v_1/Organization/**");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("v_1/Organization/*/Tenant/*");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("v_1/Organization/*/Tenant/id_3522");
        System.out.println(desc);
        desc = PolicyStatemetParser.parse("v_1/Organization/id_6764374/Tenant/id_3522");
        System.out.println(desc);
    }
}
