package annis;

/**
 * Base class for errors that occur during the execution of an AnnisRunner.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
@SuppressWarnings("serial")
public class AnnisRunnerException extends RuntimeException {

	public AnnisRunnerException() {
		super();
	}

	public AnnisRunnerException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnnisRunnerException(String message) {
		super(message);
	}

	public AnnisRunnerException(Throwable cause) {
		super(cause);
	}

}
