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

package io.pivotal.scheduler.v1.schedules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The expression type of a schedule
 */
public enum ExpressionType {

    /**
     * The cron expression type
     */
    CRON("cron_expression"),

    /**
     * The execute type
     */
    EXECUTE("execute");

    private final String value;

    ExpressionType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ExpressionType from(String s) {
        switch (s.toLowerCase()) {
            case "cron_expression":
                return CRON;
            case "execute":
                return EXECUTE;
            default:
                throw new IllegalArgumentException(String.format("Unknown expression type: %s", s));
        }
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
