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
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.util.Optional;

public class WebTauServerFakeJettyHandler extends Handler.Abstract {
    private final String serverId;

    public WebTauServerFakeJettyHandler(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        Optional<WebTauServerOverride> optionalOverride = WebTauServerGlobalOverrides.findOverride(serverId,
                request.getMethod(),
                request.getHttpURI().getPath());

        if (!optionalOverride.isPresent()) {
            response.setStatus(404);
            callback.succeeded();
        } else {
            WebTauServerOverride override = optionalOverride.get();
            override.apply(request, response, callback);
        }

        return true;
    }
}
