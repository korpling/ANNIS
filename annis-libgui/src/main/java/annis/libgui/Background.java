/*
 * Copyright 2015 SFB 632.
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

import com.vaadin.ui.UI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class Background
{
  private static final ExecutorService exec = Executors.newCachedThreadPool();
  private static final Logger log = LoggerFactory.getLogger(Background.class);
  
  public static Future<?> run(Runnable job)
  {
    return run(job, null, null);
  }
  
  public static Future<?> run(Runnable job, UI ui, Runnable guiUpdate)
  {
    return call(Executors.callable(job), ui, guiUpdate);
  }
  
  public static <T> Future<T> call(
    final Callable<T> callable) 
  {
    return call(callable, null, null);
  }
  
  public static <T> Future<T> call(
    final Callable<T> callable, UI ui, final Runnable guiUpdate) 
  {
    
    if(callable != null)
    { 
      final UI finalUI = ui == null ? UI.getCurrent() : ui;
      
      Future<T> result = exec.submit(new Callable<T>()
      {
        @Override
        public T call() throws Exception
        { 
          T result = null;
          try
          {
            result = callable.call();
            if(guiUpdate != null)
            {
              finalUI.access(guiUpdate);
            }
          }
          catch(Exception ex)
          {
            log.error("exception in background job", ex);
            throw(ex);
          }
          return result;
        }
      });
      
      return result;
    }
    
    return null;
  }
}
