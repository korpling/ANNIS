package annisservice.exceptions;

public class AnnisCorpusAccessException extends AnnisServiceException {
	private static final long serialVersionUID = 2360084114228587837L;

	public AnnisCorpusAccessException() {
		super();
	}

	public AnnisCorpusAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnnisCorpusAccessException(String message) {
		super(message);
	}

	public AnnisCorpusAccessException(Throwable cause) {
		super(cause);
	}
	
}
