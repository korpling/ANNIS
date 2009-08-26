package annis.administration;

import annis.AnnisRunnerException;

@SuppressWarnings("serial")
public class FileAccessException extends AnnisRunnerException {

	public FileAccessException() {
		super();
	}

	public FileAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileAccessException(String message) {
		super(message);
	}

	public FileAccessException(Throwable cause) {
		super(cause);
	}

}
