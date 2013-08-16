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

import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@XmlRootElement
public class ImportJob
{
  public enum Status
  {
    WAITING, RUNNING, SUCCESS, ERROR
  }
  
  
  private String uuid;
  private ZipFile inZip;
  private String corpusName;
  private Status status;
  private List<String> messages = new LinkedList<String>();
  private boolean overwrite;
  private String statusEmail;

  public String getUuid()
  {
    return uuid;
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  @XmlTransient
  public ZipFile getInZip()
  {
    return inZip;
  }

  public void setInZip(ZipFile inZip)
  {
    this.inZip = inZip;
  }

  public String getCorpusName()
  {
    return corpusName;
  }

  public void setCorpusName(String corpusName)
  {
    this.corpusName = corpusName;
  }

  public Status getStatus()
  {
    return status;
  }

  public void setStatus(Status status)
  {
    this.status = status;
  }

  @XmlElement(name = "messages")
  public List<String> getMessages()
  {
    return messages;
  }

  public boolean isOverwrite()
  {
    return overwrite;
  }

  public void setOverwrite(boolean overwrite)
  {
    this.overwrite = overwrite;
  }

  public String getStatusEmail()
  {
    return statusEmail;
  }

  public void setStatusEmail(String statusEmail)
  {
    this.statusEmail = statusEmail;
  }
  
  
  
}
