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

import java.io.Serializable;
import org.corpus_tools.annis.ApiClient;

/**
 * Defines a way to get a {@link WebResource} needed to make REST calls.
 * 
 * This interface extends {@link Serializable} so it can be included in other
 * serializable classes (the {@link WebResource} itself is not serializable).
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface ApiClientProvider extends Serializable {


    /**
     * Returns a (possible cached) {@link ApiClient}.
     * 
     * If the the user is authenticated this includes the authentication information.
     * 
     * @return The {@link ApiClient} but never {@code null}.
     */
    public ApiClient getClient();

    /**
     * Called when the client got invalid. E.g. the login might have changed and the cached resource
     * can't be used any longer. Will force the provider to update the client.
     */
    public void invalidateClient();
}
