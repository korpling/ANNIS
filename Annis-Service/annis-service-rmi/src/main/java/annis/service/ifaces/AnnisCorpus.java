/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.ifaces;

/**
 * Represents a Corpus.
 * 
 * @author k.huetter
 *
 */
public interface AnnisCorpus extends JSONAble, Comparable<AnnisCorpus>
{

  public abstract long getId();

  public abstract void setId(long id);

  public abstract String getName();

  public abstract void setName(String name);

  public abstract int getTextCount();

  public abstract void setTextCount(int textCount);

  public abstract int getTokenCount();

  public abstract void setTokenCount(int tokenCount);
}