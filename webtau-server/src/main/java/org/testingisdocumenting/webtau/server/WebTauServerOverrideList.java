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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WebTauServerOverrideList implements WebTauServerOverride {
    private final Map<String, WebTauServerOverride> overrides = new ConcurrentHashMap<>();
    private final String listId;

    public WebTauServerOverrideList(String listId) {
        this.listId = listId;
    }

    public void addOverride(WebTauServerOverride override) {
        String overrideId = override.overrideId();
        WebTauServerOverride existing = overrides.put(overrideId, override);

        if (existing != null) {
            throw new RuntimeException("already found an override for list id: " + listId +
                    ", with override id: " + overrideId + ", existing override: " + existing);
        }
    }

    @Override
    public boolean matchesUri(String method, String uri) {
        return findOverride(method, uri).isPresent();
    }

    @Override
    public String overrideId() {
        return listId;
    }

    @Override
    public WebTauServerResponse response(Request request) {
        Optional<WebTauServerOverride> found = findOverride(request);
        return found.map(override -> override.response(request))
                .orElseThrow(this::shouldNotHappen);
    }

    @Override
    public String toString() {
        return "id:" + overrideId() + "; list:\n" + overrides.values().stream()
                .map(WebTauServerOverride::overrideId).collect(Collectors.joining("\n"));
    }

    private Optional<WebTauServerOverride> findOverride(Request request) {
        return findOverride(request.getMethod(), request.getHttpURI().getPath());
    }

    private Optional<WebTauServerOverride> findOverride(String method, String uri) {
        return overrides.values().stream().filter(override -> override.matchesUri(method, uri)).findFirst();
    }

    private RuntimeException shouldNotHappen() {
        return new IllegalStateException("should not happen");
    }
}
