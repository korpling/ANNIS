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
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to manager different polling needs from different background
 * threads. This effictevly sets {@link UI#setPollInterval(int) } on the current
 * {@link UI}.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PollControl
{
  
  private static final Logger log = LoggerFactory.getLogger(PollControl.class);
  
  public static final int DEFAULT_TIME = 15000;
  private static final ExecutorService exec = Executors.newCachedThreadPool();

  private static TimeMap getId2Time(UI ui)
  {
    if(ui != null && ui.getSession() != null)
    {
      TimeMap result = ui.getSession().getAttribute(TimeMap.class);
      if(result == null)
      {
        result = new TimeMap();
        ui.getSession().setAttribute(TimeMap.class, result);
      }
      return result;
    }
    return new TimeMap();
  }
  
  private static UUID setTime(UI ui, int newPollingTime)
  {
    UUID id = UUID.randomUUID();
    // it's really unpropable, but just in case check if this ID was used before
    while(getId2Time(ui).containsKey(id))
    {
      id = UUID.randomUUID();
    }
    getId2Time(ui).put(id, newPollingTime);
    calculateAndSetPollingTime(ui);
    
    return id;
  }

  private static void unsetTime(UUID id, UI ui)
  {
    if(getId2Time(ui).remove(id) != null)
    {
      calculateAndSetPollingTime(ui);
    }
  }

  /**
   * Use the minimal time of all registered time in to determine how frequent
   * polling should be.
   */
  private static void calculateAndSetPollingTime(final UI ui)
  {
    try
    {
      
      if(ui != null)
      {
        // get the minimal non-negative time
        int min = DEFAULT_TIME;
        LinkedList<Integer> numbers = new LinkedList<Integer>(getId2Time(ui).values());
        for (int time : numbers)
        {
          if(time >= 0 && time < min)
          {
            min = time;
          }
        }        

        ui.setPollInterval(min);
//        ui.getPage().setTitle(min + "ms currently polling");
          
      }
    }
    finally
    {
      
    }
  }
  
  /**
   * Starts a thread for background execution and ensures 
   * a certain polling time which is active during the thread execution 
   * of the {@link Runnable}.
   * 
   * @param runnable 
   * @param ui The {@link UI} to access or null of the current one should be used.
   * @param pollTime polling time in milliseconds
   */
  public static  Future<?> runInBackground(int pollTime, UI ui, 
    final Runnable runnable) 
  {
    return callInBackground(pollTime, 0, ui,Executors.callable(runnable));
  }
  
  /**
   * Starts a thread for background execution and ensures 
   * a certain polling time which is active during the thread execution 
   * of the {@link Runnable}.
   * 
   * @param runnable 
   * @param ui The {@link UI} to access or null of the current one should be used.
   * @param pollTime polling time in milliseconds
   * @param initialWait The initial maximal time in milliseconds to wait for the 
   * job to return. If you don't want to wait set this to <= 0.
   */
  public static  Future<?> runInBackground(int pollTime, int initialWait, UI ui, 
    final Runnable runnable) 
  {
    return callInBackground(pollTime, initialWait, ui, Executors.callable(runnable));
  }
  
  /**
   * Starts a thread for background execution and ensures 
   * a certain polling time which is active during the thread execution 
   * of the {@link Runnable}.
   * 
   * @param callable 
   * @param ui The {@link UI} to access or null of the current one should be used.
   * @param pollTime polling time in milliseconds
   * @return The {@link Callable} that wraps the real one or null if an error occured
   */
  public static <T> Future<T> callInBackground(
    final int pollTime, 
    UI ui, 
    final Callable<T> callable) 
  {
    return callInBackground(pollTime, 0, ui, callable);
  }
  
  /**
   * Starts a thread for background execution and ensures 
   * a certain polling time which is active during the thread execution 
   * of the {@link Runnable}.
   * 
   * @param callable 
   * @param ui The {@link UI} to access or null of the current one should be used.
   * @param pollTime polling time in milliseconds
   * @param initialWait The initial maximal time to wait in milliseconds for the job to return. If you don't want to wait set this to <= 0.
   * @return The {@link Callable} that wraps the real one or null if an error occured
   */
  public static <T> Future<T> callInBackground(
    final int pollTime, 
    int initialWait,
    UI ui, 
    final Callable<T> callable) 
  {
    if(ui == null)
    {
      ui = UI.getCurrent();
    }
    
    if(ui != null && callable != null)
    {
      final UI finalUI = ui;
      final UUID id = setTime(finalUI, pollTime);
      
      Future<T> result = exec.submit(new Callable<T>()
      {
        @Override
        public T call() throws Exception
        {
          T result = null;
          try
          {
            result = callable.call();
          }
          catch(Exception ex)
          {
            log.error("exception in background job", ex);
            throw(ex);
          }
          finally
          {
            finalUI.access(new Runnable()
            {

              @Override
              public void run()
              {
                unsetTime(id, finalUI);
              }
            });
          }
          return result;
        }
      });
      try
      {
        if(initialWait > 0)
        {
          result.get(initialWait, TimeUnit.MILLISECONDS);
        }
      }
      catch (InterruptedException ex)
      {
        log.warn("Computation canceled", ex);
      }
      catch (ExecutionException ex)
      {
        log.error("Computation throw an error", ex);
      }
      catch (TimeoutException ex)
      {
        // intended behavior, nothing to report
      }
      
      return result;
    }
    
    return null;
  }
  
  public static class TimeMap extends ConcurrentHashMap<UUID, Integer>
  {
    
  }

  
}
