package annis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

@Deprecated
public class QueryExecution {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private DataSource dataSource;
	
    private long time;
    
	public ResultSet executeQuery(String sqlQuery) {
		try {
			
			log.debug("executing sql query:\n" + sqlQuery);
			time = new Date().getTime();
			
			log.debug("obtaining connection");
			Connection connection = dataSource.getConnection();
			log.debug("preparing statement");
			PreparedStatement elemStmt = connection.prepareStatement(sqlQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        log.debug("executing query");
			ResultSet res = elemStmt.executeQuery();
			log.debug("closing connection");
			connection.close();
	        
	        time = new Date().getTime() - time;
			log.info("sql query executed in " + time + " ms");
			
	        return res;
		} catch (SQLException e) {
			log.warn("an exception occured", e);
			throw new RuntimeException(e);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
