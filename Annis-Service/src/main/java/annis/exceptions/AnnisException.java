package annis.exceptions;

public class AnnisException extends RuntimeException {
	private static final long serialVersionUID = -3959838665379471035L;

	public AnnisException() { 
		super();
	}

	public AnnisException(String message) {
		super(message);
	}

	public AnnisException(Throwable cause) {
		super(cause);
	}

	public AnnisException(String message, Throwable cause) {
		super(message, cause);
	}

}
