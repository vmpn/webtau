package org.testingisdocumenting.webtau.http.testserver;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseUtils {
    public static Map<String, String> echoHeaders(Request request) {
        Map<String, String> header = new LinkedHashMap<>();

        request.getHeaders().forEach( hf ->
        {
            header.put(hf.getName(), hf.getValue());
        });
        return header;
    }

    public static byte[] echoBody(Request request) {
        try {
            return IOUtils.toByteArray(Content.Source.asInputStream(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
