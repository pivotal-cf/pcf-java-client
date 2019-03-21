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

package io.pivotal.reactor.scheduler.v1;

import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.QueryBuilder;
import org.cloudfoundry.reactor.util.AbstractReactorOperations;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class AbstractSchedulerV1Operations extends AbstractReactorOperations {

    protected AbstractSchedulerV1Operations(ConnectionContext connectionContext, Mono<String> root, TokenProvider tokenProvider) {
        super(connectionContext, root, tokenProvider);
    }

    protected final <T> Mono<T> delete(Object requestPayload, Class<T> responseType, Function<UriComponentsBuilder, UriComponentsBuilder> uriTransformer) {
        return doDelete(requestPayload, responseType,
            queryTransformer(requestPayload)
                .andThen(uriTransformer),
            outbound -> outbound,
            inbound -> inbound);
    }

    protected final <T> Mono<T> get(Object requestPayload, Class<T> responseType, Function<UriComponentsBuilder, UriComponentsBuilder> uriTransformer) {
        return doGet(responseType,
            queryTransformer(requestPayload)
                .andThen(uriTransformer),
            outbound -> outbound,
            inbound -> inbound);
    }

    protected final <T> Mono<T> post(Object requestPayload, Class<T> responseType, Function<UriComponentsBuilder, UriComponentsBuilder> uriTransformer) {
        return doPost(requestPayload, responseType,
            queryTransformer(requestPayload)
                .andThen(uriTransformer),
            outbound -> outbound,
            inbound -> inbound);
    }

    private static Function<UriComponentsBuilder, UriComponentsBuilder> queryTransformer(Object requestPayload) {
        return builder -> {
            QueryBuilder.augment(builder, requestPayload);
            return builder;
        };
    }

}
