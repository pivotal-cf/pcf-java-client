/*
 * Copyright 2018-2019 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpStatusClass;
import io.pivotal.UnknownSchedulerException;
import io.pivotal.scheduler.v1.SchedulerError;
import io.pivotal.scheduler.v1.SchedulerException;
import org.cloudfoundry.reactor.HttpClientResponseWithConnection;
import org.cloudfoundry.reactor.util.ErrorPayloadMapper;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClientResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpStatusClass.CLIENT_ERROR;
import static io.netty.handler.codec.http.HttpStatusClass.SERVER_ERROR;

public final class ErrorPayloadMappers {

    @SuppressWarnings("unchecked")
    public static ErrorPayloadMapper scheduler(ObjectMapper objectMapper) {
        return inbound -> inbound
            .flatMap(mapToError((statusCode, payload) -> {
                Map<String, Object> map = objectMapper.readValue(payload, Map.class);
                String description = (String) map.get("description");
                List<SchedulerError> errors = ((List<Map<String, Object>>) map.get("errors")).stream()
                    .map(error -> SchedulerError.builder()
                        .resource((String) error.get("resource"))
                        .messages((List<String>) error.get("messages"))
                        .build())
                    .collect(Collectors.toList());

                return new SchedulerException(statusCode, description, errors);
            }));
    }

    private static boolean isError(HttpClientResponse response) {
        HttpStatusClass statusClass = response.status().codeClass();
        return statusClass == CLIENT_ERROR || statusClass == SERVER_ERROR;
    }

    private static Function<HttpClientResponseWithConnection, Mono<HttpClientResponseWithConnection>> mapToError(ExceptionGenerator exceptionGenerator) {
        return response -> {
            if (!isError(response.getResponse())) {
                return Mono.just(response);
            }

            Connection connection = response.getConnection();
            ByteBufFlux body = ByteBufFlux.fromInbound(connection.inbound().receive()
                .doFinally(signalType -> connection.dispose()));

            return body.aggregate().asString()
                .switchIfEmpty(Mono.error(new UnknownSchedulerException(response.getResponse().status().code())))
                .flatMap(payload -> {
                    try {
                        return Mono.error(exceptionGenerator.apply(response.getResponse().status().code(), payload));
                    } catch (Exception e) {
                        return Mono.error(new UnknownSchedulerException(response.getResponse().status().code(), payload));
                    }
                });
        };
    }

    @FunctionalInterface
    private interface ExceptionGenerator {

        RuntimeException apply(Integer statusCode, String payload) throws Exception;

    }

}
