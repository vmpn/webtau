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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.utils.JsonUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class TestServerFakeFileUpload implements TestServerResponse {
    @Override
    public byte[] responseBody(Request request) throws IOException, ServletException {
        final var config = Request.getMultiPartConfig(request, null).build();
        final var parts = MultiPartFormData.getParts(request, request, request.getHeaders().get(HttpHeader.CONTENT_TYPE), config);
        final var partsList = StreamSupport.stream(parts.spliterator(), false).toList();
        final var response = createResponse(partsList);
        return JsonUtils.serialize(response).getBytes();
    }

    @Override
    public String responseType(Request request) {
        return "application/json";
    }

    @Override
    public int responseStatusCode() {
        return 201;
    }

    private Map<String, Object> createResponse(Collection<MultiPart.Part> parts) {
        Optional<MultiPart.Part> file = findPart(parts, "file");
        Optional<MultiPart.Part> descriptionPart = findPart(parts, "fileDescription");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("fileName", file.map(MultiPart.Part::getFileName).orElse("backend-generated-name-as-no-name-provided"));
        result.put("description", descriptionPart.map(this::extractContent).orElse(null));

        return result;
    }

    private Optional<MultiPart.Part> findPart(Collection<MultiPart.Part> parts, String name) {
        return parts.stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    private String extractContent(MultiPart.Part p) {
        try {
            return IOUtils.toString(Content.Source.asInputStream(p.getContentSource()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
