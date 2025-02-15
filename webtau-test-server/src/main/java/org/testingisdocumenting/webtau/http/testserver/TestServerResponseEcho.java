/*
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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;

import static org.testingisdocumenting.webtau.http.testserver.ResponseUtils.echoBody;

public class TestServerResponseEcho implements TestServerResponse {
    private final int statusCode;

    public TestServerResponseEcho(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public byte[] responseBody(Request request) {
        return echoBody(request);
    }

    @Override
    public String responseType(Request request) {
        return request.getHeaders().get(HttpHeader.CONTENT_TYPE);
    }

    @Override
    public int responseStatusCode() {
        return statusCode;
    }
}
