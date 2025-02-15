/*
 * Copyright 2020 webtau maintainers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.testingisdocumenting.webtau.http.testserver;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.testingisdocumenting.webtau.utils.JsonUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
There is no "standard" for handling GraphQL over HTTP but GraphQL provides some best practices for this which we follow here:
https://graphql.org/learn/serving-over-http/.

Note: In this handler, GraphQL endpoints must start with "/grapqhl".
 */
public class GraphQLResponseHandler extends Handler.Abstract {
    private final GraphQL graphQL;
    private final Optional<Handler> additionalHandler;
    private Optional<String> expectedAuthHeaderValue;
    private int successStatusCode = 200;

    public GraphQLResponseHandler(GraphQLSchema schema) {
        this(schema, null);
    }

    public GraphQLResponseHandler(GraphQLSchema schema, Handler additionalHandler) {
        this.graphQL = GraphQL.newGraphQL(schema).build();
        this.additionalHandler = Optional.ofNullable(additionalHandler);
        this.expectedAuthHeaderValue = Optional.empty();
    }


    /**
   * If the endpoint starts with "/graphql", treat it as a GraphQL request, otherwise delegate to
   * the optional additionalHandler.
   */
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (request.getHttpURI().getPath().startsWith("/graphql")) {
            handleGraphQLPathRequest(request, response);
        } else if (additionalHandler.isPresent()) {
            additionalHandler.get().handle(request, response, callback);
        } else {
            response.setStatus(404);
            callback.succeeded();
        }

        return true;
    }

    public <R> R withAuthEnabled(String expectedAuthHeaderValue, Supplier<R> code) {
        this.expectedAuthHeaderValue = Optional.of(expectedAuthHeaderValue);
        try {
            return code.get();
        } finally {
            this.expectedAuthHeaderValue = Optional.empty();
        }
    }

    public void withAuthEnabled(String expectedAuthHeaderValue, Runnable code) {
        withAuthEnabled(expectedAuthHeaderValue, () -> { code.run(); return null; });
    }

    public void withSuccessStatusCode(int successStatusCode, Runnable code) {
        int originalSuccessCode = this.successStatusCode;
        this.successStatusCode = successStatusCode;
        try {
            code.run();
        } finally {
            this.successStatusCode = originalSuccessCode;
        }
    }

    private void handleGraphQLPathRequest(Request request, Response response) throws IOException {
        if (!isAuthenticated(request)) {
            response.setStatus(401);
            return;
        }

        if ("GET".equals(request.getMethod())) {
            final Fields parameters;
            try {
                parameters = Request.getParameters(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String query = parameters.getValue("query");
            String operationName = parameters.getValue("operationName");
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) JsonUtils.deserializeAsMap(parameters.getValue("variables"));

            handle(query, operationName, variables, response);
        } else if ("POST".equals(request.getMethod())) {
            // Don't currently support the query param for POST
            if ("application/json".equals(request.getHeaders().get(HttpHeader.CONTENT_TYPE))) {
                Map<String, ?> requestBody = JsonUtils.deserializeAsMap(Content.Source.asString(request, StandardCharsets.UTF_8).lines().collect(Collectors.joining()));

                String query = (String) requestBody.get("query");
                String operationName = (String) requestBody.get("operationName");
                @SuppressWarnings("unchecked")
                Map<String, Object> variables = (Map<String, Object>) requestBody.get("variables");

                handle(query, operationName, variables, response);
            } else if ("application/graphql".equals(request.getHeaders().get(HttpHeader.CONTENT_TYPE))) {
                String query = Content.Source.asString(request, StandardCharsets.UTF_8).lines().collect(Collectors.joining());
                handle(query, (String) null, null, response);
            } else {
                response.setStatus(415);
            }
        } else {
            response.setStatus(405);
        }
    }

    private boolean isAuthenticated(Request request) {
        return expectedAuthHeaderValue
                .map(expectedVal -> expectedVal.equals(request.getHeaders().get("Authorization")))
                .orElse(true);
    }

    private void handle(
            String query,
            String operationName,
            Map<String, Object> variables,
            Response response) throws IOException {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput(query)
                .operationName(operationName)
                .variables(variables == null ? Collections.emptyMap() : variables)
                .build();
        ExecutionResult result = graphQL.execute(executionInput);
        Map<String, Object> responseBody = new HashMap<>();
        if (result.isDataPresent()) {
            responseBody.put("data", result.getData());
        }
        if (result.getErrors() != null && result.getErrors().size() > 0) {
            responseBody.put("errors", result.getErrors());
        }

        response.setStatus(successStatusCode);
        response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json");
        response.write(true, ByteBuffer.wrap(JsonUtils.serializeToBytes(responseBody)), null);
    }
}
