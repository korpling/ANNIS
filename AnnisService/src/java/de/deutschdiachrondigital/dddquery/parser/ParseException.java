package de.deutschdiachrondigital.dddquery.parser;

@SuppressWarnings("serial")
public class ParseException extends RuntimeException {

	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
