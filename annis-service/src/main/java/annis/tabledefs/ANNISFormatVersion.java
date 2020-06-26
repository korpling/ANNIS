/*
 * Copyright 2014 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.tabledefs;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import jline.internal.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definies the schemata of different ANNIS import format versions.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public enum ANNISFormatVersion {

    V3_1(".tab"), V3_2(".tab"), V3_3(".annis"), UNKNOWN(".x");

    private static final Logger log = LoggerFactory.getLogger(ANNISFormatVersion.class);

    private final String fileSuffix;

    private ANNISFormatVersion(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getFactsSQL() {
        try (InputStream res = this.getClass().getResourceAsStream("facts_" + name() + ".sql")) {
            if (res != null) {
                return CharStreams.toString(new InputStreamReader(res, StandardCharsets.UTF_8));
            }
        } catch (IOException ex) {
            log.error("Can't read SQL for facts definition", ex);
        }
        return null;
    }

    /**
     * The used file suffix for each single table file.
     * 
     * @return
     */
    public String getFileSuffix() {
        return fileSuffix;
    }
}
