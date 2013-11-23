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
package annis.libgui;

import com.google.common.collect.MapMaker;
import com.vaadin.ui.UI;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A helper class to manager different polling needs from different background
 * threads. This effictevly sets {@link UI#setPollInterval(int) } on the current
 * {@link UI}.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PollControl
{
  private static final ConcurrentMap<Long, Integer> threadID2Time = new MapMaker().
    makeMap();

  private static final Lock calcLock = new ReentrantLock();
  
  public static final int DEFAULT_TIME = 15000;

  private static void setTime(UI ui, int newPollingTime)
  {
    threadID2Time.put(Thread.currentThread().getId(), newPollingTime);
    calculateAndSetPollingTime(ui);
  }

  private static void unsetTime(UI ui)
  {
    if(threadID2Time.remove(Thread.currentThread().getId()) != null)
    {
      calculateAndSetPollingTime(ui);
    }
  }

  /**
   * Use the minimal time of all registered time in to determine how frequent
   * polling should be.
   */
  private static void calculateAndSetPollingTime(UI ui)
  {
    calcLock.lock();
    try
    {
      
      if(ui != null)
      {
        
        // get the minimal non-negative time
        int min = DEFAULT_TIME;
        for (int time : threadID2Time.values())
        {
          if(time >= 0 && time < min)
          {
            min = time;
          }
        }
        ui.setPollInterval(min);

      }
    }
    finally
    {
      calcLock.unlock();
    }
  }
  
  /**
   * Wraps the {@link UI#access(java.lang.Runnable) } method but includes 
   * a parameter to control a polling time which is active during the execution 
   * of the {@link Runnable}.
   * @param runnable 
   * @param pollTime 
   */
  public static void access(final int pollTime, final Runnable runnable)
  {
    access(pollTime, null, runnable);
  }
  
  /**
   * Wraps the {@link UI#access(java.lang.Runnable) } method but includes 
   * a parameter to control a polling time which is active during the execution 
   * of the {@link Runnable}.
   * @param runnable 
   * @param ui The {@link UI} to access or null of the current one should be used.
   * @param pollTime 
   */
  public static void access(final int pollTime, UI ui, final Runnable runnable)
  {
    if(ui == null)
    {
      ui = UI.getCurrent();
    }
    
    if(runnable != null)
    {
      final UI finalUI = ui;
      
      ui.access(new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            setTime(finalUI, pollTime);
            runnable.run();
          }
          finally
          {
            unsetTime(finalUI);
          }
        }
      });
    }
  }

  
}
