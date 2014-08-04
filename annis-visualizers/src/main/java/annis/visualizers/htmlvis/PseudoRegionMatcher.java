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

package annis.visualizers.htmlvis;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

/**
 *
 * @author Amir Zeldes
 */
public class PseudoRegionMatcher implements SpanMatcher{

    enum PseudoRegion {
        BEGIN,
        END;    
    }

    final private PseudoRegion psdRegion;
    
    @Override
    public String matchedAnnotation(SNode node) {
   
        return null;
        
    }
    
    public PseudoRegionMatcher(PseudoRegion psdRegion){

        this.psdRegion = psdRegion;
    }

    public PseudoRegion getPsdRegion() {
        return psdRegion;
    }

    
    
}
