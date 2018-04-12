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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.scheduler.v1.ExpressionType;
import io.pivotal.scheduler.v1.Resource;
import org.cloudfoundry.Nullable;

/**
 * Base class for responses that are Call Schedules
 */
public abstract class CallSchedule extends Resource {

    /**
     * ID for the scheduled call
     */
    @JsonProperty("call_guid")
    @Nullable
    abstract String getCallId();

    /**
     * Schedule creation time
     */
    @JsonProperty("created_at")
    @Nullable
    abstract String getCreatedAt();

    /**
     * Whether the schedule is enabled
     */
    @JsonProperty("enabled")
    abstract Boolean getEnabled();

    /**
     * Expression defining when the schedule will run
     */
    @JsonProperty("expression")
    abstract String getExpression();

    /**
     * Schedule expression type
     */
    @JsonProperty("expression_type")
    abstract ExpressionType getExpressionType();

    /**
     * Schedule update time
     */
    @JsonProperty("updated_at")
    @Nullable
    abstract String getUpdatedAt();

}
