package annis.sqlgen;

@SuppressWarnings("serial")
public class UnknownExpressionException extends RuntimeException {

	public UnknownExpressionException() {
		super();
	}

	public UnknownExpressionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownExpressionException(String message) {
		super(message);
	}

	public UnknownExpressionException(Throwable cause) {
		super(cause);
	}

}
