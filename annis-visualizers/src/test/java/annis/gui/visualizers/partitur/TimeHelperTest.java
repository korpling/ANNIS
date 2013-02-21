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

import annis.visualizers.iframe.partitur.TimeHelper;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author benjamin
 */
public class TimeHelperTest
{

  private String time1 = "12.1231-0.2121";
  private String time2 = "6";
  private String time3 = "45-";
  private String time4 = "98.0";
  private String time5 = "12.1211-";
  private String time6 = "-12";
  private String time7 = "-12.212";
  private String time8 = "-0.12";
  private String time9 = "-.543";

  @Test
  public void getStartTime()
  {
    TimeHelper t = new TimeHelper();
    assertTrue(t.getStartTime(time1).equals("12.1231"));
    assertTrue(t.getStartTime(time2).equals("6"));
    assertTrue(t.getStartTime(time3).equals("45"));
    assertTrue(t.getStartTime(time4).equals("98.0"));
    assertTrue(t.getStartTime(time5).equals("12.1211"));
    assertTrue(t.getStartTime(time6).equals("undefined"));
    assertTrue(t.getStartTime(time7).equals("undefined"));
    assertTrue(t.getStartTime(time8).equals("undefined"));
    assertTrue(t.getStartTime(time9).equals("undefined"));
  }

  @Test
  public void getEndTime()
  {
    TimeHelper t = new TimeHelper();
    assertTrue(t.getEndTime(time1).equals("0.2121"));
    assertTrue(t.getEndTime(time2).equals("undefined"));
    assertTrue(t.getEndTime(time3).equals("undefined"));
    assertTrue(t.getEndTime(time4).equals("undefined"));
    assertTrue(t.getEndTime(time5).equals("undefined"));
    assertTrue(t.getEndTime(time6).equals("12"));
    assertTrue(t.getEndTime(time7).equals("12.212"));
    assertTrue(t.getEndTime(time8).equals("0.12"));
    assertTrue(t.getEndTime(time9).equals(".543"));
  }
}
