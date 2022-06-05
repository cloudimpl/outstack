package com.cloudimpl.outstack.spring.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TenantAwareReactiveRequest extends ReactiveRequest{
    private String tenantId;
}
