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

import java.io.File;
import java.io.Serializable;

/**
 * This class contains a reference to a file object and metainformations of it.
 * This interface is just for communication between ExtFileMgr and and all clients.
 * 
 * @author Florian Zipser
 *
 */
public interface ExtFileObjectCom extends ExtFileObject, Serializable
{
	/**
	 * returns a file object to this file
	 * @return the context of this object
	 */
	public File getFile();
	
	/**
	 * returns a file object to this file
	 * @param fName String - a user defined name for the context file 
	 * @return the context of this object with the given name
	 */
	public File getFile(String fName);
	
	/**
	 * sets the file of this object
	 * @param file File - the file
	 */
	public void setFile(File file);
	
	/**
	 * Returns this file as Byte-array, this method is needed for RMI. 
	 * @return
	 */
	//public byte[] getBytes();
	
	/**
	 * Sets this Byte-array, this method is needed for RMI. 
	 * @return
	 */
	//public void setBytes(byte[] bytes);
}
