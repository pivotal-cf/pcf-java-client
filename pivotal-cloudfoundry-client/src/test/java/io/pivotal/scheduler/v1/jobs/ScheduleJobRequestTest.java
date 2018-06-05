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

package io.pivotal.scheduler.v1.jobs;

import org.junit.Test;

import static io.pivotal.scheduler.v1.schedules.ExpressionType.CRON;

public class ScheduleJobRequestTest {

    @Test(expected = IllegalStateException.class)
    public void noEnabled() {
        ScheduleJobRequest.builder()
            .expression("test-expression")
            .expressionType(CRON)
            .jobId("test-job-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noExpression() {
        ScheduleJobRequest.builder()
            .enabled(true)
            .expressionType(CRON)
            .jobId("test-job-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noExpressionType() {
        ScheduleJobRequest.builder()
            .enabled(true)
            .expression("test-expression")
            .jobId("test-job-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noJobId() {
        ScheduleJobRequest.builder()
            .enabled(true)
            .expression("test-expression")
            .expressionType(CRON)
            .build();
    }

    @Test
    public void valid() {
        ScheduleJobRequest.builder()
            .enabled(true)
            .expression("test-expression")
            .expressionType(CRON)
            .jobId("test-job-id")
            .build();
    }

}
