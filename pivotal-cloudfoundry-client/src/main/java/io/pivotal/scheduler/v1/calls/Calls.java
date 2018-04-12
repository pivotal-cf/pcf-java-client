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

package io.pivotal.scheduler.v1.calls;

import reactor.core.publisher.Mono;

/**
 * Main entry point to the Calls API
 */
public interface Calls {

    /**
     * Makes the <a href="http://docs.pivotal.io/pcf-scheduler/1-1/api/#create-call">Create Call</a> request
     *
     * @param request the Create Call request
     * @return the response to the Create Call request
     */
    Mono<CreateCallResponse> create(CreateCallRequest request);

    /**
     * Makes the <a href="http://docs.pivotal.io/pcf-scheduler/1-1/api/#delete-a-call">Delete a Call</a> request
     *
     * @param request the Delete a Call request
     * @return the response to the Delete a Call request
     */
    Mono<Void> delete(DeleteCallRequest request);

    /**
     * Makes the <a href="http://docs.pivotal.io/pcf-scheduler/1-1/api/#get-all-calls-within-space">List Calls</a> request
     *
     * @param request the List Calls request
     * @return the response to the List Calls request
     */
    Mono<ListCallsResponse> list(ListCallsRequest request);

}
