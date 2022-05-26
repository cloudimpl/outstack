package com.cloudimpl.outstack.spring.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TenantAwareReactiveRequest extends ReactiveRequest{
    private String tenantId;
}
