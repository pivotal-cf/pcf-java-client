/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.reactor.scheduler.v1.calls;

import io.pivotal.reactor.scheduler.v1.AbstractSchedulerV1Operations;
import io.pivotal.scheduler.v1.calls.Calls;
import io.pivotal.scheduler.v1.calls.CreateCallRequest;
import io.pivotal.scheduler.v1.calls.CreateCallResponse;
import io.pivotal.scheduler.v1.calls.DeleteCallRequest;
import io.pivotal.scheduler.v1.calls.DeleteCallScheduleRequest;
import io.pivotal.scheduler.v1.calls.ExecuteCallRequest;
import io.pivotal.scheduler.v1.calls.ExecuteCallResponse;
import io.pivotal.scheduler.v1.calls.GetCallRequest;
import io.pivotal.scheduler.v1.calls.GetCallResponse;
import io.pivotal.scheduler.v1.calls.ListCallHistoriesRequest;
import io.pivotal.scheduler.v1.calls.ListCallHistoriesResponse;
import io.pivotal.scheduler.v1.calls.ListCallScheduleHistoriesRequest;
import io.pivotal.scheduler.v1.calls.ListCallScheduleHistoriesResponse;
import io.pivotal.scheduler.v1.calls.ListCallSchedulesRequest;
import io.pivotal.scheduler.v1.calls.ListCallSchedulesResponse;
import io.pivotal.scheduler.v1.calls.ListCallsRequest;
import io.pivotal.scheduler.v1.calls.ListCallsResponse;
import io.pivotal.scheduler.v1.calls.ScheduleCallRequest;
import io.pivotal.scheduler.v1.calls.ScheduleCallResponse;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import reactor.core.publisher.Mono;

/**
 * The Reactor-based implementation of {@link Calls}
 */
public class ReactorCalls extends AbstractSchedulerV1Operations implements Calls {

    /**
     * Creates an instance
     *
     * @param connectionContext the {@link ConnectionContext} to use when communicating with the server
     * @param root              the root URI of the server. Typically something like {@code https://api.run.pivotal.io}.
     * @param tokenProvider     the {@link TokenProvider} to use when communicating with the server
     */
    public ReactorCalls(ConnectionContext connectionContext, Mono<String> root, TokenProvider tokenProvider) {
        super(connectionContext, root, tokenProvider);
    }

    @Override
    public Mono<CreateCallResponse> create(CreateCallRequest request) {
        return post(request, CreateCallResponse.class, builder -> builder.pathSegment("calls"))
            .checkpoint();
    }

    @Override
    public Mono<Void> delete(DeleteCallRequest request) {
        return delete(request, Void.class, builder -> builder.pathSegment("calls", request.getCallId()))
            .checkpoint();
    }

    @Override
    public Mono<Void> deleteSchedule(DeleteCallScheduleRequest request) {
        return delete(request, Void.class, builder -> builder.pathSegment("calls", request.getCallId(), "schedules", request.getScheduleId()))
            .checkpoint();
    }

    @Override
    public Mono<ExecuteCallResponse> execute(ExecuteCallRequest request) {
        return post(request, ExecuteCallResponse.class, builder -> builder.pathSegment("calls", request.getCallId(), "execute"))
            .checkpoint();
    }

    @Override
    public Mono<GetCallResponse> get(GetCallRequest request) {
        return get(request, GetCallResponse.class, builder -> builder.pathSegment("calls", request.getCallId()))
            .checkpoint();
    }

    @Override
    public Mono<ListCallsResponse> list(ListCallsRequest request) {
        return get(request, ListCallsResponse.class, builder -> builder.pathSegment("calls"))
            .checkpoint();
    }

    @Override
    public Mono<ListCallHistoriesResponse> listHistories(ListCallHistoriesRequest request) {
        return get(request, ListCallHistoriesResponse.class, builder -> builder.pathSegment("calls", request.getCallId(), "history"))
            .checkpoint();
    }

    @Override
    public Mono<ListCallScheduleHistoriesResponse> listScheduleHistories(ListCallScheduleHistoriesRequest request) {
        return get(request, ListCallScheduleHistoriesResponse.class, builder -> builder.pathSegment("calls", request.getCallId(), "schedules", request.getScheduleId(), "history"))
            .checkpoint();
    }

    @Override
    public Mono<ListCallSchedulesResponse> listSchedules(ListCallSchedulesRequest request) {
        return get(request, ListCallSchedulesResponse.class, builder -> builder.pathSegment("calls", request.getCallId(), "schedules"))
            .checkpoint();
    }

    @Override
    public Mono<ScheduleCallResponse> schedule(ScheduleCallRequest request) {
        return post(request, ScheduleCallResponse.class, builder -> builder.pathSegment("calls", request.getCallId(), "schedules"))
            .checkpoint();
    }

}
