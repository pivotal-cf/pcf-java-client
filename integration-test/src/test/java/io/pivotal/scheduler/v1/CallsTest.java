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

package io.pivotal.scheduler.v1;

import io.pivotal.AbstractIntegrationTest;
import io.pivotal.reactor.scheduler.ReactorSchedulerClient;
import io.pivotal.scheduler.v1.calls.Call;
import io.pivotal.scheduler.v1.calls.CallHistory;
import io.pivotal.scheduler.v1.calls.CallSchedule;
import io.pivotal.scheduler.v1.calls.CreateCallRequest;
import io.pivotal.scheduler.v1.calls.CreateCallResponse;
import io.pivotal.scheduler.v1.calls.DeleteCallRequest;
import io.pivotal.scheduler.v1.calls.DeleteCallScheduleRequest;
import io.pivotal.scheduler.v1.calls.ExecuteCallRequest;
import io.pivotal.scheduler.v1.calls.ExecuteCallResponse;
import io.pivotal.scheduler.v1.calls.GetCallRequest;
import io.pivotal.scheduler.v1.calls.GetCallResponse;
import io.pivotal.scheduler.v1.calls.ListCallHistoriesRequest;
import io.pivotal.scheduler.v1.calls.ListCallScheduleHistoriesRequest;
import io.pivotal.scheduler.v1.calls.ListCallSchedulesRequest;
import io.pivotal.scheduler.v1.calls.ListCallsRequest;
import io.pivotal.scheduler.v1.calls.ScheduleCallRequest;
import io.pivotal.scheduler.v1.calls.ScheduleCallResponse;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.CreateApplicationRequest;
import org.cloudfoundry.client.v2.applications.CreateApplicationResponse;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingResponse;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Duration;

import static io.pivotal.scheduler.v1.schedules.ExpressionType.CRON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.cloudfoundry.util.tuple.TupleUtils.function;

public final class CallsTest extends AbstractIntegrationTest {

    private static final String CRON_EXPRESSION_FAST = "* * ? * * *";

    private static final String CRON_EXPRESSION_SLOW = "* * * * ? 2099";

    @Autowired
    String schedulerServiceInstanceName;

    @Autowired
    Mono<String> spaceId;

    @Autowired
    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    private ReactorSchedulerClient schedulerClient;

