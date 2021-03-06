/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.frequency;

import java.io.Serializable;
import java.util.Objects;
import org.corpus_tools.annis.gui.objects.FrequencyTableEntry;
import org.corpus_tools.annis.gui.objects.FrequencyTableEntryType;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class UserGeneratedFrequencyEntry implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2233620631396727690L;
    private String nr;
    private String annotation;
    private String comment = "manually created";

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
        final UserGeneratedFrequencyEntry other = (UserGeneratedFrequencyEntry) obj;
        if (!Objects.equals(this.nr, other.nr)) {
            return false;
        }
        if (!Objects.equals(this.annotation, other.annotation)) {
            return false;
        }
        return true;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getComment() {
        return comment;
    }

    public String getNr() {
        return nr;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.nr);
        hash = 53 * hash + Objects.hashCode(this.annotation);
        return hash;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    /**
     * Converts this object to a proper definition.
     * 
     * @return
     */
    public FrequencyTableEntry toFrequencyTableEntry() {
        FrequencyTableEntry result = new FrequencyTableEntry();

        result.setReferencedNode(nr);

        if ("tok".equals(annotation)) {
            result.setType(FrequencyTableEntryType.span);
        } else {
            result.setType(FrequencyTableEntryType.annotation);
            result.setKey(annotation);
        }

        return result;
    }

}
