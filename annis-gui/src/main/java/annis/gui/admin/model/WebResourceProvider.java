/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.admin.model;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;

/**
 * Defines a way to get a {@link WebResource} needed to make REST calls.
 * 
 * This interface extends {@link Serializable} so it can be included in
 * other serializable classes (the {@link WebResource} itself is not 
 * serializable).
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface WebResourceProvider extends Serializable
{
  
  /**
   * Returns a (possible cached) {@link WebResource}.
   * 
   * If the the user is authentifaced this includes the authentifaction information.
   * @return The {@link WebResource} but never {@code null}.
   */
  public WebResource getWebResource();
  
  /**
   * Returns a (possible cached) {@link AsyncWebResource}.
   * 
   * If the the user is authentifaced this includes the authentifaction information.
   * @return  The {@link AsyncWebResource} but never {@code null}.
   */
  public AsyncWebResource getAsyncWebResource();  
  
  /**
   * Called when the web resource got invalid.
   * E.g. the login might have changed and the cached resource can't be used any longer.
   * Will force the provider to update the resources.
   */
  public void invalidateWebResource();
}
