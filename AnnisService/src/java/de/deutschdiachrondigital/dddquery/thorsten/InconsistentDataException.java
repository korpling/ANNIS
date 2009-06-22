/*
 * $Id: InconsistentDataException.java,v 1.1 2005/10/25 14:40:17 vitt Exp $
 *
 */
package de.deutschdiachrondigital.dddquery.thorsten;

/**
 * @author Thorsten Vitt
 */
@SuppressWarnings("serial")
public class InconsistentDataException extends Exception {

    /**
     * 
     */
    public InconsistentDataException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public InconsistentDataException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public InconsistentDataException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public InconsistentDataException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
