/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.Map;

/**
 *
 * @author nuwansa
 */
public interface Input{
     String tenantId();
    // trn:restrata:sso:tenant/00fdd02a-c082-421b-b3b8-357c9cbdf4dc/User/ee0495fc-3be5-40a8-a7e5-0a9479835b06/Trip/7662d4bd-ac9e-493e-9794-50d469f4ae28
    // brn:restrata:sso/User/1
    // rrn:restrata:sso/User/1/UserCreated/1
    //rrn:restrata:sso/User/1/Trip/3/TripCreated/2
    
    //brn:genal:travel:tenant/1234/User/1
    //brn:genal:travel/User/1/Trip/3
    //rrn:genal:travel:tenant/1234/User/2
    
    //action : list*
   //brn:genal:travel/User/1/*
    //brn:genal:travel:tenant/1234/*
    //brn:genal:travel:tenant/*
    
//    [
//            action: [createUser,updateUser,listUser]
//            rn:
//            [
//            brn:genal:travel/User/1/*,
//            brn:genal:travel:tenant/1234/Trip/*
//            */
//            ]
//            ]
    //brn:restrata:sso/*
    //brn:genal:travel:tenant/*
            
 
}
