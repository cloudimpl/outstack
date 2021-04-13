/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.common;

import reactor.util.retry.Retry;

/**
 *
 * @author nuwan
 */
public class RetryUtil {
    public static Retry wrap(reactor.retry.Retry retry)
    {
        return Retry.withThrowable(retry);
    }
}
