/*
 * Copyright 2012 SFB 632.
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

/**
 *
 * @author thomas
 */
public enum SchemeType
{

  FULLFACTS(FullFactsCorpusAdministration.class, "Full facts", "fullfacts"),
  ANNO_POOL(AnnoTableCorpusAdministration.class, "Annotation pool", "annopool");
  
  private final Class<? extends CorpusAdministration> adminClazz;
  private final String description;
  private final String scriptAppendix;

  private SchemeType(Class<? extends CorpusAdministration> adminClazz,
    String description, String scriptAppendix)
  {
    this.adminClazz = adminClazz;
    this.description = description;
    this.scriptAppendix = scriptAppendix;
  }


  public Class<? extends CorpusAdministration> getAdminClazz()
  {
    return adminClazz;
  }

  public String getDescription()
  {
    return description;
  }
  
  public String getScriptAppendix()
  {
    return scriptAppendix;
  }
  
}
