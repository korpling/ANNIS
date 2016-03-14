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

package annis.gui.admin.view;

import com.google.common.util.concurrent.FutureCallback;
import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * An general interface for different toplevel ANNIS views.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface UIView extends Serializable
{
  public void addListener(Listener listener);
  
  public void showInfo(String info, String description);
  public void showBackgroundInfo(String info, String description);
  public void showWarning(String warning, String description);
  public void showError(String error, String description);
  
  /**
   * Execute a job in the background and call the callback when finished.
   * The callback must be executed in the same main UI thread.
   * @param <T>
   * @param job
   * @param callback 
   */
  public<T> void runInBackground(Callable<T> job, FutureCallback<T> callback);
  
  public interface Listener extends Serializable
  {
    public void loginChanged(boolean isLoggedIn);
    public void loadedTab(Object selectedTab);
  }
}
