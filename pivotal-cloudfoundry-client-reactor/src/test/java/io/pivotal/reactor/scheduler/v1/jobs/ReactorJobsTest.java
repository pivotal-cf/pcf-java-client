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

import io.pivotal.reactor.scheduler.AbstractSchedulerApiTest;
import io.pivotal.scheduler.v1.jobs.CreateJobRequest;
import io.pivotal.scheduler.v1.jobs.CreateJobResponse;
import io.pivotal.reactor.InteractionContext;
import io.pivotal.reactor.TestRequest;
import io.pivotal.reactor.TestResponse;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public final class ReactorJobsTest extends AbstractSchedulerApiTest {

    private final ReactorJobs jobs = new ReactorJobs(CONNECTION_CONTEXT, this.root, TOKEN_PROVIDER);

    @Test
    public void create() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(POST).path("/jobs?app_guid=test-application-id")
                .payload("fixtures/scheduler/v1/jobs/POST_{id}_request.json")
                .build())
            .response(TestResponse.builder()
                .status(OK)
                .payload("fixtures/scheduler/v1/jobs/POST_{id}_response.json")
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
                .jobId("test-job-id")
                .name("test-name")
                .spaceId("test-space-id")
                .state("test-state")
                .updatedAt("test-updated-at")
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

}
