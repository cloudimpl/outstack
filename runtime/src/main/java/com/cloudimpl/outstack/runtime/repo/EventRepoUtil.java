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
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.ResultSet;
import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class EventRepoUtil {
    public static <T> boolean onFilter(T item, Map<String, String> params) {
        if (params.isEmpty()) {
            return true;
        }
        JsonObject json = GsonCodecRuntime.encodeToJson(item).getAsJsonObject();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            JsonElement el = json.get(entry.getKey());
            if (el == null || !el.isJsonPrimitive()) {
                System.out.println("el " + entry.getKey() + " not found or not an primitive data type");
                return false;
            } else {
                JsonPrimitive jsonPrim = (JsonPrimitive) el;
                if (jsonPrim.isNumber()) {
                    BigDecimal target = new BigDecimal(entry.getValue());
                    if (jsonPrim.getAsBigDecimal().compareTo(target) != 0) {
                        return false;
                    }
                } else if (jsonPrim.isString()) {
                    if (entry.getValue().startsWith("%") && entry.getValue().length() > 1) {
                        if (!jsonPrim.getAsString().toLowerCase().endsWith(entry.getValue().substring(1).toLowerCase())) {
                            return false;
                        }
                    } else if (entry.getValue().endsWith("%") && entry.getValue().length() > 1) {
                        if (!jsonPrim.getAsString().toLowerCase().startsWith(entry.getValue().substring(0, entry.getValue().length() - 1).toLowerCase())) {
                            return false;
                        }
                    } else if (entry.getValue().contains("*")) {
                        String queryStr = entry.getValue();
                        queryStr = queryStr.replaceAll("\\*", "\\\\w*");
                        if (!jsonPrim.getAsString().matches(queryStr)) {
                            return false;
                        }
                    } else if (!jsonPrim.getAsString().equalsIgnoreCase(entry.getValue())) {
                        return false;
                    }
                } else if (jsonPrim.isBoolean()) {
                    if (jsonPrim.getAsBoolean() != Boolean.valueOf(entry.getValue())) {
                        return false;
                    }
                } else {
                    System.out.println("unhandle primitive data type:" + jsonPrim);
                    return false;
                }
            }
        }
        return true;
    }
    
     public static int compare(String name, Object left, Object right) {
        JsonObject leftJson = GsonCodecRuntime.encodeToJson(left).getAsJsonObject();
        JsonObject rightJson = GsonCodecRuntime.encodeToJson(right).getAsJsonObject();
        JsonElement leftEl = leftJson.get(name);
        JsonElement rightEl = rightJson.get(name);
        if (leftEl == null && rightEl == null) {
            //throw new RepositoryException("null not supported for sorting: " + name + " field");
            return 0;
        } else if (leftEl == null && rightEl != null) {
            return 1;
        } else if (leftEl != null && rightEl == null) {
            return -1;
        }
        if (!leftEl.isJsonPrimitive() || !rightEl.isJsonPrimitive()) {
            throw new RepositoryException("only primitive types supported for sorting");
        }
        JsonPrimitive leftPrim = leftEl.getAsJsonPrimitive();
        JsonPrimitive rightPrim = rightEl.getAsJsonPrimitive();
        if (leftPrim.isNumber() && rightPrim.isNumber()) {
            return leftPrim.getAsBigDecimal().compareTo(rightPrim.getAsBigDecimal());
        } else if (leftPrim.isString() && rightPrim.isString()) {
            return leftPrim.getAsString().compareToIgnoreCase(rightPrim.getAsString());
        } else if (leftPrim.isBoolean() && rightPrim.isBoolean()) {
            return Boolean.compare(leftPrim.getAsBoolean(), rightPrim.getAsBoolean());
        }
        throw new RepositoryException("unsupported data type for sorting . {0} : {1} ", leftJson, rightJson);
    }
     
    public static  <T> ResultSet<T> onPageable(Collection<T> result, Query.PagingRequest paging) {
        if (paging == null) {
            return new ResultSet<>(result.size(), 1, 0, result);
        }

        Comparator<T> comparator = null;
        for (Query.Order order : paging.orders()) {
            Comparator<T> comp = (left, right) -> compare(order.getName(), left, right);
            if (order.getDirection() == Query.Direction.DESC) {
                comp = comp.reversed();
            }
            comparator = (comparator == null) ? comp : comparator.thenComparing(comp);
        }
        int offset = paging.pageNum() * paging.pageSize();
        int min = Math.min(result.size() - offset, paging.pageSize());
        Collection<T> out;
        if (comparator != null) {
            out = result.stream().sorted(comparator).skip(offset).limit(min).collect(Collectors.toList());
        } else {
            out = result.stream().skip(offset).limit(min < 0 ? 0 : min).collect(Collectors.toList());
        }
        return new ResultSet<>(result.size(), (int) Math.ceil(((double) result.size()) / paging.pageSize()), paging.pageNum(), out);
    }

}
