/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.admin.model;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;
import org.corpus_tools.annis.gui.CriticalServiceQueryException;
import org.corpus_tools.annis.gui.ServiceQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * A model that handles the corpus list
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusManagement implements Serializable {

  private static final long serialVersionUID = -5750760811957151548L;

  private final TreeSet<String> corpora = new TreeSet<>();

  private ApiClientProvider clientProvider;

  private final Logger log = LoggerFactory.getLogger(CorpusManagement.class);

  public void clear() {
    corpora.clear();
  }

  public void delete(String corpusName)
      throws CriticalServiceQueryException, ServiceQueryException {
    if (clientProvider != null) {
      WebClient client = clientProvider.getWebClient();
      try {
        client.delete().uri("/corpora/{corpus}", corpusName).retrieve().bodyToMono(Void.class)
            .block();
      } catch (WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
          throw new CriticalServiceQueryException("You are not authorized to delete a corpus");
        } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
          throw new ServiceQueryException("Corpus with name " + corpusName + " not found");
        } else {
          log.error(null, ex);
          throw new ServiceQueryException("Remote exception: " + ex.getLocalizedMessage());
        }
      }

    }
  }

  public void fetchFromService() throws CriticalServiceQueryException, ServiceQueryException {
    if (clientProvider != null) {
      corpora.clear();

      try {
        List<String> queriedCorpora = clientProvider.getWebClient().get().uri("/corpora").retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<String>>() {}).block();
        corpora.addAll(queriedCorpora);
      } catch (WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
          throw new CriticalServiceQueryException("You are not authorized to get the corpus list.");
        } else {
          log.error(null, ex);
          throw new ServiceQueryException("Remote exception: " + ex.getLocalizedMessage());
        }
      }

    }
  }

  public ImmutableList<String> getCorpora() {
    return ImmutableList.copyOf(corpora);
  }

  public ApiClientProvider getClientProvider() {
    return clientProvider;
  }

  public void setClientProvider(ApiClientProvider clientProvider) {
    this.clientProvider = clientProvider;
  }
}
