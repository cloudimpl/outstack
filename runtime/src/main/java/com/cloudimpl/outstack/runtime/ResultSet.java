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
package com.cloudimpl.outstack.runtime;

import java.util.Collection;
import com.google.gson.internal.LinkedTreeMap;
import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import java.util.stream.Collectors;
/**
 *
 * @author nuwan
 */
public class ResultSet<T> {
    private final long totalItems;
    private final int totalPages;
    private final int currentPage;
    private final Collection<T> items;

    public ResultSet(long totalItems, int totalPages, int currentPage, Collection<T> items) {
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.items = items;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Deprecated
    public Collection<T> getItems() {
        return items;
    }
    
    public  Collection<T> getItems(Class<T> cls) {
        return getItems().stream().map(i -> {
            if (i instanceof LinkedTreeMap) {
                return GsonCodecRuntime.decodeTree(cls, (LinkedTreeMap) i);
            }
            return i;
        }).collect(Collectors.toList());
    }
    
}
