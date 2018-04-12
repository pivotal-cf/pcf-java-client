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
import org.cloudfoundry.Nullable;
import org.cloudfoundry.QueryParameter;
import org.immutables.value.Value;

/**
 * The request payload for the Create Call operation
 */
@Value.Immutable
abstract class _CreateCallRequest {

    /**
     * The application id
     */
    @Nullable
    @QueryParameter("app_guid")
    abstract String getApplicationId();

    /**
     * Authorization header of the request made to the call’s Http endpoint
     */
    @JsonProperty("auth_header")
    abstract String getAuthorizationHeader();

    /**
     * Name of the call
     */
    @JsonProperty("name")
    abstract String getName();

    /**
     * Http endpoint that the call will make requests to
     */
    @JsonProperty("url")
    abstract String getUrl();

}
