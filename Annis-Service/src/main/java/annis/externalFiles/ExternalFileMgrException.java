package annis.externalFiles;

@SuppressWarnings("serial")
public class ExternalFileMgrException extends RuntimeException {

	public ExternalFileMgrException() {
		super();
	}

	public ExternalFileMgrException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExternalFileMgrException(String message) {
		super(message);
	}

	public ExternalFileMgrException(Throwable cause) {
		super(cause);
	}

}
