///*
// * Copyright 2021 nuwan.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.cloudimpl.outstack.spring.component;
//
//
//import com.google.common.base.Predicate;
//import com.google.common.base.Predicates;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;
//
///**
// *
// * @author nuwan
//// */
//@Configuration
//@EnableSwagger2WebFlux
// public class SwaggerConfig {
//
//     @Bean
//     public Docket postsApi() {
//         return new Docket(DocumentationType.SWAGGER_2)
//                 .groupName("public-api")
//                 .apiInfo(apiInfo())
//                 .select()
//            //     .paths((s)->postPaths().apply(s))
//                 .build();
//     }
//
////     private com.google.common.base.Predicate<String> postPaths() {
////         return com.google.common.base.Predicates.or(
////                 PathSelectors.regex("/api/posts.*"),
////                 PathSelectors.regex("/api/comments.*")
////         );
////     }
//
//     private ApiInfo apiInfo() {
//         return new ApiInfoBuilder()
//                 .title("SpringMVC Example API")
//                 .description("SpringMVC Example API reference for developers")
//                 .termsOfServiceUrl("http://hantsy.blogspot.com")
//                 .contact(new Contact("nuwan", "http://locahost", "nuwan@xzz.com"))
//                 .license("Apache License Version 2.0")
//                 .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
//                 .version("2.0")
//                 .build();
//     }
//
// }