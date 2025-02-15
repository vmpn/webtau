/*
 * Copyright 2021 webtau maintainers
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

package org.testingisdocumenting.webtau.server.registry;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebTauServerHandledRequest {
    public static final WebTauServerHandledRequest NULL = new WebTauServerHandledRequest();

    private final String method;
    private final String url;
    private final String requestType;
    private final String responseType;
    private final String capturedRequest;
    private final String capturedResponse;
    private final long startTime;
    private final long elapsedTime;
    private final int statusCode;

    private WebTauServerHandledRequest() {
        method = "";
        url = "";
        requestType = "";
        responseType = "";
        startTime = 0;
        elapsedTime = 0;
        statusCode = 0;
        capturedRequest = "[null handled request: captured request]";
        capturedResponse = "[null handled request: captured response]";
    }

    public WebTauServerHandledRequest(Request request, Response response,
                                      long startTime,
                                      long endTime,
                                      String capturedRequest,
                                      String capturedResponse) {
        this.method = request.getMethod();
        this.url = request.getHttpURI().getPathQuery();
        this.statusCode = response.getStatus();
        this.requestType = requestContentTypeOrEmpty(request);
        this.responseType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
        this.startTime = startTime;
        this.elapsedTime = endTime - startTime;

        this.capturedRequest = extractContent(request.getHeaders().get(HttpHeader.CONTENT_TYPE), capturedRequest);
        this.capturedResponse = extractContent(response.getHeaders().get(HttpHeader.CONTENT_TYPE), capturedResponse);
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getCapturedRequest() {
        return capturedRequest;
    }

    public String getCapturedResponse() {
        return capturedResponse;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public Map<String, ?> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("method", method);
        result.put("url", url);
        result.put("requestType", requestType);
        result.put("responseType", responseType != null ? responseType : "");
        result.put("capturedRequest", capturedRequest);
        result.put("capturedResponse", capturedResponse);
        result.put("startTime", startTime);
        result.put("elapsedTime", elapsedTime);
        result.put("statusCode", statusCode);

        return result;
    }

    public int getStatusCode() {
        return statusCode;
    }

    private String extractContent(String contentType, String captureAsString) {
        return isTextBasedContent(contentType) ?
                captureAsString :
                "[non text content]";
    }

    private static boolean isTextBasedContent(String contentType) {
        return contentType != null && (
                contentType.contains("text") ||
                        contentType.contains("html") ||
                        contentType.contains("json") ||
                        contentType.contains("xml"));
    }

    private String requestContentTypeOrEmpty(Request request) {
        String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
        return contentType != null ? contentType : "";
    }
}
