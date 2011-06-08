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

/**
 * This class contains a reference to a file object and metainformations of it.
 * @author Florian Zipser
 *
 */
public interface ExtFileObject 
{
	/**
	 * returns the unique id of this object
	 * @return
	 */
	public Long getID();
	
	/**
	 * Returns the filename of the context file 
	 * @return Name of the context file
	 * @throws Exception
	 */
	public String getFileName();
	
	/**
	 * sets the unique id of this object
	 * @param id long - the unique id
	 */
	/*
	public void setID(Long id) throws Exception;
	*/
	
	/**
	 * returns the original file name of this file
	 * @return
	 */
	/*
	public String getOrigName() throws Exception;
	*/
	/**
	 * sets the original name of file
	 * @param origName String - the original name
	 */
	//public void setOrigName(String origName) throws Exception;
	
	/**
	 * sets the branch to which the external file depends on.
	 * @param branch String - a unique name of the branch
	 */
	public void setBranch(String branch);
	
	/**
	 * returns the unique Branch name of this file
	 * @return unique Branch name
	 */
	public String getBranch();
	
	/**
	 * returns the mime type of this file
	 * @return
	 */
	public String getMime();
	
	/**
	 * sets the mime type of file
	 * @param mime String - the mime type
	 */
	public void setMime(String mime);
	
	/**
	 * returns the comment belongs to this object
	 * @return
	 */
	public String getComment();
	
	/**
	 * sets a comment to the file of this object
	 * @param comment String - a comment
	 */
	public void setComment(String comment);
}
