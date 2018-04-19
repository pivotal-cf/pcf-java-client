/*
 * Copyright 2018-2018 the original author or authors.
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

package io.pivotal.reactor.scheduler;

import io.pivotal.reactor.scheduler.v1.calls.ReactorCalls;
import io.pivotal.reactor.scheduler.v1.jobs.ReactorJobs;
import io.pivotal.scheduler.SchedulerClient;
import io.pivotal.scheduler.v1.calls.Calls;
import io.pivotal.scheduler.v1.jobs.Jobs;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.immutables.value.Value;
import reactor.core.publisher.Mono;

/**
 * The Reactor-based implementation of {@link SchedulerClient}
 */
@Value.Immutable
abstract class _ReactorSchedulerClient implements SchedulerClient {

    @Override
    @Value.Derived
    public Calls calls() {
        return new ReactorCalls(getConnectionContext(), getRoot(), getTokenProvider());
    }

    @Override
    @Value.Derived
    public Jobs jobs() {
        return new ReactorJobs(getConnectionContext(), getRoot(), getTokenProvider());
    }

    /**
     * The connection context
     */
    abstract ConnectionContext getConnectionContext();

    @Value.Default
    Mono<String> getRoot() {
        return getConnectionContext().getRootProvider().getRoot("scheduler", getConnectionContext());
    }

    /**
     * The token provider
     */
    abstract TokenProvider getTokenProvider();

}