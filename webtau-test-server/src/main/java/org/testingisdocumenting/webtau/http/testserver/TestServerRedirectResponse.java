package org.testingisdocumenting.webtau.http.testserver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.utils.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestServerRedirectResponse implements TestServerResponse {
    private final int statusCode;
    private final TestServer testServer;
    private final String redirectPath;

    public TestServerRedirectResponse(int statusCode, TestServer testServer, String redirectPath) {
        this.statusCode = statusCode;
        this.testServer = testServer;
        this.redirectPath = redirectPath;
    }

    private String redirectUrl() {
        return StringUtils.stripTrailing(testServer.getUri().toString(), '/')
                + StringUtils.ensureStartsWith(redirectPath, "/");
    }

    @Override
    public byte[] responseBody(Request request) throws IOException, ServletException {
        String msg = "Redirecting to " + redirectUrl() + " with status code " + statusCode;
        return msg.getBytes();
    }

    @Override
    public String responseType(Request request) {
        return "application/text";
    }

    @Override
    public Map<String, String> responseHeader(Request request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", redirectUrl());
        return headers;
    }

    @Override
    public int responseStatusCode() {
        return statusCode;
    }
}
