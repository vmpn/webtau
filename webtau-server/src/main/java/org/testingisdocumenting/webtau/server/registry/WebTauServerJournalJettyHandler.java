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

package org.testingisdocumenting.webtau.server.registry;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
import org.testingisdocumenting.webtau.time.Time;

import java.util.EventListener;

public class WebTauServerJournalJettyHandler implements Handler {
    private final WebTauServerJournal journal;
    private final Handler delegate;

    public WebTauServerJournalJettyHandler(WebTauServerJournal journal, Handler delegate) {
        this.journal = journal;
        this.delegate = delegate;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        long startTime = Time.currentTimeMillis();
        ContentCaptureRequestWrapper captureRequestWrapper = new ContentCaptureRequestWrapper(request);
        ContentCaptureResponseWrapper captureResponseWrapper = new ContentCaptureResponseWrapper(captureRequestWrapper, response);

        try {
            final var result = delegate.handle(captureRequestWrapper, captureResponseWrapper, callback);
            long endTime = Time.currentTimeMillis();

            WebTauServerHandledRequest handledRequest = new WebTauServerHandledRequest(request, response,
                    startTime, endTime,
                    captureRequestWrapper.getCaptureAsString(),
                    captureResponseWrapper.getCaptureAsString());
            journal.registerCall(handledRequest);
            return result;
        } finally {
            captureResponseWrapper.close();
        }
    }

    @Override
    public void setServer(Server server) {
        delegate.setServer(server);
    }

    @Override
    public Server getServer() {
        return delegate.getServer();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public void start() throws Exception {
        delegate.start();
    }

    @Override
    public void stop() throws Exception {
        delegate.stop();
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }

    @Override
    public boolean isStarting() {
        return delegate.isStarting();
    }

    @Override
    public boolean isStopping() {
        return delegate.isStopping();
    }

    @Override
    public boolean isStopped() {
        return delegate.isStopped();
    }

    @Override
    public boolean isFailed() {
        return delegate.isFailed();
    }

    @Override
    public boolean addEventListener(EventListener eventListener) {
        return delegate.addEventListener(eventListener);
    }

    @Override
    public boolean removeEventListener(EventListener eventListener) {
        return delegate.removeEventListener(eventListener);
    }
}
