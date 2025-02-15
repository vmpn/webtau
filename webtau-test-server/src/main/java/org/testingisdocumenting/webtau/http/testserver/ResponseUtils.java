package org.testingisdocumenting.webtau.http.testserver;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ResponseUtils {
    private final static Set<String> NON_ECHOED_HEADERS = Set.of(
            HttpHeader.CONTENT_LENGTH.lowerCaseName(),
            HttpHeader.CONTENT_TYPE.lowerCaseName());
    public static Map<String, String> echoHeaders(Request request) {
        Map<String, String> header = new LinkedHashMap<>();

        request.getHeaders().stream()
                .filter(hn -> !NON_ECHOED_HEADERS.contains(hn.getLowerCaseName()))
                .forEach( hf ->
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
