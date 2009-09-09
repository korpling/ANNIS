package annis.administration;

import annis.AnnisRunnerException;

@SuppressWarnings("serial")
public class DatabaseAccessException extends AnnisRunnerException {

	public DatabaseAccessException() {
		super();
	}

	public DatabaseAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseAccessException(String message) {
		super(message);
	}

	public DatabaseAccessException(Throwable cause) {
		super(cause);
	}

}
