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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.scheduler.v1.Resource;
import org.cloudfoundry.Nullable;

/**
 * Base class for responses that are Job Schedules
 */
public abstract class JobSchedule extends Resource {

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
     * ID for the scheduled job
     */
    @JsonProperty("job_guid")
    @Nullable
    abstract String getJobId();

    /**
     * Schedule update time
     */
    @JsonProperty("updated_at")
    @Nullable
    abstract String getUpdatedAt();

}
