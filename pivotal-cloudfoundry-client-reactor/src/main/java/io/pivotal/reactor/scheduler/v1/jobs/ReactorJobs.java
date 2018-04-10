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

package io.pivotal.reactor.scheduler.v1.jobs;

import io.pivotal.reactor.scheduler.v1.AbstractSchedulerV1Operations;
import io.pivotal.scheduler.v1.jobs.CreateJobRequest;
import io.pivotal.scheduler.v1.jobs.CreateJobResponse;
import io.pivotal.scheduler.v1.jobs.DeleteJobRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobResponse;
import io.pivotal.scheduler.v1.jobs.GetJobRequest;
import io.pivotal.scheduler.v1.jobs.GetJobResponse;
import io.pivotal.scheduler.v1.jobs.Jobs;
import io.pivotal.scheduler.v1.jobs.ListJobHistoriesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobHistoriesResponse;
import io.pivotal.scheduler.v1.jobs.ListJobsRequest;
import io.pivotal.scheduler.v1.jobs.ListJobsResponse;
import io.pivotal.scheduler.v1.jobs.ScheduleJobRequest;
import io.pivotal.scheduler.v1.jobs.ScheduleJobResponse;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import reactor.core.publisher.Mono;

/**
 * The Reactor-based implementation of {@link Jobs}
 */
public class ReactorJobs extends AbstractSchedulerV1Operations implements Jobs {

    /**
     * Creates an instance
     *
     * @param connectionContext the {@link ConnectionContext} to use when communicating with the server
     * @param root              the root URI of the server. Typically something like {@code https://api.run.pivotal.io}.
     * @param tokenProvider     the {@link TokenProvider} to use when communicating with the server
     */
    public ReactorJobs(ConnectionContext connectionContext, Mono<String> root, TokenProvider tokenProvider) {
        super(connectionContext, root, tokenProvider);
    }

    @Override
    public Mono<CreateJobResponse> create(CreateJobRequest request) {
        return post(request, CreateJobResponse.class, builder -> builder.pathSegment("jobs"))
            .checkpoint();
    }

    @Override
    public Mono<Void> delete(DeleteJobRequest request) {
        return delete(request, Void.class, builder -> builder.pathSegment("jobs", request.getJobId()))
            .checkpoint();
    }

    @Override
    public Mono<ExecuteJobResponse> execute(ExecuteJobRequest request) {
        return post(request, ExecuteJobResponse.class, builder -> builder.pathSegment("jobs", request.getJobId(), "execute"))
            .checkpoint();
    }

    @Override
    public Mono<GetJobResponse> get(GetJobRequest request) {
        return get(request, GetJobResponse.class, builder -> builder.pathSegment("jobs", request.getJobId()))
            .checkpoint();
    }

    @Override
    public Mono<ListJobsResponse> list(ListJobsRequest request) {
        return get(request, ListJobsResponse.class, builder -> builder.pathSegment("jobs"))
            .checkpoint();
    }

    @Override
    public Mono<ListJobHistoriesResponse> listHistories(ListJobHistoriesRequest request) {
        return get(request, ListJobHistoriesResponse.class, builder -> builder.pathSegment("jobs", request.getJobId(), "history"))
            .checkpoint();
    }

    @Override
    public Mono<ScheduleJobResponse> schedule(ScheduleJobRequest request) {
        return post(request, ScheduleJobResponse.class, builder -> builder.pathSegment("jobs", request.getJobId(), "schedules"))
            .checkpoint();
    }

}
