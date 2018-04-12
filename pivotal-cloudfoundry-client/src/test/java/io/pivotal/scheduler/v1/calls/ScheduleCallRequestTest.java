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

import org.junit.Test;

import static io.pivotal.scheduler.v1.ExpressionType.CRON;

public class ScheduleCallRequestTest {

    @Test(expected = IllegalStateException.class)
    public void noEnabled() {
        ScheduleCallRequest.builder()
            .expression("test-expression")
            .expressionType(CRON)
            .callId("test-call-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noExpression() {
        ScheduleCallRequest.builder()
            .enabled(true)
            .expressionType(CRON)
            .callId("test-call-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noExpressionType() {
        ScheduleCallRequest.builder()
            .enabled(true)
            .expression("test-expression")
            .callId("test-call-id")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noJobId() {
        ScheduleCallRequest.builder()
            .enabled(true)
            .expression("test-expression")
            .expressionType(CRON)
            .build();
    }

    @Test
    public void valid() {
        ScheduleCallRequest.builder()
            .enabled(true)
            .expression("test-expression")
            .expressionType(CRON)
            .callId("test-call-id")
            .build();
    }

}
