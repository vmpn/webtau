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

import org.eclipse.jetty.server.Request;

public class WebTauServerOverrideFake implements WebTauServerOverride {
    private final String method;
    private final String uri;
    private final WebTauServerResponse fakeResponse;

    public WebTauServerOverrideFake(String method, String uri, WebTauServerResponse fakeResponse) {
        this.method = method.toUpperCase();
        this.uri = uri;
        this.fakeResponse = fakeResponse;
    }

    @Override
    public boolean matchesUri(String method, String uri) {
        return this.method.equals(method.toUpperCase()) &&
                this.uri.equals(uri);
    }

    @Override
    public String overrideId() {
        return method + "-" + uri;
    }

    @Override
    public WebTauServerResponse response(Request request) {
        return fakeResponse;
    }
}
