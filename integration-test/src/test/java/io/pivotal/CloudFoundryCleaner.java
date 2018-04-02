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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import javax.net.ssl.SSLException;
import java.time.Duration;

final class CloudFoundryCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger("cloudfoundry-client.test");

    CloudFoundryCleaner() {
    }

    void clean() {
        Flux.empty()
            .retry(5, t -> t instanceof SSLException)
            .doOnSubscribe(s -> LOGGER.debug(">> CLEANUP <<"))
            .doOnComplete(() -> LOGGER.debug("<< CLEANUP >>"))
            .then()
            .block(Duration.ofMinutes(30));
    }

}
