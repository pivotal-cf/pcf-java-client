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

package io.pivotal.reactor.scheduler.v1.calls;

import io.pivotal.reactor.InteractionContext;
import io.pivotal.reactor.TestRequest;
import io.pivotal.reactor.TestResponse;
import io.pivotal.reactor.scheduler.AbstractSchedulerApiTest;
import io.pivotal.scheduler.v1.calls.CreateCallRequest;
import io.pivotal.scheduler.v1.calls.CreateCallResponse;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;

public final class ReactorCallsTest extends AbstractSchedulerApiTest {

    private final ReactorCalls calls = new ReactorCalls(CONNECTION_CONTEXT, this.root, TOKEN_PROVIDER);

    @Test
    public void create() {
        mockRequest(InteractionContext.builder()
            .request(TestRequest.builder()
                .method(POST).path("/calls?app_guid=test-application-id")
                .payload("fixtures/scheduler/v1/calls/POST_{app_id}_request.json")
                .build())
            .response(TestResponse.builder()
                .status(CREATED)
                .payload("fixtures/scheduler/v1/calls/POST_{app_id}_response.json")
                .build())
            .build());

        this.calls
            .create(CreateCallRequest.builder()
                .applicationId("test-application-id")
                .authorizationHeader("test-authorization-header")
                .name("test-name")
                .url("test-url")
                .build())
            .as(StepVerifier::create)
            .expectNext(CreateCallResponse.builder()
                .applicationId("test-application-id")
                .authorizationHeader("test-authorization-header")
                .createdAt("test-created-at")
                .id("test-job-id")
                .name("test-name")
                .spaceId("test-space-id")
                .updatedAt("test-updated-at")
                .url("test-url")
                .build())
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

}
