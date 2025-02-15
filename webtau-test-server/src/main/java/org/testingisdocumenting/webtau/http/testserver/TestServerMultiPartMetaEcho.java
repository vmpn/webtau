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

import jakarta.servlet.ServletException;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.utils.JsonUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.testingisdocumenting.webtau.http.testserver.MultiPartUtils.getPartsStream;

public class TestServerMultiPartMetaEcho implements TestServerResponse {
    private final int statusCode;

    public TestServerMultiPartMetaEcho(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public byte[] responseBody(Request request) throws IOException, ServletException {
        final var mappedParts = getPartsStream(request).map(this::partToMap).toList();
        return JsonUtils.serialize(mappedParts).getBytes();
    }

    @Override
    public String responseType(Request request) {
        return "application/json";
    }

    @Override
    public int responseStatusCode() {
        return statusCode;
    }

    private Map<String, Object> partToMap(MultiPart.Part part) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fieldName", part.getName());
        result.put("fileName", part.getFileName());

        return result;
    }
}
