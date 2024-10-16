/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.reporter.file.formatter;

import io.gravitee.am.reporter.file.formatter.csv.CsvFormatter;
import io.gravitee.am.reporter.file.formatter.elasticsearch.ElasticsearchFormatter;
import io.gravitee.am.reporter.file.formatter.json.JsonFormatter;
import io.gravitee.am.reporter.file.formatter.msgpack.MsgPackFormatter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public final class FormatterFactory {

    public static Formatter getFormatter(Type type) {
        switch (type) {
            case CSV:
                return new CsvFormatter();
            case MESSAGE_PACK:
                return new MsgPackFormatter();
            case JSON:
                return new JsonFormatter();
            case ELASTICSEARCH:
                return new ElasticsearchFormatter();
        }

        return new JsonFormatter();
    }
}
