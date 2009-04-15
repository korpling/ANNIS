package annisservice.handler;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.Assert;

import annisservice.exceptions.AnnisServiceException;

/**
 * Base class for handlers that implement the methods defined in the AnnisService interface.
 * 
 * <p>
 * The main responsibility of this class is to log and time the execution
 * of AnnisService requests and to handle exceptions in a consistent manner.
 * 
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 * 
 * @param <T>	Return type of the method that is implemented by this handler.
 */
public abstract class AnnisServiceHandler<T> {
	private Logger log = Logger.getLogger(this.getClass());
	
	// name/description of the handler, set in the constructor
	private String name;
	
	// easy access to a Spring JdbcTemplate object for subclasses
	private SimpleJdbcTemplate simpleJdbcTemplate;

	/**
	 * Construct a AnnisServiceHandler with a name.
	 * 
	 * <p>
	 * The name will be printed in the log to identify this handler.
	 * 
	 * @param name	The name of the handler.  Must not be <tt>null</tt>.
	 */
	public AnnisServiceHandler(String name) {
		Assert.notNull(name);
		this.name = name;
	}
	
	/**
	 * Abstract method for handler-specific code.
	 *
	 * @param args	Arguments that are passed to the AnnisService method
	 * 				implemented by this handler stored in a map.
	 * 
	 * @return	The object to be returned by the AnnisService method
	 * 			implemented by this handler.
	 */
	protected abstract T getResult(Map<String, Object> args);
	
	/**
	 * Handle a request to an AnnisService method.
	 * 
	 * <p>
	 * Logs and times the request and handles exceptions.
	 * 
	 * <ul>
	 * <li>{@link AnnisServiceException}s are passed to the caller.</li>
	 * <li>Exceptions that signal problems with the database are wrapped in an
	 * 	   appropriate {@link RemoteException}.</li>
	 * <li>All other {@link RuntimeException}s are wrapped in an {@link RemoteException}.</li>
	 * </ul>
	 * 
	 * @param args	Arguments that are passed to the AnnisService method
	 * 				implemented by this handler stored in a map.
	 * 
	 * @return	The object to be returned by the AnnisService method
	 * 			implemented by this handler.
	 * 
	 * @throws RemoteException	Something unexpected went wrong while answering the request.
	 */
	public T handleRequest(Map<String, Object> args) throws RemoteException {
		log.info(name + ( args != null ? " " + args : "" ) );
		
		long time = new Date().getTime();
		
		T result;
		try {
			result = getResult(args);
		} catch (AnnisServiceException e) {
			// ANNIS-specific error, pass up to frontend
			log.warn(name + " ERROR", e);
			throw e;
		} catch (DataAccessException e) {
			// something is wrong with the database
			log.error("A problem occured while accessing the database.", e);
			throw new RemoteException("A problem occured while accessing the database: " + e.getMessage());
		} catch (RuntimeException e) {
			// everything else
			log.error("An unexpected problem occured.", e);
			throw new RemoteException("An unexpected problem occured: " + e.getMessage());
		}
		
		log.info(name + " completed in " + (new Date().getTime() - time) + " ms.");

		return result;
	}
	
	///// GETTERS / SETTERS
	
	/**
	 * Setter for the DataSource that is used by the SimpleJdbcTemplate.
	 */
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	/**
	 * Getter for a Spring JdbcTemplate provided for convenience for subclasses.
	 */
	protected SimpleJdbcTemplate getSimpleJdbcTemplate() {
		return simpleJdbcTemplate;
	}
	
}
