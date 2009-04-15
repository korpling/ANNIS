package annisservice.administration.exceptions;

@SuppressWarnings("serial")
public class FileAccessException extends RuntimeException {

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
