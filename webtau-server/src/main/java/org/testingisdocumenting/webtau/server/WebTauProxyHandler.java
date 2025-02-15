package org.testingisdocumenting.webtau.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.proxy.ProxyHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Callback;
import org.testingisdocumenting.webtau.server.registry.ContentCaptureRequestWrapper;
import org.testingisdocumenting.webtau.server.registry.ContentCaptureResponseWrapper;
import org.testingisdocumenting.webtau.server.registry.WebTauServerHandledRequest;
import org.testingisdocumenting.webtau.server.registry.WebTauServerJournal;
import org.testingisdocumenting.webtau.time.Time;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WebTauProxyHandler extends ProxyHandler {
    private static final String START_TIME_ATTR_KEY = "org.testingisdocumenting.webtau.server.startTime";

    private final WebTauServerJournal journal;
    private final String urlToProxy;

    public WebTauProxyHandler(WebTauServerJournal journal, String urlToProxy) {
        this.journal = journal;
        this.urlToProxy = urlToProxy;
    }

    @Override
    protected HttpURI rewriteHttpURI(Request clientToProxyRequest) {
        return HttpURI.build()
                .uri(urlToProxy)
                .pathQuery(clientToProxyRequest.getHttpURI().getPathQuery()).asImmutable();
    }

    @Override
    public boolean handle(final Request clientToProxyRequest, final org.eclipse.jetty.server.Response proxyToClientResponse, final Callback proxyToClientCallback) {

        final var requestWrapper = new ContentCaptureRequestWrapper(clientToProxyRequest);
        requestWrapper.setAttribute(START_TIME_ATTR_KEY, Time.currentTimeMillis());
        final var responseWrapper = new ContentCaptureResponseWrapper(clientToProxyRequest, proxyToClientResponse);

        return super.handle(requestWrapper, responseWrapper, new Callback() {
            @Override
            public void completeWith(CompletableFuture<?> completable) {
                Callback.super.completeWith(completable);
            }

            @Override
            public void succeeded() {
                completed();
                Callback.super.succeeded();
            }

            @Override
            public void failed(Throwable x) {
                completed();
                Callback.super.failed(x);
            }

            private void completed()
            {
                final var handledRequest = new WebTauServerHandledRequest(clientToProxyRequest, proxyToClientResponse,
                        (Long) requestWrapper.getAttribute(START_TIME_ATTR_KEY),
                        Time.currentTimeMillis(),
                        requestWrapper.getCaptureAsString(),
                        responseWrapper.getCaptureAsString());
                journal.registerCall(handledRequest);
            }
        });
    }
}
