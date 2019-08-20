/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.http;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Iterables;
import com.palantir.atlasdb.http.negotiation.AtlasDbHttpProtocolVersion;
import com.palantir.remoting2.errors.SerializableError;

public final class ExceptionMappers {
    private ExceptionMappers() {
        // utility
    }

    public static Response encodeAsUnavailable(Exception exception, Optional<Duration> retryAfter) {
        Response.ResponseBuilder builder = Response.status(503);
        retryAfter.ifPresent(backoff ->
                builder.header(com.google.common.net.HttpHeaders.RETRY_AFTER, Long.toString(backoff.toMillis())));
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        builder.entity(createSerializableError(exception));
        return builder.build();
    }

    /**
     * Returns a 503 response, with a JSON-serialized form of the causing exception as the body, and an appropriate
     * HTTP header (Content-Type: application/json).
     */
    public static Response encode503ResponseWithoutRetryAfter(Exception exception) {
        return encode503ResponseInternal(exception).build();
    }

    /**
     * Returns a 503 response, with a JSON-serialized form of the causing exception as the body, and an appropriate
     * HTTP header (Content-Type: application/json). In addition, we also include a Retry-After header with a zero
     * value.
     */
    public static Response encode503ResponseWithRetryAfter(Exception exception) {
        return encode503ResponseInternal(exception)
                .header(HttpHeaders.RETRY_AFTER, "0")
                .build();
    }

    /**
     * Attempts to parse the {@link AtlasDbHttpProtocolVersion} indicated by {@link HttpHeaders}.
     *
     * Does not handle multiple values.
     */
    static Optional<AtlasDbHttpProtocolVersion> tryParseProtocolVersion(HttpHeaders headers) {
        List<String> httpProtocolVersions = headers.getRequestHeader(AtlasDbHttpProtocolVersion.VERSION_HEADER);
        String httpProtocolVersion = Iterables.getOnlyElement(httpProtocolVersions);
        return Optional.ofNullable(httpProtocolVersion)
                .flatMap(AtlasDbHttpProtocolVersion::fromStringRepresentation);
    }


    private static Response.ResponseBuilder encode503ResponseInternal(Exception exception) {
        return encodeExceptionResponse(exception, 503);
    }

    /**
     * Returns a response builder with the specified status code, with a JSON serialized form of the causing exception,
     * and an appropriate HTTP header (Content-Type: application/json).
     */
    public static Response.ResponseBuilder encodeExceptionResponse(Exception exception, int statusCode) {
        return Response.serverError()
                .entity(createSerializableError(exception))
                .status(statusCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private static SerializableError createSerializableError(Exception exception) {
        return SerializableError.of(
                exception.getMessage(),
                exception.getClass(),
                getStackTraceElementsAsList(exception));
    }

    private static List<StackTraceElement> getStackTraceElementsAsList(Exception exception) {
        return Arrays.asList(exception.getStackTrace());
    }
}
