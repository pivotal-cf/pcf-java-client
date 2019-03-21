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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.scheduler.v1.Resource;
import org.cloudfoundry.Nullable;

/**
 * Base class for responses that are Call Histories
 */
public abstract class AbstractCallHistory extends Resource {

    /**
     * The call ID
     */
    @JsonProperty("call_guid")
    @Nullable
    abstract String getCallId();

    /**
     * Time when the associated schedule finished execution
     */
    @JsonProperty("execution_end_time")
    @Nullable
    abstract String getExecutionEndTime();

    /**
     * Time when the associated schedule started execution
     */
    @JsonProperty("execution_start_time")
    @Nullable
    abstract String getExecutionStartTime();

    /**
     * Output message of the scheduled execution
     */
    @JsonProperty("message")
    @Nullable
    abstract String getMessage();

    /**
     * ID of the schedule associated to this history
     */
    @JsonProperty("schedule_guid")
    @Nullable
    abstract String getScheduleId();

    /**
     * Time that the associated schedule was scheduled to execute
     */
    @JsonProperty("scheduled_time")
    @Nullable
    abstract String getScheduledTime();

    /**
     * History state
     */
    @JsonProperty("state")
    @Nullable
    abstract String getState();

}
