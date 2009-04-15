package annisservice.exceptions;


public class AnnisQLSyntaxException extends AnnisServiceException {
	private static final long serialVersionUID = 5291798992268251561L;

	public AnnisQLSyntaxException() {
		super();
	}

	public AnnisQLSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnnisQLSyntaxException(String message) {
		super(message);
	}

	public AnnisQLSyntaxException(Throwable cause) {
		super(cause);
	}
	
	
}
