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
package annis.externalFiles;

import annis.service.ifaces.AnnisBinary;

/**
 * This class manages storing and getting external files for the ANNIS 2.0 system.
 * Main functions are putting a file and getting it back. 
 * The manager corresponds the given file to an unique id, which is reference to the file.
 * All files are stored in a single branch, a branch must be unique and can be for example
 * a unique corpus identifier. 
 *
 * @author Florian Zipser
 *
 */
public interface ExternalFileMgr 
{
	/**
	 * Returns whether a branch with the given name already exists or not.   
	 * @param branch String - name of the branch
	 * @return true if the branch exists
	 * @throws Exception
	 */
	public boolean hasBranch(String branch);	
	
	/**
	 * Creates a new branch with the given name, in which the external files would be stored.
	 * @param branch String - name of the new branch
	 * @throws Exception
	 */
	public void createBranch(String branch);
	
	/**
	 * Deletes an existing branch with the given name, in which the external files would be stored.
	 * @param branch String - name of the new branch
	 * @throws Exception Error if branch does not exists, or branch isn�t empty, for deleting recursivly call delete(String, true)
	 */
	public void deleteBranch(String branch);
	
	/**
	 * Deletes an existing branch with the given name, in which the external files would be stored.
	 * @param branch String - name of the new branch
	 * @param delRec boolean - if set to true, all entries will be deleted recursivly
	 * @throws Exception Error if branch does not exists, or branch isn�t empty and delRec isn�t set to true
	 */
	public void deleteBranch(String branch, boolean delRec);
	
	/**
	 * Returns true, if the eFileMgr has an entry with the given value
	 * @param id Long - reference to the needed file 
	 * @param true, if a value exists, else false 
	 */
	public boolean hasId(long id);
	
	/**
	 * Puts a new file into the storage and returns an unique id to refer it.
	 * @param extFile File - new file which should be inserted.
	 */
	public Long putFile(ExtFileObjectCom extFile);
	
	/**
	 * Returns the file which corresponds to the given reference. If there�s
	 * no file to the given reference null will be returned.
	 * @param id Long - reference to the needed file 
	 * @return file which corresponds to the given reference
	 * @throws Exception
	 */
	public ExtFileObjectCom getExtFileObj(Long id);
	
	/**
	 * Returns the file which corresponds to the given reference in byte packages. 
	 * If there�s no file to the given reference null will be returned.
	 * @param id Long - reference to the needed file 
	 * @return file which corresponds to the given reference
	 * @throws Exception
	 */
	public AnnisBinary getBinary(Long id); 
	
	/**
	 * Deletes a file from the eFileMgr. The file will be searched by the
	 * given id.
	 * @param id Long - reference to the needed file
	 * @throws Exception
	 */
	public void deleteFile(long id);
}
