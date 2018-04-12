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

package io.pivotal.reactor.util;

import io.pivotal.scheduler.v1.PaginatedResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

/**
 * A utility class to provide functions for handling {@link PaginatedResponse}s and those containing lists of {@link Resource}s.
 */
public final class PaginationUtils {

    private PaginationUtils() {
    }

    /**
     * Generate the stream of resources accumulated from a series of responses obtained from the page supplier.
     *
     * @param pageSupplier a function from integers to {@link Mono}s of {@link PaginatedResponse}s.
     * @param <T>          the type of resource in the list on each {@link PaginatedResponse}.
     * @param <U>          the type of {@link PaginatedResponse}.
     * @return a stream of <code>T</code> objects.
     */
    @SuppressWarnings("rawtypes")
    public static <T, U extends PaginatedResponse<T>> Flux<T> requestResources(Function<Integer, Mono<U>> pageSupplier) {
        return pageSupplier
            .apply(1)
            .flatMapMany(requestAdditionalPages(pageSupplier))
            .flatMapIterable(PaginatedResponse::getResources);
    }

    private static <T> Function<T, Flux<T>> requestAdditionalPages(Function<Integer, Mono<T>> pageSupplier, Function<T, Integer> totalPagesSupplier) {
        return response -> {
            Integer totalPages = Optional.ofNullable(totalPagesSupplier.apply(response)).orElse(1);

            return Flux
                .range(2, totalPages - 1)
                .flatMap(pageSupplier)
                .startWith(response)
                .buffer()
                .flatMapIterable(d -> d);
        };
    }

    private static <T extends PaginatedResponse<?>> Function<T, Flux<T>> requestAdditionalPages(Function<Integer, Mono<T>> pageSupplier) {
        return requestAdditionalPages(pageSupplier, response -> response.getPagination().getTotalPages());
    }

}
