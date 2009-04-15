package annisservice.exceptions;

public class AnnisBinaryNotFoundException extends AnnisServiceException {
	private static final long serialVersionUID = -6440920661178781203L;

	public AnnisBinaryNotFoundException() {
		super();
	}

	public AnnisBinaryNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnnisBinaryNotFoundException(String message) {
		super(message);
	}

	public AnnisBinaryNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
