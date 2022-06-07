package com.cloudimpl.outstack.spring.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TenantAwareReactiveRequest extends ReactiveRequest{
    private String tenantId;
}
