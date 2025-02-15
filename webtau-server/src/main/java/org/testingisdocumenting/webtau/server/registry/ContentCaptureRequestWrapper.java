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

import jakarta.servlet.ServletInputStream;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentCaptureRequestWrapper extends Request.Wrapper {
    private final List<CapturingChunk> capturingChunks = new LinkedList<>();

    private ServletInputStream input;

    public ContentCaptureRequestWrapper(Request request) {
        super(request);
    }

    @Override
    public void demand(Runnable demandCallback) {
        super.demand(demandCallback);
    }

    @Override
    public Content.Chunk read() {
        final var capturingChunk = new CapturingChunk(super.read());
        capturingChunks.add(capturingChunk);
        return capturingChunk;
    }

    public byte[] getCaptureAsBytes() throws IOException {
        final var totalDataSize = capturingChunks.stream().map(CapturingChunk::getRead).filter(Objects::nonNull)
                .mapToInt(read -> read.length).sum();
        final var data = new byte[totalDataSize];

        final var index = new AtomicInteger(0);
        capturingChunks.stream()
                .map(CapturingChunk::getRead)
                .filter(Objects::nonNull)
                .forEach(bytes ->
                {
                    System.arraycopy(bytes, 0, data, index.get(), bytes.length);
                    index.addAndGet(bytes.length);
                });

        return data;
    }

    public String getCaptureAsString() {
        try {
            String charsetName = getWrapped().getHeaders().get(HttpHeader.CONTENT_ENCODING);

            return charsetName != null ?
                    new String(getCaptureAsBytes(), charsetName):
                    new String(getCaptureAsBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class CapturingChunk implements Content.Chunk {

        private byte[] read = null;
        private ByteBuffer readBuffer = null;
        private final Content.Chunk delegate;

        private CapturingChunk(Content.Chunk delegate) {
            this.delegate = delegate;
        }

        @Override
        public ByteBuffer getByteBuffer() {
            if(read == null) {
                final var byteBuffer = delegate.getByteBuffer();
                if (!byteBuffer.hasRemaining()) {
                    return byteBuffer;
                }

                read = new byte[byteBuffer.remaining()];
                byteBuffer.get(read);
                readBuffer = ByteBuffer.wrap(read);
            }
            return readBuffer;
        }

        public byte[] getRead() {
            return read;
        }

        @Override
        public boolean isLast() {
            return delegate.isLast();
        }
    }
}
