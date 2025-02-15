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

package org.testingisdocumenting.webtau.server.registry;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentCaptureResponseWrapper extends Response.Wrapper {

    private final List<byte[]> captures = new LinkedList<>();
    private final String outputEncoding;

    public ContentCaptureResponseWrapper(Request request, Response response) {
        super(request, response);
        this.outputEncoding = response.getHeaders().get(HttpHeader.CONTENT_ENCODING);
    }

    @Override
    public void write(boolean last, ByteBuffer byteBuffer, Callback callback) {
        if(!byteBuffer.hasRemaining()) {
            super.write(last, byteBuffer, callback);
        }

        final var data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        captures.add(data);
        super.write(last, ByteBuffer.wrap(data), callback);
    }

    public byte[] getCaptureAsBytes() throws IOException {
        final var totalDataSize = captures.stream().mapToInt(read -> read.length).sum();
        final var data = new byte[totalDataSize];

        final var index = new AtomicInteger(0);
        captures.forEach(bytes ->
                {
                    System.arraycopy(bytes, 0, data, index.get(), bytes.length);
                    index.addAndGet(bytes.length);
                });

        return data;
    }

    public void close() {
    }

    public String getCaptureAsString() {
        try {
            return outputEncoding != null ?
                    new String(getCaptureAsBytes(), outputEncoding):
                    new String(getCaptureAsBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
