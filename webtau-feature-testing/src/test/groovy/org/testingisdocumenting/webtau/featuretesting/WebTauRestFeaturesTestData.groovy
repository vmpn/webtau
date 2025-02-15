/*
 * Copyright 2020 webtau maintainers
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
import org.testingisdocumenting.webtau.http.testserver.FixedResponsesHandler
import org.testingisdocumenting.webtau.http.testserver.TestServer
import org.testingisdocumenting.webtau.http.testserver.TestServerJsonResponse
import org.testingisdocumenting.webtau.http.testserver.TestServerRedirectResponse
import org.testingisdocumenting.webtau.http.testserver.TestServerResponseFullEcho
import org.testingisdocumenting.webtau.http.testserver.TestServerResponse
import org.testingisdocumenting.webtau.http.testserver.TestServerTextResponse
import org.testingisdocumenting.webtau.utils.JsonUtils
import org.testingisdocumenting.webtau.utils.ResourceUtils

import javax.servlet.ServletException

class WebTauRestFeaturesTestData {
    static void registerEndPoints(TestServer testServer, FixedResponsesHandler handler) {
        def temperature = [temperature: 88]
        handler.registerGet("/weather", json(temperature))
        handler.registerGet("/statement", new BalancePerPersonaResponse())
        handler.registerGet("/redirect", new TestServerRedirectResponse(HttpURLConnection.HTTP_MOVED_TEMP,
                testServer, "/weather"))
        handler.registerGet("/city/London", json([time: "2018-11-27 13:05:00", weather: temperature]))
        handler.registerGet("/city/NewYork", new TestServerJsonResponse(ResourceUtils.textContent("new-york-weather.json")))
        handler.registerPost("/employee", json([id: "id-generated-2"], 201))
        handler.registerGet("/employee/id-generated-2", json([firstName: "FN", lastName: "LN"]))
        handler.registerGet("/text-message", new TestServerTextResponse("hello world"))
        handler.registerGet("/resource/generated-id-123", json([message: "hello"]))
        handler.registerPut("/full-echo", new TestServerResponseFullEcho(200))
        handler.registerPut("/invalid-json-response", new TestServerJsonResponse(
                '{"key": "value', 200))
    }

    private static TestServerResponse json(Map response, statusCode = 200) {
        return new TestServerJsonResponse(JsonUtils.serialize(response), statusCode)
    }

    static class BalancePerPersonaResponse implements TestServerResponse {
        @Override
        byte[] responseBody(Request request) throws IOException, ServletException {
            def authz = request.getHeaders().get("Authorization")
            def balance = authz.contains('alice') ? 150 : 30

            def response = JsonUtils.serialize([balance: balance])
            return response.getBytes()
        }

        @Override
        String responseType(Request request) {
            return "application/json"
        }
    }
}
