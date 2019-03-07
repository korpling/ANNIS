/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import annis.CommonHelper;
import annis.service.objects.OrderType;

/**
 * The query state of the actual displayed result query.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class DisplayedResultQuery extends PagedResultQuery {
    private Set<Long> selectedMatches = new TreeSet<>();
    private String baseText;

    public Set<Long> getSelectedMatches() {
        return selectedMatches;
    }

    public void setSelectedMatches(Set<Long> selectedMatches) {
        Preconditions.checkNotNull(selectedMatches,
                "The selected matches set of a paged result query must never be null (but can be empty)");
        this.selectedMatches = selectedMatches;
    }

    public String getBaseText() {
        return baseText;
    }

    public void setBaseText(String baseText) {
        this.baseText = baseText;
    }

    public String toCitationFragment() throws UnsupportedEncodingException {
        Map<String, String> result = getCitationFragmentArguments();
        List<String> fragmentParts = new LinkedList<String>();
        for (Map.Entry<String, String> e : result.entrySet()) {
            String value;
            // every name that starts with "_" is base64 encoded
            if (e.getKey().startsWith("_")) {
                value = new String(Base64.decodeBase64(e.getValue()), "UTF-8");
            } else {
                value = URLDecoder.decode(e.getValue(), "UTF-8");
            }
            fragmentParts.add(e.getKey() + "=" + value);
        }

        return StringUtils.join(fragmentParts, "&");
    }

    public Map<String, String> getCitationFragmentArguments() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("_q", getQuery());
        result.put("ql", getQueryLanguage().name().toLowerCase());
        result.put("_c", StringUtils.join(getCorpora(), ","));
        result.put("cl", "" + getLeftContext());
        result.put("cr", "" + getRightContext());
        result.put("s", "" + getOffset());
        result.put("l", "" + getLimit());
        if (getSegmentation() != null) {
            result.put("_seg", CommonHelper.encodeBase64URL(getSegmentation()));
        }
        // only output "bt" if it is not the same as the context segmentation
        if (!Objects.equals(getBaseText(), getSegmentation())) {
            result.put("_bt", (getBaseText() == null ? "" : getBaseText()));
        }
        if (getOrder() != OrderType.ascending && getOrder() != null) {
            result.put("o", getOrder().toString());
        }
        if (getSelectedMatches() != null && !getSelectedMatches().isEmpty()) {
            result.put("m", Joiner.on(',').join(getSelectedMatches()));
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCorpora(), getQuery(), getLeftContext(), getRightContext(), getSegmentation(),
                getLimit(), getOffset(), getOrder(), getBaseText(), getSelectedMatches());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DisplayedResultQuery other = (DisplayedResultQuery) obj;
        return Objects.equals(getQuery(), other.getQuery()) && Objects.equals(getCorpora(), other.getCorpora())
                && Objects.equals(getLeftContext(), other.getLeftContext())
                && Objects.equals(getRightContext(), other.getRightContext())
                && Objects.equals(getSegmentation(), other.getSegmentation())
                && Objects.equals(getLimit(), other.getLimit()) && Objects.equals(getOffset(), other.getOffset())
                && Objects.equals(getOrder(), other.getOrder()) && Objects.equals(getBaseText(), other.getBaseText())
                && Objects.equals(getSelectedMatches(), other.getSelectedMatches());
    }
}
