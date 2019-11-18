/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import annis.CommonHelper;
import annis.service.objects.QueryLanguage;

/**
 * A POJO representing a query.
 * 
 * This objects holds all relevant information about the state of the UI related
 * to querying, e.g. the AQL, the search options and the type of the query.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class Query implements Serializable, Cloneable {
    private String query;
    private Set<String> corpora;
    private QueryLanguage queryLanguage = QueryLanguage.AQL;

    public Query() {
        corpora = new HashSet<>();
    }

    public Query(Query orig) {
        this.query = orig.getQuery();
        this.corpora = orig.getCorpora();
        this.queryLanguage = orig.getQueryLanguage();
    }

    public Query(String query, QueryLanguage queryLanguage, Set<String> corpora) {
        this.query = query == null ? "" : query;
        this.corpora = corpora == null ? new LinkedHashSet<String>() : corpora;
        this.queryLanguage = queryLanguage;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
    }

    public Set<String> getCorpora() {
        return corpora;
    }

    public void setCorpora(Set<String> corpora) {
        this.corpora = corpora == null ? new LinkedHashSet<String>() : corpora;
    }

    public QueryLanguage getQueryLanguage() {
        return queryLanguage;
    }

    public void setQueryLanguage(QueryLanguage queryLanguage) {
        Preconditions.checkNotNull(queryLanguage, "The query language of a paged result query must never be null.");
        this.queryLanguage = queryLanguage;
    }

    public Map<String, String> getCitationFragmentArguments() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("_q", getQuery());
        result.put("ql", getQueryLanguage().name().toLowerCase());
        result.put("_c", StringUtils.join(getCorpora(), ","));
        return result;
    }

    public String toCitationFragment() {
        Map<String, String> result = getCitationFragmentArguments();
        List<String> fragmentParts = new LinkedList<String>();
        for (Map.Entry<String, String> e : result.entrySet()) {
            String value;
            // every name that starts with "_" is base64 encoded
            if (e.getKey().startsWith("_")) {
                value = CommonHelper.encodeBase64URL(e.getValue());
            } else {
                try {
                    value = URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ex) {
                    // should not happen, from Java 10 onward we can use
                    // URLDecoder.decode(e.getValue(), StandardCharsets.UTF_8) directly
                    value = "";
                }
            }
            fragmentParts.add(e.getKey() + "=" + value);
        }

        return StringUtils.join(fragmentParts, "&");
    }

    @Override
    public int hashCode() {
        return Objects.hash(corpora, query, queryLanguage);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Query other = (Query) obj;

        return Objects.equals(this.query, other.query) && Objects.equals(this.corpora, other.corpora)
                && Objects.equals(this.queryLanguage, other.queryLanguage);
    }

    @Override
    public Query clone() throws CloneNotSupportedException {
        return (Query) super.clone();
    }

}
