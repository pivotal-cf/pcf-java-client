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
import io.pivotal.scheduler.v1.jobs.CreateJobRequest;
import io.pivotal.scheduler.v1.jobs.CreateJobResponse;
import io.pivotal.scheduler.v1.jobs.DeleteJobRequest;
import io.pivotal.scheduler.v1.jobs.DeleteJobScheduleRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobResponse;
import io.pivotal.scheduler.v1.jobs.GetJobRequest;
import io.pivotal.scheduler.v1.jobs.GetJobResponse;
import io.pivotal.scheduler.v1.jobs.Job;
import io.pivotal.scheduler.v1.jobs.JobHistory;
import io.pivotal.scheduler.v1.jobs.JobSchedule;
import io.pivotal.scheduler.v1.jobs.ListJobHistoriesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobScheduleHistoriesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobSchedulesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobsRequest;
import io.pivotal.scheduler.v1.jobs.ScheduleJobRequest;
import io.pivotal.scheduler.v1.jobs.ScheduleJobResponse;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Optional;

import static io.pivotal.scheduler.v1.schedules.ExpressionType.CRON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.cloudfoundry.util.tuple.TupleUtils.function;

public final class JobsTest extends AbstractIntegrationTest {

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
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> this.schedulerClient.jobs()
                .create(CreateJobRequest.builder()
                    .applicationId(applicationId)
                    .command("ls")
                    .name(jobName)
                    .build())
                .map(CreateJobResponse::getId))
            .flatMap(jobId -> getJobName(this.schedulerClient, jobId))
            .as(StepVerifier::create)
            .expectNext(jobName)
            .verifyComplete();
    }

    @Test
    public void delete() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .delayUntil(jobId -> this.schedulerClient.jobs()
                .delete(DeleteJobRequest.builder()
                    .jobId(jobId)
                    .build()))
            .flatMap(jobId -> requestGetJob(this.schedulerClient, jobId))
            .as(StepVerifier::create)
            .consumeErrorWith(t -> assertThat(t).isInstanceOf(SchedulerException.class).hasMessageMatching("Not Found"))
            .verify(Duration.ofMinutes(5));
    }

    @Test
    public void deleteSchedule() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .flatMap(jobId -> Mono.zip(
                Mono.just(jobId),
                createScheduleId(this.schedulerClient, CRON_EXPRESSION_SLOW, jobId)
            ))
            .flatMap(function((jobId, scheduleId) -> this.schedulerClient.jobs()
                .deleteSchedule(DeleteJobScheduleRequest.builder()
                    .jobId(jobId)
                    .scheduleId(scheduleId)
                    .build())
                .thenReturn(jobId)))
            .flatMapMany(jobId -> requestListJobSchedules(this.schedulerClient, jobId))
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void execute() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .flatMap(jobId -> this.schedulerClient.jobs()
                .execute(ExecuteJobRequest.builder()
                    .jobId(jobId)
                    .build()))
            .map(ExecuteJobResponse::getState)
            .as(StepVerifier::create)
            .expectNext("PENDING")
            .verifyComplete();
    }

    @Test
    public void get() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .flatMap(jobId -> this.schedulerClient.jobs()
                .get(GetJobRequest.builder()
                    .jobId(jobId)
                    .build()))
            .map(GetJobResponse::getName)
            .as(StepVerifier::create)
            .expectNext(jobName)
            .verifyComplete();
    }

    @Test
    public void list() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName),
                Mono.just(spaceId)
            ))
            .flatMap(function((applicationId, schedulerId, spaceId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(Tuples.of(applicationId, spaceId))))
            .flatMap(function((applicationId, spaceId) -> Mono.zip(
                createJobId(this.schedulerClient, applicationId, jobName),
                Mono.just(spaceId)
            )))
            .flatMapMany(function((jobId, spaceId) -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .list(ListJobsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .filter(resource -> jobName.endsWith(resource.getName()))
            .map(Job::getCommand)
            .as(StepVerifier::create)
            .expectNext("ls")
            .verifyComplete();
    }

    @Test
    public void listDetailed() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName),
                Mono.just(spaceId)
            ))
            .flatMap(function((applicationId, schedulerId, spaceId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(Tuples.of(applicationId, spaceId))))
            .flatMap(function((applicationId, spaceId) -> Mono.zip(
                createJobId(this.schedulerClient, applicationId, jobName),
                Mono.just(spaceId)
            )))
            .flatMap(function((jobId, spaceId) -> requestScheduleJob(this.schedulerClient, CRON_EXPRESSION_SLOW, jobId)
                .thenReturn(spaceId)))
            .flatMapMany(spaceId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .list(ListJobsRequest.builder()
                        .detailed(true)
                        .page(page)
                        .spaceId(spaceId)
                        .build())))
            .filter(resource -> jobName.endsWith(resource.getName()))
            .flatMapIterable(Job::getJobSchedules)
            .map(JobSchedule::getExpression)
            .as(StepVerifier::create)
            .expectNext(CRON_EXPRESSION_SLOW)
            .verifyComplete();
    }

    @Test
    public void listDetailedNoSchedules() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName),
                Mono.just(spaceId)
            ))
            .flatMap(function((applicationId, schedulerId, spaceId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(Tuples.of(applicationId, spaceId))))
            .flatMap(function((applicationId, spaceId) -> createJobId(this.schedulerClient, applicationId, jobName)
                .thenReturn(spaceId)))
            .flatMapMany(spaceId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .list(ListJobsRequest.builder()
                        .detailed(true)
                        .page(page)
                        .spaceId(spaceId)
                        .build())))
            .filter(resource -> jobName.endsWith(resource.getName()))
            .map(job -> Optional.ofNullable(job.getJobSchedules()))
            .as(StepVerifier::create)
            .expectNext(Optional.empty())
            .verifyComplete();
    }

    @Test
    public void listHistories() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .delayUntil(jobId -> requestExecuteJob(this.schedulerClient, jobId))
            .flatMapMany(jobId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .listHistories(ListJobHistoriesRequest.builder()
                        .jobId(jobId)
                        .page(page)
                        .build())))
            .map(JobHistory::getState)
            .as(StepVerifier::create)
            .expectNext("FAILED")
            .verifyComplete();
    }

    @Test
    public void listNoneFound() {
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMapMany(spaceId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .list(ListJobsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build())))
            .filter(resource -> jobName.endsWith(resource.getName()))
            .map(Job::getCommand)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void listScheduleHistories() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .flatMap(jobId -> Mono.zip(
                Mono.just(jobId),
                createScheduleId(this.schedulerClient, CRON_EXPRESSION_FAST, jobId)
            ))
            .delayElement(Duration.ofSeconds(90))
            .flatMapMany(function((jobId, scheduleId) -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .listScheduleHistories(ListJobScheduleHistoriesRequest.builder()
                        .jobId(jobId)
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
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .delayUntil(jobId -> requestScheduleJob(this.schedulerClient, CRON_EXPRESSION_SLOW, jobId))
            .flatMapMany(jobId -> io.pivotal.reactor.util.PaginationUtils
                .requestResources(page -> this.schedulerClient.jobs()
                    .listSchedules(ListJobSchedulesRequest.builder()
                        .jobId(jobId)
                        .page(page)
                        .build())))
            .map(JobSchedule::getExpression)
            .as(StepVerifier::create)
            .expectNext(CRON_EXPRESSION_SLOW)
            .verifyComplete();
    }

    @Test
    public void schedule() {
        String applicationName = this.nameFactory.getApplicationName();
        String jobName = this.nameFactory.getJobName();

        this.spaceId
            .flatMap(spaceId -> Mono.zip(
                createApplicationId(this.cloudFoundryClient, applicationName, spaceId),
                getSchedulerServiceId(this.cloudFoundryClient, this.schedulerServiceInstanceName)
            ))
            .flatMap(function((applicationId, schedulerId) -> requestCreateServiceBinding(this.cloudFoundryClient, applicationId, schedulerId)
                .thenReturn(applicationId)))
            .flatMap(applicationId -> createJobId(this.schedulerClient, applicationId, jobName))
            .delayUntil(jobId -> this.schedulerClient.jobs()
                .schedule(ScheduleJobRequest.builder()
                    .enabled(true)
                    .expression(CRON_EXPRESSION_SLOW)
                    .expressionType(CRON)
                    .jobId(jobId)
                    .build()))
            .flatMapMany(jobId -> requestListJobSchedules(this.schedulerClient, jobId))
            .map(JobSchedule::getExpression)
            .as(StepVerifier::create)
            .expectNext(CRON_EXPRESSION_SLOW)
            .verifyComplete();
    }

    private static Mono<String> createApplicationId(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return requestCreateApplication(cloudFoundryClient, applicationName, spaceId)
            .map(ResourceUtils::getId);
    }

    private static Mono<String> createJobId(ReactorSchedulerClient schedulerClient, String applicationId, String jobName) {
        return requestCreateJob(schedulerClient, applicationId, jobName)
            .map(CreateJobResponse::getId);
    }

    private static Mono<String> createScheduleId(ReactorSchedulerClient schedulerClient, String cronExpression, String jobId) {
        return requestScheduleJob(schedulerClient, cronExpression, jobId)
            .map(ScheduleJobResponse::getId);
    }

    private static Mono<String> getJobName(ReactorSchedulerClient schedulerClient, String jobId) {
        return requestGetJob(schedulerClient, jobId)
            .map(GetJobResponse::getName);
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

    private static Mono<CreateJobResponse> requestCreateJob(ReactorSchedulerClient schedulerClient, String applicationId, String jobName) {
        return schedulerClient.jobs()
            .create(CreateJobRequest.builder()
                .applicationId(applicationId)
                .command("ls")
                .name(jobName)
                .build());
    }

    private static Mono<CreateServiceBindingResponse> requestCreateServiceBinding(CloudFoundryClient cloudFoundryClient, String applicationId, String schedulerId) {
        return cloudFoundryClient.serviceBindingsV2()
            .create(CreateServiceBindingRequest.builder()
                .applicationId(applicationId)
                .serviceInstanceId(schedulerId)
                .build());
    }

    private static Mono<ExecuteJobResponse> requestExecuteJob(ReactorSchedulerClient schedulerClient, String jobId) {
        return schedulerClient.jobs()
            .execute(ExecuteJobRequest.builder()
                .jobId(jobId)
                .build());
    }

    private static Mono<GetJobResponse> requestGetJob(ReactorSchedulerClient schedulerClient, String jobId) {
        return schedulerClient.jobs()
            .get(GetJobRequest.builder()
                .jobId(jobId)
                .build());
    }

    private static Flux<JobSchedule> requestListJobSchedules(ReactorSchedulerClient schedulerClient, String jobId) {
        return io.pivotal.reactor.util.PaginationUtils
            .requestResources(page -> schedulerClient.jobs()
                .listSchedules(ListJobSchedulesRequest.builder()
                    .jobId(jobId)
                    .page(page)
                    .build()));
    }

    private static Mono<ScheduleJobResponse> requestScheduleJob(ReactorSchedulerClient schedulerClient, String cronExpression, String jobId) {
        return schedulerClient.jobs()
            .schedule(ScheduleJobRequest.builder()
                .enabled(true)
                .expression(cronExpression)
                .expressionType(CRON)
                .jobId(jobId)
                .build());
    }

}
