/*
 * Copyright 2019 TWO SIGMA OPEN SOURCE, LLC
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

package org.testingisdocumenting.webtau.featuretesting

import org.eclipse.jetty.server.Request
import org.testingisdocumenting.webtau.http.testserver.TestServerResponse

class TestServerHtmlResponse implements TestServerResponse {
    private String response

    TestServerHtmlResponse(String response) {
        this.response = response
    }

    @Override
    byte[] responseBody(Request request) {
        return response.getBytes()
    }

    @Override
    String responseType(Request request) {
        return "text/html"
    }
}
