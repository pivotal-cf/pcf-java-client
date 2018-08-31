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

import org.cloudfoundry.AbstractCloudFoundryException;
import org.cloudfoundry.Nullable;

import java.util.List;

/**
 * An exception encapsulating an error returned from the Cloud Foundry Scheduler API
 */
public final class SchedulerException extends AbstractCloudFoundryException {

    private static final long serialVersionUID = -1335773222917771462L;

    private final String description;

    private final List<SchedulerError> errors;

    /**
     * Creates a new instance
     *
     * @param statusCode  the status code
     * @param description the description
     */
    public SchedulerException(Integer statusCode, String description, List<SchedulerError> errors) {
        super(statusCode, description);
        this.description = description;
        this.errors = errors;
    }

    /**
     * Returns the description
     */
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public List<SchedulerError> getErrors() {
        return this.errors;
    }

}
