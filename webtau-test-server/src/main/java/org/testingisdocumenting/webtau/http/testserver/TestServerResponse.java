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

package org.testingisdocumenting.webtau.http.testserver;

import jakarta.servlet.ServletException;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public interface TestServerResponse {
    byte[] responseBody(Request request) throws IOException, ServletException;
    String responseType(Request request);

    default Map<String, String> responseHeader(Request request) {
        return Collections.emptyMap();
    }

    default int responseStatusCode() {
        return 200;
    }
}
