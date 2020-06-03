/*
 * (c) Copyright 2020 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.lock.watch;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import com.google.errorprone.annotations.concurrent.GuardedBy;

final class FailureCheckingLockWatchEventCache extends AbstractInvocationHandler {

    static LockWatchEventCache newProxyInstance(LockWatchEventCache defaultCache) {
        return (LockWatchEventCache) Proxy.newProxyInstance(
                LockWatchEventCache.class.getClassLoader(),
                new Class<?>[] {LockWatchEventCache.class},
                new FailureCheckingLockWatchEventCache(defaultCache));
    }

    private final LockWatchEventCache defaultCache;
    private final LockWatchEventCache noOpCache;

    @GuardedBy("this")
    private boolean hasFailed = false;

    private FailureCheckingLockWatchEventCache(LockWatchEventCache defaultCache) {
        this.defaultCache = defaultCache;
        this.noOpCache = NoOpLockWatchEventCache.INSTANCE;
    }

    @Override
    protected synchronized Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        if (hasFailed) {
            return method.invoke(noOpCache, args);
        } else {
            hasFailed = true;
            Object result = method.invoke(defaultCache, args);
            hasFailed = false;
            return result;
        }
    }
}
