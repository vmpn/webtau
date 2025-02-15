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

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FixedResponsesHandler extends Handler.Abstract {
    private final Map<String, TestServerResponse> getResponses = new HashMap<>();
    private final Map<String, TestServerResponse> patchResponses = new HashMap<>();
    private final Map<String, TestServerResponse> postResponses = new HashMap<>();
    private final Map<String, TestServerResponse> putResponses = new HashMap<>();
    private final Map<String, TestServerResponse> deleteResponses = new HashMap<>();

    public void registerGet(String relativeUrl, TestServerResponse response) {
        getResponses.put(relativeUrl, response);
    }

    public void registerPatch(String relativeUrl, TestServerResponse response) {
        patchResponses.put(relativeUrl, response);
    }

    public void registerPost(String relativeUrl, TestServerResponse response) {
        postResponses.put(relativeUrl, response);
    }

    public void registerPut(String relativeUrl, TestServerResponse response) {
        putResponses.put(relativeUrl, response);
    }

    public void registerDelete(String relativeUrl, TestServerResponse response) {
        deleteResponses.put(relativeUrl, response);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        Map<String, TestServerResponse> responses = findResponses(request);

        TestServerResponse testServerResponse = responses.get(request.getHttpURI().toURI().toString());
        if (testServerResponse == null) {
            response.setStatus(404);
        } else {
            testServerResponse.responseHeader(request).forEach(response.getHeaders()::add);

            byte[] responseBody = testServerResponse.responseBody(request);
            response.setStatus(testServerResponse.responseStatusCode());
            response.getHeaders().add(HttpHeader.CONTENT_TYPE, testServerResponse.responseType(request));

            if (responseBody != null) {
                response.getHeaders().add(HttpHeader.CONTENT_LENGTH, responseBody.length);
                response.write(true, ByteBuffer.wrap(responseBody), null);
            }
        }

        return true;
    }

    private Map<String, TestServerResponse> findResponses(Request request) {
        switch (request.getMethod()) {
            case "GET":
                return getResponses;
            case "PATCH":
                return patchResponses;
            case "POST":
                return postResponses;
            case "PUT":
                return putResponses;
            case "DELETE":
                return deleteResponses;
            default:
                return Collections.emptyMap();

        }
    }
}
