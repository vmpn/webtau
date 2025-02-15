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

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.testingisdocumenting.webtau.server.route.RouteParams;
import org.testingisdocumenting.webtau.server.route.RouteParamsParser;
import org.testingisdocumenting.webtau.utils.JsonUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebTauServerRequest {
    private final String method;
    private final String path;
    private final String query;
    private final Map<String, List<String>> parsedQuery;
    private final String pathWithQuery;
    private final RouteParams routeParams;
    private final String contentType;
    private final String textContent;
    private final byte[] bytesContent;
    private final Map<String, CharSequence> header;
    private final String fullUrl;

    public static WebTauServerRequest create(RouteParamsParser routeParamsParser, Request request) {
        try {
            String queryString = request.getHttpURI().getQuery();
            return new WebTauServerRequest(routeParamsParser.parse(request.getHttpURI().getPathQuery()),
                    request.getMethod(),
                    request.getHttpURI().getPath(),
                    queryString,
                    request.getHttpURI().getPath(),
                    request.getHeaders().get(HttpHeader.CONTENT_TYPE),
                    IOUtils.toByteArray(Content.Source.asInputStream(request)),
                    headerFromRequest(request));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public WebTauServerRequest(RouteParams routeParams,
                               String method,
                               String path,
                               String query,
                               String fullUrlWithoutQuery,
                               String contentType, byte[] bytesContent,
                               Map<String, CharSequence> header) {
        this.routeParams = routeParams;
        this.method = method;
        this.path = path;
        this.query = query == null ? "" : query;
        this.parsedQuery = QueryParamsParser.parse(this.query);
        this.pathWithQuery = path + (this.query.isEmpty() ? "" : "?" + this.query);
        this.fullUrl = fullUrlWithoutQuery + (this.query.isEmpty() ? "" : "?" + this.query);
        this.contentType = contentType;
        this.bytesContent = bytesContent;
        this.textContent = new String(bytesContent);
        this.header = header;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    /**
     * @deprecated use {@link #getPath()}
     * @return request path
     */
    @Deprecated
    public String getUri() {
        return path;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getPathWithQuery() {
        return pathWithQuery;
    }

    public String getMethod() {
        return method;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContentAsBytes() {
        return bytesContent;
    }

    public String getContentAsText() {
        return textContent;
    }

    /**
     * converts json content into map
     *
     * @return json content as map
     */
    public Map<String, ?> getContentAsMap() {
        return JsonUtils.deserializeAsMap(getContentAsText());
    }

    /**
     * converts json content into list
     *
     * @return json content as list
     */
    public List<?> getContentAsList() {
        return JsonUtils.deserializeAsList(getContentAsText());
    }

    public Map<String, CharSequence> getHeader() {
        return header;
    }

    /**
     * get value of a route param by name
     *
     * @param routerParamName param name
     * @return router param value
     */
    public String param(String routerParamName) {
        return routeParams.get(routerParamName);
    }

    public RouteParams getRouteParams() {
        return routeParams;
    }

    /**
     * get value of a query param by name
     *
     * @param name param name
     * @return query param value
     */
    public String queryParam(String name) {
        List<String> values = parsedQuery.getOrDefault(name, Collections.emptyList());
        return values.isEmpty() ? null : values.get(values.size() - 1);
    }

    /**
     * get values of a query param by name
     *
     * @param name param name
     * @return query param value
     */
    public List<String> queryParamList(String name) {
        List<String> values = parsedQuery.getOrDefault(name, Collections.emptyList());
        return values.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(values);
    }

    private static Map<String, CharSequence> headerFromRequest(Request request) {
        Map<String, CharSequence> header = new LinkedHashMap<>();
        request.getHeaders().forEach(h -> header.put(h.getName(), h.getValue()));
        return header;
    }
}
