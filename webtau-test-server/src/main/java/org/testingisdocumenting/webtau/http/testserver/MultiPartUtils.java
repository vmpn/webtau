package org.testingisdocumenting.webtau.http.testserver;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.server.Request;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MultiPartUtils {
    public static List<MultiPart.Part> getPartsList(Request request) {
        return getPartsStream(request).toList();
    }

    public static Stream<MultiPart.Part> getPartsStream(Request request) {
        final var config = Request.getMultiPartConfig(request, null).build();
        final var parts = MultiPartFormData.getParts(request, request, request.getHeaders().get(HttpHeader.CONTENT_TYPE), config);
        return StreamSupport.stream(parts.spliterator(), false);

    }
}
