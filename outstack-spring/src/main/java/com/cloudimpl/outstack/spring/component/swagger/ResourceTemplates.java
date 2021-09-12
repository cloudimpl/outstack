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
package com.cloudimpl.outstack.spring.component.swagger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 *
 * @author nuwan
 */
public class ResourceTemplates {

    @Getter
    public static final class Path {

        private transient String path;
        private Map<String, Object> items = new HashMap<>();

        public Path withType(String type, Post post) {
            this.items.put(type, post);
            return this;
        }

        public Path withPath(String path) {
            this.path = path;
            return this;
        }
    }

    @lombok.Builder
    @Getter
    public static final class Info {

        private String description;
        private String version;
        private String title;
        private Contact contact;
        private License license;
    }

    @Builder
    @Getter
    public static final class License {

        private String name;
    }

    @Builder
    @Getter
    public static final class Contact {

        private String name;
        private String url;
        private String email;
    }

    @Builder
    @Getter
    public static final class Tag {

        private String name;
        private String description;
    }

    @Data
    @Builder
    public static final class Get {

        private List<String> tags = new LinkedList<>();
        private String summary;
        private String operationId;
        private List<String> produces;
        private List<Parameter> parameters = new LinkedList<>();

        public Get withParam(Parameter param) {
            this.parameters.add(param);
            return this;
        }
    }

    @Data
    @Builder
    public static final class Post {

        private List<String> tags = new LinkedList<>();
        private String summary;
        private String operationId;
        private List<String> produces;
        private List<Parameter> parameters = new LinkedList<>();

        public Post withParam(Parameter param) {
            this.parameters.add(param);
            return this;
        }
    }

    @Data
    public static final class Response {

        private String description;

    }

    @Data
    public static final class Schema {

        private String type;
    }

    @Getter
    @Builder
    public static final class Parameter {

        private String name;
        private String in;
        private boolean required;
        private String type;
        private String format;
        private Item items;
        private List<String> Enum = new LinkedList<>();
        private String collectionFormat;

        public void setItems(Item items) {
            this.items = items;
        }

        public void addEnum(String e) {
            this.Enum.add(e);
        }
    }

    @Getter
    @Data
    public static final class Item {

        private String type;
        private List<String> Enum = new LinkedList<>();
        private String Default;

        public void addEnum(String e) {
            this.Enum.add(e);
        }
    }

    public static final class In {

        public static final String QUERY = "query";
        public static final String HEADER = "header";
        public static final String PATH = "path";
        private static final String FORMDARA = "formData";
    }
}
