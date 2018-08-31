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

package io.pivotal;

import org.cloudfoundry.AbstractCloudFoundryException;

/**
 * A exception representing a Scheduler error that cannot be mapped to any other error.
 */
public final class UnknownSchedulerException extends AbstractCloudFoundryException {

    private static final long serialVersionUID = 8242780600598462905L;

    private final String payload;

    /**
     * Creates a new instance
     *
     * @param statusCode the status code
     * @param payload    the payload of the error
     */
    public UnknownSchedulerException(Integer statusCode, String payload) {
        super(statusCode, "Unknown Scheduler Exception");
        this.payload = payload;
    }

    /**
     * Creates a new instance
     *
     * @param statusCode the status code
     */
    public UnknownSchedulerException(Integer statusCode) {
        this(statusCode, null);
    }

    /**
     * Returns the payload of the error
     */
    public String getPayload() {
        return this.payload;
    }

}
