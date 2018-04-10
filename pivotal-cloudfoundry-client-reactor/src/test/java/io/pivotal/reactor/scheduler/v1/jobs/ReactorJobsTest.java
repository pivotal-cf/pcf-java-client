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

import io.pivotal.reactor.InteractionContext;
import io.pivotal.reactor.TestRequest;
import io.pivotal.reactor.TestResponse;
import io.pivotal.reactor.scheduler.AbstractSchedulerApiTest;
import io.pivotal.scheduler.v1.Link;
import io.pivotal.scheduler.v1.Pagination;
import io.pivotal.scheduler.v1.jobs.CreateJobRequest;
import io.pivotal.scheduler.v1.jobs.CreateJobResponse;
import io.pivotal.scheduler.v1.jobs.DeleteJobRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobResponse;
import io.pivotal.scheduler.v1.jobs.GetJobRequest;
import io.pivotal.scheduler.v1.jobs.GetJobResponse;
import io.pivotal.scheduler.v1.jobs.JobResource;
import io.pivotal.scheduler.v1.jobs.ListJobsRequest;
import io.pivotal.scheduler.v1.jobs.ListJobsResponse;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public final class ReactorJobsTest extends AbstractSchedulerApiTest {

    private final ReactorJobs jobs = new ReactorJobs(CONNECTION_CONTEXT, this.root, TOKEN_PROVIDER);

    @Test
    public void create() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(POST).path("/jobs?app_guid=test-application-id")
                .payload("fixtures/scheduler/v1/jobs/POST_{app_id}_request.json")
                .build())
            .response(TestResponse.builder()
                .status(OK)
                .payload("fixtures/scheduler/v1/jobs/POST_{app_id}_response.json")
                .build())
            .build());

        this.jobs
            .create(CreateJobRequest.builder()
                .applicationId("test-application-id")
                .command("test-command")
                .name("test-name")
                .build())
            .as(StepVerifier::create)
            .expectNext(CreateJobResponse.builder()
                .applicationId("test-application-id")
                .command("test-command")
                .createdAt("test-created-at")
                .id("test-job-id")
                .name("test-name")
                .spaceId("test-space-id")
                .state("test-state")
                .updatedAt("test-updated-at")
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void delete() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(DELETE).path("/jobs/test-job-id")
                .build())
            .response(TestResponse.builder()
                .status(NO_CONTENT)
                .build())
            .build());

        this.jobs
            .delete(DeleteJobRequest.builder()
                .jobId("test-job-id")
                .build())
            .as(StepVerifier::create)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void execute() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(POST).path("/jobs/test-job-id/execute")
                .build())
            .response(TestResponse.builder()
                .status(OK)
                .payload("fixtures/scheduler/v1/jobs/POST_{job_id}_response.json")
                .build())
            .build());

        this.jobs
            .execute(ExecuteJobRequest.builder()
                .jobId("test-job-id")
                .build())
            .as(StepVerifier::create)
            .expectNext(ExecuteJobResponse.builder()
                .executionEndTime("test-execution-end-time")
                .executionStartTime("test-execution-start-time")
                .id("test-history-id")
                .jobId("test-job-id")
                .message("test-message")
                .scheduleId("test-schedule-id")
                .scheduledTime("test-scheduled-time")
                .state("test-state")
                .taskId("test-task-id")
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void get() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(GET).path("/jobs/test-job-id")
                .build())
            .response(TestResponse.builder()
                .status(OK)
                .payload("fixtures/scheduler/v1/jobs/GET_{id}_response.json")
                .build())
            .build());

        this.jobs
            .get(GetJobRequest.builder()
                .jobId("test-job-id")
                .build())
            .as(StepVerifier::create)
            .expectNext(GetJobResponse.builder()
                .applicationId("test-application-id")
                .command("test-command")
                .createdAt("test-created-at")
                .id("test-job-id")
                .name("test-name")
                .spaceId("test-space-id")
                .state("test-state")
                .updatedAt("test-updated-at")
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    public void list() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(GET).path("/jobs?space_guid=test-space-id")
                .build())
            .response(TestResponse.builder()
                .status(OK)
                .payload("fixtures/scheduler/v1/jobs/GET_{space_id}_response.json")
                .build())
            .build());

        this.jobs
            .list(ListJobsRequest.builder()
                .spaceId("test-space-id")
                .build())
            .as(StepVerifier::create)
            .expectNext(ListJobsResponse.builder()
                .pagination(Pagination.builder()
                    .first(Link.builder()
                        .href("test-first-link")
                        .build())
                    .last(Link.builder()
                        .href("test-last-link")
                        .build())
                    .next(Link.builder()
                        .href("test-next-link")
                        .build())
                    .previous(Link.builder()
                        .href("test-previous-link")
                        .build())
                    .totalPages(1)
                    .totalResults(1)
                    .build())
                .resource(JobResource.builder()
                    .applicationId("test-application-id")
                    .command("test-command")
                    .createdAt("test-created-at")
                    .id("test-job-id")
                    .name("test-name")
                    .spaceId("test-space-id")
                    .state("test-state")
                    .updatedAt("test-updated-at")
                    .build())
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

}
