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

package org.testingisdocumenting.webtau.server;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * Server can register a response based on low-level servlet request.
 */
public interface WebTauServerOverride {
    boolean matchesUri(String method, String uri);

    String overrideId();

    WebTauServerResponse response(Request request);

    default void apply(Request servletRequest, Response servletResponse, Callback callback) {
        WebTauServerResponse serverResponse = response(servletRequest);
        serverResponse.getHeader().forEach((k, v) -> servletResponse.getHeaders().add(k, v.toString()));

        byte[] responseBody = serverResponse.getContent();
        servletResponse.setStatus(serverResponse.getStatusCode());
        servletResponse.getHeaders().add(HttpHeader.CONTENT_TYPE, serverResponse.getContentType());

        if (responseBody != null) {
            servletResponse.write(true, ByteBuffer.wrap(responseBody), callback);
        }
    }
}
