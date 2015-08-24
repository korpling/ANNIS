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
package annis;

/**
 * Signifies an error by the user, usually a bad command.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
@SuppressWarnings("serial")
public class UsageException extends AnnisRunnerException {

	public UsageException() {
		super(4);
	}

	public UsageException(String message, Throwable cause) {
		super(message, cause);
	}

	public UsageException(String message) {
		super(message, 4);
	}

	public UsageException(Throwable cause) {
		super(cause, 4);
	}


  
}
