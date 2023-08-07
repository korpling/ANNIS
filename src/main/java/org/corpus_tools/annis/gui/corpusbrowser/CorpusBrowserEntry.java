/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.corpusbrowser;

import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.corpus_tools.annis.gui.query_references.CitationProvider;

/**
 *
 * @author thomas
 */
public class CorpusBrowserEntry implements CitationProvider, Serializable, Comparable<CorpusBrowserEntry> {

    private static final long serialVersionUID = 3609486046248403457L;
    private String name;
    private String example;
    private String corpus;

    @Override
    public int compareTo(CorpusBrowserEntry o) {
        return ComparisonChain.start().compare(this.getName(), o.getName()).compare(this.getCorpus(), o.getCorpus())
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CorpusBrowserEntry other = (CorpusBrowserEntry) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.corpus != other.corpus && (this.corpus == null || !this.corpus.equals(other.corpus))) {
            return false;
        }
        return true;
    }

    @Override
    public Set<String> getCorpora() {
        Set<String> result = new HashSet<>();
        result.add(corpus);
        return result;
    }

    public String getCorpus() {
        return corpus;
    }

    public String getExample() {
        return example;
    }

    @Override
    public int getLeftContext() {
        return 5;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getQuery() {
        return example;
    }

    @Override
    public int getRightContext() {
        return 5;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 13 * hash + (this.corpus != null ? this.corpus.hashCode() : 0);
        return hash;
    }

    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setName(String name) {
        this.name = name;
    }

}
