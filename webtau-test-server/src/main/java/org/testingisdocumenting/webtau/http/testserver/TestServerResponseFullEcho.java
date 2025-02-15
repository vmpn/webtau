/*
 * Copyright 2020 webtau maintainers
 * Copyright 2019 TWO SIGMA OPEN SOURCE, LLC
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

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.utils.JsonUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.testingisdocumenting.webtau.http.testserver.ResponseUtils.echoHeaders;

public class TestServerResponseFullEcho implements TestServerResponse {
    private final int statusCode;

    public TestServerResponseFullEcho(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public byte[] responseBody(Request request) {
        try {
            String input = Content.Source.asString(request, StandardCharsets.UTF_8);
            Object parsedRequest = parsedOrAsIsResponse(input);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("request", parsedRequest);
            response.put("urlPath", request.getHttpURI().getPath());
            response.put("urlQuery", request.getHttpURI().getQuery());

            response.putAll(echoHeaders(request));

            return IOUtils.toByteArray(new StringReader(JsonUtils.serializePrettyPrint(response)),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> responseHeader(Request request) {
        return echoHeaders(request);
    }

    @Override
    public String responseType(Request request) {
        return "application/json";
    }

    @Override
    public int responseStatusCode() {
        return statusCode;
    }

    private Object parsedOrAsIsResponse(String input) {
        if (input.isEmpty()) {
            return Collections.emptyMap();
        }

        if (input.startsWith("[") || input.startsWith("{")) {
            return input.startsWith("[") ?
                    JsonUtils.deserializeAsList(input) :
                    JsonUtils.deserializeAsMap(input);
        }

        return input;
    }
}
