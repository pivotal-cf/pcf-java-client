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
import io.pivotal.scheduler.v1.Resource;
import org.cloudfoundry.Nullable;

/**
 * Base class for responses that are Calls
 */
public abstract class Call extends Resource {

    /**
     * ID of the application this job runs commands against
     */
    @JsonProperty("app_guid")
    @Nullable
    abstract String getApplicationId();

    /**
     * Authorization header of the request made to the call’s Http endpoint
     */
    @JsonProperty("auth_header")
    abstract String getAuthorizationHeader();

    /**
     * Call creation time
     */
    @JsonProperty("created_at")
    @Nullable
    abstract String getCreatedAt();

    /**
     * Call name
     */
    @JsonProperty("name")
    @Nullable
    abstract String getName();

    /**
     * ID of the space containing the app associated with this call
     */
    @JsonProperty("space_guid")
    @Nullable
    abstract String getSpaceId();

    /**
     * Last time the call was updated
     */
    @JsonProperty("updated_at")
    @Nullable
    abstract String getUpdatedAt();

    /**
     * Endpoint where the call will make a request
     */
    @JsonProperty("url")
    abstract String getUrl();

}
