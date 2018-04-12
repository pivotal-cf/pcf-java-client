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

package io.pivotal.scheduler.v1;

import io.pivotal.AbstractIntegrationTest;
import io.pivotal.reactor.scheduler.ReactorSchedulerClient;
import io.pivotal.scheduler.v1.jobs.ListJobsRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static io.pivotal.reactor.util.PaginationUtils.requestResources;

public final class JobsTest extends AbstractIntegrationTest {

    @Autowired
    Mono<String> spaceId;

    @Autowired
    private ReactorSchedulerClient schedulerClient;

    @Test
    public void demonstration() {
        this.spaceId
            .flatMapMany(spaceId ->
                requestResources(page -> this.schedulerClient.jobs()
                    .list(ListJobsRequest.builder()
                        .spaceId(spaceId)
                        .page(page)
                        .build())))
            .as(StepVerifier::create)
            .verifyComplete();
    }
}
