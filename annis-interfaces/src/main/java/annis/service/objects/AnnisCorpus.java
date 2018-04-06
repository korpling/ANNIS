/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.objects;

import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnnisCorpus implements Serializable, Comparable<AnnisCorpus> {

    private String name;
    private int documentCount, tokenCount;
    private String sourcePath;

    public AnnisCorpus(String name, int documentCount, int tokenCount) {
        this.documentCount = documentCount;
        this.tokenCount = tokenCount;
        this.name = name;
    }

    public AnnisCorpus() {
        this(null, 0, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public Map<String, Object> asTableRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", getName());
        row.put("tokens", getTokenCount());
        row.put("documents", getDocumentCount());
        row.put("source path", getSourcePath());
        return row;
    }

    @Override
    public String toString() {
        return String.valueOf("corpus " + name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnisCorpus other = (AnnisCorpus) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(AnnisCorpus o) {
        if (o == null) {
            return -1;
        } else {
            return ComparisonChain.start().compare(this.name, o.name).result();
        }
    }
}
