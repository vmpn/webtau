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

package org.testingisdocumenting.webtau.server.route;

import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.server.WebTauServerOverride;
import org.testingisdocumenting.webtau.server.WebTauServerOverrideList;
import org.testingisdocumenting.webtau.server.WebTauServerOverrideRouteFake;
import org.testingisdocumenting.webtau.server.WebTauServerRequest;
import org.testingisdocumenting.webtau.server.WebTauServerResponse;

import java.util.function.Function;

public class WebTauRouter implements WebTauServerOverride {
    private final WebTauServerOverrideList overrideList;

    public WebTauRouter(String id) {
        this.overrideList = new WebTauServerOverrideList(id);
    }

    @Override
    public boolean matchesUri(String method, String uri) {
        return overrideList.matchesUri(method, uri);
    }

    @Override
    public String overrideId() {
        return overrideList.overrideId();
    }

    @Override
    public WebTauServerResponse response(Request request) {
        return overrideList.response(request);
    }

    @Override
    public String toString() {
        return overrideList.toString();
    }

    public WebTauRouter get(String urlWithParams, Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        return register("GET", urlWithParams, responseFunc);
    }

    public WebTauRouter post(String urlWithParams, Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        return register("POST", urlWithParams, responseFunc);
    }

    public WebTauRouter put(String urlWithParams, Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        return register("PUT", urlWithParams, responseFunc);
    }

    public WebTauRouter delete(String urlWithParams, Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        return register("DELETE", urlWithParams, responseFunc);
    }

    public WebTauRouter patch(String urlWithParams, Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        return register("PATCH", urlWithParams, responseFunc);
    }

    private WebTauRouter register(String method, String urlWithParams,
                                  Function<WebTauServerRequest, WebTauServerResponse> responseFunc) {
        overrideList.addOverride(new WebTauServerOverrideRouteFake(method, urlWithParams, responseFunc));
        return this;
    }
}
