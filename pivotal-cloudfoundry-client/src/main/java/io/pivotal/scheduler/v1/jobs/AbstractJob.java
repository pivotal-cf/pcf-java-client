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

package io.pivotal.scheduler.v1.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.scheduler.v1.Resource;
import org.cloudfoundry.Nullable;

/**
 * Base class for responses that are Jobs
 */
public abstract class AbstractJob extends Resource {

    /**
     * ID of the application this job runs commands against
     */
    @JsonProperty("app_guid")
    @Nullable
    abstract String getApplicationId();

    /**
     * Command to be executed
     */
    @JsonProperty("command")
    @Nullable
    abstract String getCommand();

    /**
     * Time the job was created
     */
    @JsonProperty("created_at")
    @Nullable
    abstract String getCreatedAt();

    /**
     * Name of the job
     */
    @JsonProperty("name")
    @Nullable
    abstract String getName();

    /**
     * ID of the space that the app is running inside
     */
    @JsonProperty("space_guid")
    @Nullable
    abstract String getSpaceId();

    /**
     * Most recent state of the job
     */
    @JsonProperty("state")
    @Nullable
    abstract String getState();

    /**
     * Time when the job was last updated
     */
    @JsonProperty("updated_at")
    @Nullable
    abstract String getUpdatedAt();

}
