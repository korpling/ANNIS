/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.visualizers.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.corpus_tools.annis.visualizers.component.RawTextVisualizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class RawTextVisualizerTest {

    private RawTextVisualizer vis;

    @BeforeEach
    public void setup() {
        vis = new RawTextVisualizer();
    }

    @Test
    public void testIsUsingRawText() {
        assertTrue(

                vis.isUsingRawText(), vis.getShortName() + " must use the raw text");
    }

    @Test
    public void testIsUsingText() {
        assertFalse(vis.isUsingText(), vis.getShortName()
                + " never uses the text from a salt project, for a dramatic performance increase");
    }


    /**
     * Test of hasOnlyWhiteSpace method, of class RawTextVisualizer.
     */
    @Test
    public void testHasOnlyWhiteSpace() {
        assertTrue(vis.hasOnlyWhiteSpace("     "));
        assertFalse(vis.hasOnlyWhiteSpace(" x "));
        assertFalse(vis.hasOnlyWhiteSpace("x "));
        assertFalse(vis.hasOnlyWhiteSpace(" x"));
    }

}
