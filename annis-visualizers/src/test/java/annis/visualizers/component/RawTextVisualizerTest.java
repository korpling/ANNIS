/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.component;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RawTextVisualizerTest {

    private RawTextVisualizer vis;

    @Before
    public void setup() {
        vis = new RawTextVisualizer();
    }

    @Test
    public void testIsUsingRawText() {
        assertTrue(
                vis.getShortName() + " must use the raw text",
                vis.isUsingRawText());
    }

    @Test
    public void testIsUsingText() {
        assertFalse(
                vis.getShortName() + " never uses the text from a salt project, for a dramatic performance increase",
                vis.isUsingText());
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
