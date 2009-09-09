package annis;

/**
 * Signifies an error by the user, usually a bad command.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 */
@SuppressWarnings("serial")
public class UsageException extends AnnisRunnerException {

	public UsageException() {
		super();
	}

	public UsageException(String message, Throwable cause) {
		super(message, cause);
	}

	public UsageException(String message) {
		super(message);
	}

	public UsageException(Throwable cause) {
		super(cause);
	}

}
