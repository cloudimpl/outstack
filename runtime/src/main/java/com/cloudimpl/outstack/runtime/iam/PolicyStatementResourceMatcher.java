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
public class PolicyStatementResourceMatcher {
//    public static boolean matchRootResource(String tenantId,String targetResource,String givenResource)
//    {
//        return 
//        //trn:restrata:
//    }
    
}
//Gateway validation
//{
//scopes: [read]
//}
//policy actions
//        
//{
//    sid : 'aaa',
//    effect: DENY,
//    action: [
//        Organization:List*,
//        Organization:Get*
//        Orgnization:GetTeant
//
//    ],
//    resource:[
//        trn:restrata:platform:Organization/**, //all the resources under the Organization including Organization itself
//        trn:restrata:platform:Organization/*
//        trn:restrata:platform:Organization/id-1234/Tenant/*
//    ]
//}
///platfrom/v1/Organization/1234
//{
//    sid : 'aaa',
//    effect: ALLOW,
//    action: [
//        *
//
//    ],
//    resource:[
//        trn:restrata:platform:tenant/1234, //all the resources under the Organization including Organization itself
//    ]
//}
//
//{
//    sid : 'aaa',
//    effect: ALLOW,
//    action: [
//        Organization:CreateOrganization,
//        Organization:ListOrganization
//
//    ],
//    resource:[
//        trn:restrata:platform:Organization/**,
//
//    ]
//}
//
//{
//    sid : 'aaa',
//    effect: ALLOW,
//    action: [
//        Organization:CreateOrganization,
//        Organization:ListOrganization
//        Organization:CreateTenant
//        Organization:UpdateTenant
//    ],
//    resource:[
//        trn:restrata:platform:Organization/*,
//        trn:restrata:platform:Organization/*/Tenant/*
//    ]
//}
//
//
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        Trip:CreateTrip
//    ],
//    resource:[
//        trn:restrata:travel:tenant/{token.tid}/Trip/**
//    ]
//}
//
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        User:*
//    ],
//    resource:[
//        trn:restrata:travel:User/{token.principle}/**
//    ]
//}
//
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        User:*
//    ],
//Principle:[
//trn:restrata:platfrom:User/1234,
//trn:restrata:platfrom:User/12356
//    ]
//    resource:[
//        trn:restrata:travel:User/{token.principle}/**
//    ]
//}
//
//
//
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        Trip:ApproveTrip
//    ],
//    resource:[
//        trn:restrata:travel:tenant/{token:tid}/Trip/**
//    ]
//}
//
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        Trip:ApproveTrip
//    ],
//    Principle: trn:restrata:User:1234,
//    resource:[
//        trn:restrata:travel:tenant/{token:tid}/Trip/**
//    ]
//}
//
//{
//sid: "Policy1",
//statments:[
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        Trip:ApproveTrip
//    ],
//    Principle: trn:restrata:User:1234,
//    resource:[
//        trn:restrata:travel:tenant/{token:tid}/Trip/**
//    ]
//},
//{
//    sid : 'bbbb',
//    effect: ALLOW,
//    action: [
//        Trip:ApproveTrip
//    ],
//    Principle: trn:restrata:User:1234,
//    resource:[
//        trn:restrata:travel:tenant/{token:tid}/Trip/**
//    ]
//}
//]
//}