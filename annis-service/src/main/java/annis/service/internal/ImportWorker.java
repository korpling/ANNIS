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
package annis.service.internal;

import annis.administration.AdministrationDao;
import annis.utils.RelANNISHelper;
import com.google.common.io.Files;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ImportWorker implements Callable<String>
{
  
  private AdministrationDao adminDao;
  
  private ZipFile inZip;
  private String corpusName;
  
  private String status = null;
  
  final private String[] allTables = new String[] {
    "corpus", "corpus_annotation", "text", "node", "component", "rank",
    "node_annotation", "edge_annotation", "resolver_vis_map", "media_files",
    "example_queries"
  };

  public ImportWorker(AdministrationDao adminDao, ZipFile inZip,
    String corpusName)
  {
    this.adminDao = adminDao;
    this.inZip = inZip;
    this.corpusName = corpusName;
  }
  
  @Override
  public String call() throws Exception
  {
    // unzip
    File outDir = Files.createTempDir();
    for(String table : allTables)
    {
      ZipEntry entry = RelANNISHelper.getRelANNISEntry(inZip, table, "tab");
      if(entry != null)
      {
        
      }
    }
    
    return "";
  }

  public String getStatus()
  {
    return status;
  }
  
}
