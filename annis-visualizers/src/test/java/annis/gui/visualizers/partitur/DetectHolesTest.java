/*
 * Copyright 2012 SFB 632.
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
package annis.gui.visualizers.partitur;

import annis.model.AnnisNode;
import annis.visualizers.iframe.partitur.DetectHoles;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author benjamin
 */
public class DetectHolesTest
{

  List<AnnisNode> token;
  AnnisNode node1 = new AnnisNode(1, 0, 0, 0, 0, "annis", "test",
    50, "test8", 19, 22);
  AnnisNode node2 = new AnnisNode(2, 0, 0, 0, 0, "annis", "test",
    50, "test9", 40, 45);
  AnnisNode node3 = new AnnisNode(3, 0, 0, 0, 0, "annis", "test",
    51, "test10", 12, 54);
  AnnisNode node4 = new AnnisNode(4, 0, 0, 0, 0, "annis", "test",
    52, "test11", 39, 42);

  private void initializeTokens()
  {
    token = new ArrayList<AnnisNode>();
    token.add(new AnnisNode(5, 0, 0, 0, 0, "annis", "test",
      20, "test1", 20, 20));
    token.add(new AnnisNode(6, 0, 0, 0, 0, "annis", "test",
      21, "test2", 21, 21));
    token.add(new AnnisNode(7, 0, 0, 0, 0, "annis", "test",
      22, "test3", 22, 22));
    token.add(new AnnisNode(8, 0, 0, 0, 0, "annis", "test",
      40, "test4", 40, 40));
    token.add(new AnnisNode(9, 0, 0, 0, 0, "annis", "test",
      41, "test5", 41, 41));
    token.add(new AnnisNode(10, 0, 0, 0, 0, "annis", "test",
      42, "test6", 42, 42));
    token.add(new AnnisNode(11, 0, 0, 0, 0, "annis", "test",
      50, "test7", 50, 50));
  }

  public DetectHolesTest()
  {
    initializeTokens();
  }

  /**
   * Test of getLeftBorder method, of class DetectHoles.
   */
  @Test
  public void testGetLeftBorder()
  {
    System.out.println("getLeftBorder");
    DetectHoles instance = new DetectHoles(token);
    AnnisNode result = instance.getLeftBorder(node1);
    assertEquals(token.get(0), result);
    result = instance.getLeftBorder(node2);
    assertEquals(token.get(3), result);
    result = instance.getLeftBorder(node3);
    assertEquals(token.get(0), result);
    result = instance.getRightBorder(node4);
    assertEquals(token.get(5), result);
  }

  /**
   * Test of getRightBorder method, of class DetectHoles.
   */
  @Test
  public void testGetRightBorder()
  {
    System.out.println("getRightBorder");
    AnnisNode result = null;
    DetectHoles instance = new DetectHoles(token);
    result = instance.getRightBorder(node1);
    assertEquals(token.get(2), result);
    result = instance.getRightBorder(node2);
    assertEquals(token.get(5), result);
    result = instance.getRightBorder(node3);
    assertEquals(token.get(6), result);
    result = instance.getRightBorder(node4);
    assertEquals(token.get(5), result);
  }
}
