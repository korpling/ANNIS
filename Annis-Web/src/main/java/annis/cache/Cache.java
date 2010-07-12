/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
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
package annis.cache;

import java.io.IOException;

public interface Cache {

	public abstract String get(String key) throws CacheException;
	
	public abstract byte[] getBytes(String key) throws CacheException;

	public abstract void put(String key, String value);
	
	public abstract void put(String key, byte[] value);

	public abstract void invalidateAll();
  
	public abstract void invalidate();

}