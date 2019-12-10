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
package annis.service.objects;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Describes an import job.
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@XmlRootElement
public class ImportJob
{
  /**
   * Processing status of the job.
   */
  public enum Status
  {
    /**
     * Waits to be started.
     */
    WAITING, 
    /**
     * Import is currently running.
     */
    RUNNING, 
    /**
     * Import finished successfully.
     */
    SUCCESS, 
    /**
     * Import finished with an error.
     */
    ERROR
  }
  
  private String uuid;
  private File importRootDirectory;
  private String caption;
  private Status status;
  private final List<String> messages = new LinkedList<String>();
  private boolean overwrite;
  private String alias;
  private String statusEmail;

  /**
   * Get the unique identifier of this job.
   * @return  the UUID
   */
  public String getUuid()
  {
    return uuid;
  }

  /**
   * @see #getUuid() 
   * @param uuid  the UUID
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  /**
   * Get the directory where the corpora to import are located
   * @return a reference to the directory
   */
  @XmlTransient
  public File getImportRootDirectory()
  {
    return importRootDirectory;
  }

  /**
   * @see #getImportRootDirectory() 
   * @param importRootDirectory  the root directory
   */
  public void setImportRootDirectory(File importRootDirectory)
  {
    this.importRootDirectory = importRootDirectory;
  }
  
  

  /**
   * Get the visible caption of the import job.
   * The caption can be e.g. the corpus name.
   * @return the caption
   */
  public String getCaption()
  {
    return caption;
  }

  /**
   * @see #getCaption() 
   * @param caption the caption to set
   */
  public void setCaption(String caption)
  {
    this.caption = caption;
  }

  /**
   * Current status of the import.
   * @return the status
   */
  public Status getStatus()
  {
    return status;
  }

  /**
   * @see #getStatus()
   * @param status the status to set
   */
  public void setStatus(Status status)
  {
    this.status = status;
  }

  /**
   * A list of messages that where produces during the import process.
   * @return list of messages as string
   */
  @XmlElementWrapper(name = "messages")
  @XmlElement(name="m")
  public List<String> getMessages()
  {
    return messages;
  }

  /**
   * Get if the import is configured in a way that the corpus will be overwritten
   * when it already exists.
   * @return True if overwrite configured
   */
  public boolean isOverwrite()
  {
    return overwrite;
  }

  /**
   * @see #isOverwrite() 
   * @param overwrite whether to overwrite an existing corpus
   */
  public void setOverwrite(boolean overwrite)
  {
    this.overwrite = overwrite;
  }

  /**
   * Get the email address to which status reports should be send.
   * @return the e-mail status address
   */
  public String getStatusEmail()
  {
    return statusEmail;
  }

  /**
   * @see #getStatusEmail() 
   * @param statusEmail the e-mail status address
   */
  public void setStatusEmail(String statusEmail)
  {
    this.statusEmail = statusEmail;
  }

  /**
   * Get alias name of the corpus as defined by the import request.
   * @return the alias
   */
  public String getAlias()
  {
    return alias;
  }

  /**
   * @see #getAlias() 
   * @param alias the alias
   */
  public void setAlias(String alias)
  {
    this.alias = alias;
  }
  
  
  
}
