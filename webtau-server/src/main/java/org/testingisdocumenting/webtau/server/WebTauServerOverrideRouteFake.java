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

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.server.route.RouteParamsParser;

import java.util.function.Function;

/**
 * server override
 */
public class WebTauServerOverrideRouteFake implements WebTauServerOverride {
    private final String method;
    private final RouteParamsParser routeParamsParser;
    private final Function<WebTauServerRequest, WebTauServerResponse> responseFunc;

    public WebTauServerOverrideRouteFake(String method, String urlWithParams,
                                         Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        routeParamsParser = new RouteParamsParser(urlWithParams);
        this.method = method.toUpperCase();
        this.responseFunc = responseFunc;
    }

    @Override
    public boolean matchesUri(String method, String uri) {
        return this.method.equals(method.toUpperCase()) &&
                this.routeParamsParser.matches(uri);
    }

    @Override
    public String overrideId() {
        return method + "-" + routeParamsParser.getPathDefinition();
    }

    @Override
    public WebTauServerResponse response(Request request) {
        WebTauServerResponse serverResponse = responseFunc.apply(WebTauServerRequest.create(routeParamsParser,
                request));
        return serverResponse.newResponseWithUpdatedStatusCodeIfRequired(request.getMethod());
    }

    @Override
    public String toString() {
        return "WebTauServerOverrideRouteFake{" +
                "method='" + method + '\'' +
                ", route=" + routeParamsParser.getPathDefinition() +
                '}';
    }
}
