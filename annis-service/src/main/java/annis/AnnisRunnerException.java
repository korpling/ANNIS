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
 * Base class for errors that occur during the execution of an AnnisRunner.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
@SuppressWarnings("serial")
public class AnnisRunnerException extends RuntimeException {

  private int exitCode = 1;
  
	public AnnisRunnerException(int exitCode) {
		super();
    this.exitCode = exitCode;
	}

	public AnnisRunnerException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnnisRunnerException(String message, int exitCode) {
		super(message);
    this.exitCode = exitCode;
	}

	public AnnisRunnerException(Throwable cause, int exitCode) {
		super(cause);
	}

  public final int getExitCode()
  {
    return exitCode;
  }
  
}
