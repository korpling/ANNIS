package annisservice.administration.exceptions;

@SuppressWarnings("serial")
public class AdministrationException extends RuntimeException {

	public AdministrationException() {
		super();
	}

	public AdministrationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AdministrationException(String message) {
		super(message);
	}

	public AdministrationException(Throwable cause) {
		super(cause);
	}

}
