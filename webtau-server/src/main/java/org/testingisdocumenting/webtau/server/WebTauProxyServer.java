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

import org.eclipse.jetty.server.Handler;

import java.util.Collections;
import java.util.Map;

public class WebTauProxyServer extends WebTauJettyServer {
    private final String urlToProxy;

    public WebTauProxyServer(String id, String urlToProxy, int passedPort) {
        super(id, passedPort);
        this.urlToProxy = urlToProxy;
    }

    public String getUrlToProxy() {
        return urlToProxy;
    }

    @Override
    public String getType() {
        return "proxy";
    }

    @Override
    protected Map<String, Object> provideStepInput() {
        return Collections.singletonMap("url to proxy", urlToProxy);
    }

    @Override
    protected void validateParams() {
    }

    @Override
    public boolean autoAddToJournal() {
        return false;
    }

    @Override
    protected Handler createJettyHandler() {
//        proxyServletHolder.setInitParameter("maxThreads", String.valueOf(WebTauServersConfig.getProxyMaxThreads()));
        return new WebTauProxyHandler(getJournal(), urlToProxy);
    }
}
