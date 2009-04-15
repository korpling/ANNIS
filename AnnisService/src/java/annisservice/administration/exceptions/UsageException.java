package annisservice.administration.exceptions;

@SuppressWarnings("serial")
public class UsageException extends RuntimeException {

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
