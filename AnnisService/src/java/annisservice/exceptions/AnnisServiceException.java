package annisservice.exceptions;

public class AnnisServiceException extends RuntimeException {
	private static final long serialVersionUID = -3959838665379471035L;

	public AnnisServiceException() { 
		super();
	}

	public AnnisServiceException(String message) {
		super(message);
	}

	public AnnisServiceException(Throwable cause) {
		super(cause);
	}

	public AnnisServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
