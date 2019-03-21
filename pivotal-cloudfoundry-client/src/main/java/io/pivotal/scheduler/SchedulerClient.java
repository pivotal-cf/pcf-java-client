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

package io.pivotal.scheduler;

import io.pivotal.scheduler.v1.calls.Calls;
import io.pivotal.scheduler.v1.jobs.Jobs;

/**
 * Main entry point to the Scheduler Client API
 */
public interface SchedulerClient {

    /**
     * Main entry point to the Calls API
     */
    Calls calls();

    /**
     * Main entry point to the Jobs API
     */
    Jobs jobs();

}
