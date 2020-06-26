/*
 * Copyright 2014 SFB 632.
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
package annis.administration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Collects the exceptions (throwables) from an import process and provides
 * several methods for extracting them.
 */
public interface ImportStatus extends Serializable {

    /**
     * Makes an conjuction of the {@link ImportStatus}, which means that if at least
     * one import failed the status is set to false.
     *
     * @param importStats
     *            The imported statistics which are connected.
     */
    public void add(ImportStatus importStats);

    /**
     * Assigns every Exception to a corpus.
     *
     * @param corpusName
     *            the name of the corpus
     * @param ex
     *            the exception
     */
    public void addException(String corpusName, Throwable ex);

    public Map<String, List<Throwable>> getAllThrowable();

    /**
     * Returns all excecptions.
     *
     * @return empty if no exceptions occurs.
     */
    public List<Exception> getExceptions();

    /**
     * Identifies the general success of an import. When at least one corpus import
     * fails, this returns false.
     *
     * @return the import status.
     */
    public boolean getStatus();

    /**
     * Returns all throwables of a specific corpus.
     *
     * @param corpusName
     *            the name of the corpus
     * @return null if no error occured with this corpus.
     */
    public List<Throwable> getThrowable(String corpusName);

    /**
     * Returns all throwables.
     *
     * @return empty if no exceptions occurs.
     */
    public List<Throwable> getThrowables();

    public String printDetails();

    public String printMessages();

    public String printType();

    /**
     * Set status of import
     *
     * @param status
     *            true, if everything is fine.
     */
    public void setStatus(boolean status);

}
