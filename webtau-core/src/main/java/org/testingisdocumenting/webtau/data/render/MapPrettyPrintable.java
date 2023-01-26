/*
 * Copyright 2023 webtau maintainers
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

package org.testingisdocumenting.webtau.data.render;

import org.testingisdocumenting.webtau.data.ValuePath;

import java.util.Map;
import java.util.Objects;

import static org.testingisdocumenting.webtau.data.render.PrettyPrinter.*;

public class MapPrettyPrintable implements PrettyPrintable {
    private final Map<?, ?> map;

    public MapPrettyPrintable(Map<?, ?> map) {
        this.map = map;
    }

    @Override
    public void prettyPrint(PrettyPrinter printer) {
        prettyPrint(printer, ValuePath.UNDEFINED);
    }

    @Override
    public void prettyPrint(PrettyPrinter printer, ValuePath root) {
        if (map.isEmpty()) {
            printEmptyMap(printer);
        } else {
            printNonEmptyMap(printer, root);
        }
    }

    private void printEmptyMap(PrettyPrinter printer) {
        printer.printDelimiter("{");
        printer.printDelimiter("}");
    }

    public void printNonEmptyMap(PrettyPrinter printer, ValuePath path) {
        printer.printDelimiter("{");
        printer.printLine();
        printer.increaseIndentation();

        int idx = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            printKey(printer,key);
            printer.printDelimiter(": ");
            printer.printObject(path.property(key.toString()), value);

            boolean isLast = idx == map.size() - 1;
            if (!isLast) {
                printer.printDelimiter(",");
                printer.printLine();
            }

            idx++;
        }

        printer.printLine();
        printer.decreaseIndentation();
        printer.printDelimiter("}");
    }

    private void printKey(PrettyPrinter printer, Object key) {
        String renderedKey = key instanceof CharSequence ?
                "\"" + key + "\"" :
                Objects.toString(key);
        printer.print(KEY_COLOR, renderedKey);
    }
}