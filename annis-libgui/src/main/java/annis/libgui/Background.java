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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
  private static final Logger log = LoggerFactory.getLogger(Background.class);
  
  public static Future<?> run(Runnable job)
  {
    return call(Executors.callable(job));
  }
  
  /**
   * Execute the job in the background and provide a callback which is called
   * when the job is finished.
   * 
   * It is guarantied that the callback is executed inside of the UI thread.
   * 
   * @param <T>
   * @param job
   * @param callback 
   */
  public static <T> void runWithCallback(Callable<T> job, final FutureCallback<T> callback)
  {
    final UI ui = UI.getCurrent();
    
    ListeningExecutorService exec = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    ListenableFuture<T> future = exec.submit(job);
    if(callback != null)
    {
      Futures.addCallback(future, new FutureCallback<T>()
      {

        @Override
        public void onSuccess(final T result)
        {
          ui.access(new Runnable()
          {

            @Override
            public void run()
            {
              callback.onSuccess(result);
            }
          });
        }

        @Override
        public void onFailure(final Throwable t)
        {
          ui.access(new Runnable()
          {

            @Override
            public void run()
            {
              callback.onFailure(t);
            }
          });
        }
      });
    }
  }
  
  public static <T> Future<T> call(
    final Callable<T> callable) 
  {
    
    if(callable != null)
    { 
      // create a new thread for every job to ensure that Vaadin.getSession() works
      // as expected
      ExecutorService exec = Executors.newSingleThreadExecutor();
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
          return result;
        }
      });
      
      return result;
    }
    
    return null;
  }
  
}
