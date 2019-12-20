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

package io.pivotal.scheduler.v1.calls;

import reactor.core.publisher.Mono;

/**
 * Main entry point to the Calls API
 */
public interface Calls {

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#create-call">Create Call</a> request
     *
     * @param request the Create Call request
     * @return the response to the Create Call request
     */
    Mono<CreateCallResponse> create(CreateCallRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#delete-a-call">Delete a Call</a> request
     *
     * @param request the Delete a Call request
     * @return the response to the Delete a Call request
     */
    Mono<Void> delete(DeleteCallRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#deletes-the-given-schedule-for-the-given-job">Delete a Call Schedule</a> request
     *
     * @param request the Delete a Call Schedule request
     * @return the response to the Delete a Call Schedule request
     */
    Mono<Void> deleteSchedule(DeleteCallScheduleRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#execute-a-call-as-soon-as-possible">Execute a Call</a> request
     *
     * @param request the Execute a Call request
     * @return the response to the Execute a Call request
     */
    Mono<ExecuteCallResponse> execute(ExecuteCallRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#get-a-call">Get a Call</a> request
     *
     * @param request the Get a Call request
     * @return the response to the Get a Call request
     */
    Mono<GetCallResponse> get(GetCallRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#get-all-calls-within-space">List Calls</a> request
     *
     * @param request the List Calls request
     * @return the response to the List Calls request
     */
    Mono<ListCallsResponse> list(ListCallsRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#gets-all-execution-histories-for-a-call">List Call Histories</a> request
     *
     * @param request the List Call Histories request
     * @return the response to the List Call Histories request
     */
    Mono<ListCallHistoriesResponse> listHistories(ListCallHistoriesRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#gets-all-execution-histories-for-a-call-and-schedule">List Call Schedule Histories</a> request
     *
     * @param request the List Call Schedule Histories request
     * @return the response to the List Call Schedule Histories request
     */
    Mono<ListCallScheduleHistoriesResponse> listScheduleHistories(ListCallScheduleHistoriesRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#gets-all-execution-histories-for-a-call">List Call Schedules</a> request
     *
     * @param request the List Call Schedules request
     * @return the response to the List Call Schedules request
     */
    Mono<ListCallSchedulesResponse> listSchedules(ListCallSchedulesRequest request);

    /**
     * Makes the <a href="https://docs.pivotal.io/pcf-scheduler/1-1/api/#schedules-a-call-to-run-later">Schedule a Call</a> request
     *
     * @param request the Schedule a Call request
     * @return the response to the Schedule a Call request
     */
    Mono<ScheduleCallResponse> schedule(ScheduleCallRequest request);

}
