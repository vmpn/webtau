package org.testingisdocumenting.webtau.http.testserver;

import jakarta.servlet.ServletException;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.utils.JsonUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestServerJsonDerivativeResponse implements TestServerResponse {
    @Override
    public byte[] responseBody(Request request) throws IOException, ServletException {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");

        return IOUtils.toByteArray(new StringReader(JsonUtils.serializePrettyPrint(response)),
                StandardCharsets.UTF_8);
    }

    @Override
    public String responseType(Request request) {
        try {
            String json = Content.Source.asString(request, StandardCharsets.UTF_8);
            Map<String, ?> body = json.equals("") ? Collections.emptyMap() : JsonUtils.deserializeAsMap(json);
            return (String) body.get("contentType");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int responseStatusCode() {
        return 201;
    }
}