    @Test
    public void create() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> this.schedulerClient.calls()
                .create(CreateCallRequest.builder()
                    .applicationId(applicationId)
                    .authorizationHeader("test-header")
                    .name(callName)
                    .url("test.url")
                    .build())
                .map(CreateCallResponse::getId))
            .flatMap(callId -> getCallName(this.schedulerClient, callId))
            .as(StepVerifier::create)
            .expectNext(callName)
            .verifyComplete();
    }

    @Test
    public void delete() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .delayUntil(callId -> this.schedulerClient.calls()
                .delete(DeleteCallRequest.builder()
                    .callId(callId)
                    .build()))
            .flatMap(callId -> requestGetCall(this.schedulerClient, callId))
            .as(StepVerifier::create)
            .consumeErrorWith(t -> assertThat(t).isInstanceOf(SchedulerException.class).hasMessageMatching("Not Found"))
            .verify(Duration.ofMinutes(5));
    }

    @Test
    public void deleteSchedule() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .flatMap(callId -> Mono.zip(
                Mono.just(callId),
                createScheduleId(this.schedulerClient, callId, CRON_EXPRESSION_SLOW)
            ))
            .flatMap(function((callId, scheduleId) -> this.schedulerClient.calls()
                .deleteSchedule(DeleteCallScheduleRequest.builder()
                    .callId(callId)
                    .scheduleId(scheduleId)
                    .build())
                .thenReturn(callId)))
            .flatMapMany(callId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.calls()
                    .listSchedules(ListCallSchedulesRequest.builder()
                        .callId(callId)
                        .page(page)
                        .build())))
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void execute() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .flatMap(callId -> this.schedulerClient.calls()
                .execute(ExecuteCallRequest.builder()
                    .callId(callId)
                    .build()))
            .map(ExecuteCallResponse::getState)
            .as(StepVerifier::create)
            .expectNext("PENDING")
            .verifyComplete();
    }

    @Test
    public void get() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .flatMap(callId -> requestGetCall(this.schedulerClient, callId))
            .map(GetCallResponse::getName)
            .as(StepVerifier::create)
            .expectNext(callName)
            .verifyComplete();
    }

    @Test
    public void list() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName),
                Mono.just(spaceId)
            ))
            .flatMap(function((applicationId, schedulerId, spaceId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(Tuples.of(applicationId, spaceId))))
            .flatMap(function((applicationId, spaceId) -> Mono.zip(
                createCallId(this.schedulerClient, applicationId, callName),
                Mono.just(spaceId)
            )))
            .flatMapMany(function((callId, spaceId) -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.calls()
                    .list(ListCallsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .filter(resource -> callName.endsWith(resource.getName()))
            .map(Call::getUrl)
            .as(StepVerifier::create)
            .expectNext("test.url")
            .verifyComplete();
    }

    @Test
    public void listHistories() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .delayUntil(callId -> this.schedulerClient.calls()
                .execute(ExecuteCallRequest.builder()
                    .callId(callId)
                    .build()))
            .flatMapMany(callId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.calls()
                    .listHistories(ListCallHistoriesRequest.builder()
                        .callId(callId)
                        .page(page)
                        .build())))
            .map(CallHistory::getState)
            .as(StepVerifier::create)
            .expectNext("PENDING")
            .verifyComplete();
    }

    @Test
    public void listScheduleHistories() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .flatMap(callId -> Mono.zip(
                Mono.just(callId),
                createScheduleId(this.schedulerClient, callId, CRON_EXPRESSION_FAST)
            ))
            .delayElement(Duration.ofSeconds(90))
            .flatMapMany(function((callId, scheduleId) -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.calls()
                    .listScheduleHistories(ListCallScheduleHistoriesRequest.builder()
                        .callId(callId)
                        .page(page)
                        .scheduleId(scheduleId)
                        .build()))))
            .next()
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void listSchedules() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .delayUntil(callId -> requestScheduleCall(this.schedulerClient, callId, CRON_EXPRESSION_SLOW))
            .flatMapMany(callId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.calls()
                    .listSchedules(ListCallSchedulesRequest.builder()
                        .callId(callId)
                        .page(page)
                        .build())))
            .map(CallSchedule::getExpression)
            .as(StepVerifier::create)
            .expectNext(CRON_EXPRESSION_SLOW)
            .verifyComplete();
    }

    @Test
    public void schedule() {
        String applicationName = this.nameFactory.getApplicationName();
        String callName = this.nameFactory.getCallName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createCallId(this.schedulerClient, applicationId, callName))
            .delayUntil(callId -> this.schedulerClient.calls()
                .schedule(ScheduleCallRequest.builder()
                    .enabled(true)
                    .expression(CRON_EXPRESSION_SLOW)
                    .expressionType(CRON)
                    .callId(callId)
                    .build()))
            .flatMapMany(callId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.calls()
                    .listSchedules(ListCallSchedulesRequest.builder()
                        .callId(callId)
                        .page(page)
                        .build())))
            .map(CallSchedule::getExpression)
            .as(StepVerifier::create)
            .expectNext(CRON_EXPRESSION_SLOW)
            .verifyComplete();
    }

    private static Mono<String> createApplicationId(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return requestCreateApplication(cloudFoundryClient, applicationName, spaceId)
            .map(ResourceUtils::getId);
    }

    private static Mono<String> createCallId(ReactorSchedulerClient schedulerClient, String applicationId, String callName) {
        return requestCreateCall(schedulerClient, applicationId, callName)
            .map(CreateCallResponse::getId);
    }

    private static Mono<String> createScheduleId(ReactorSchedulerClient schedulerClient, String callId, String cronExpression) {
        return requestScheduleCall(schedulerClient, callId, cronExpression)
            .map(ScheduleCallResponse::getId);
    }

    private static Mono<String> getCallName(ReactorSchedulerClient schedulerClient, String callId) {
        return requestGetCall(schedulerClient, callId)
            .map(GetCallResponse::getName);
    }

    private static Mono<String> getSchedulerServiceId(CloudFoundryClient cloudFoundryClient, String schedulerServiceInstanceName) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.serviceInstances()
                .list(ListServiceInstancesRequest.builder()
                    .name(schedulerServiceInstanceName)
                    .page(page)
                    .build()))
            .single()
            .map(ResourceUtils::getId);
    }

    private static Mono<CreateApplicationResponse> requestCreateApplication(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return cloudFoundryClient.applicationsV2()
            .create(CreateApplicationRequest.builder()
                .name(applicationName)
                .spaceId(spaceId)
                .build());
    }

    private static Mono<CreateCallResponse> requestCreateCall(ReactorSchedulerClient schedulerClient, String applicationId, String callName) {
        return schedulerClient.calls()
            .create(CreateCallRequest.builder()
                .applicationId(applicationId)
                .authorizationHeader("test-header")
                .name(callName)
                .url("test.url")
                .build());
    }

    private static Mono<CreateServiceBindingResponse> requestCreateServiceBinding(CloudFoundryClient cloudFoundryClient, String applicationId, String schedulerId) {
        return cloudFoundryClient.serviceBindingsV2()
            .create(CreateServiceBindingRequest.builder()
                .applicationId(applicationId)
                .serviceInstanceId(schedulerId)
                .build());
    }

    private static Mono<GetCallResponse> requestGetCall(ReactorSchedulerClient schedulerClient, String callId) {
        return schedulerClient.calls()
            .get(GetCallRequest.builder()
                .callId(callId)
                .build());
    }

    private static Mono<ScheduleCallResponse> requestScheduleCall(ReactorSchedulerClient schedulerClient, String callId, String cronExpression) {
        return schedulerClient.calls()
            .schedule(ScheduleCallRequest.builder()
                .enabled(true)
                .expression(cronExpression)
                .expressionType(CRON)
                .callId(callId)
                .build());
    }

}
