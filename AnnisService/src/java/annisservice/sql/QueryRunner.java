package annisservice.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class QueryRunner {
	private Connection dbConn;
	
	public QueryRunner(Properties properties) throws SQLException, ClassNotFoundException {
		this(
				properties.getProperty("dbURL"), 
				properties.getProperty("dbDriver"), 
				properties.getProperty("dbUser"), 
				properties.getProperty("dbPassword")
			);
	}
	
	public QueryRunner(String dbURL, String dbDriver, String dbUser, String dbPassword) throws SQLException, ClassNotFoundException {
		try {
			Class.forName(dbDriver).newInstance();
			dbConn = DriverManager.getConnection( dbURL, dbUser, dbPassword );
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public Connection getConnection() throws SQLException {
       return this.dbConn;
    }

	public ResultSet executeQuery(String sqlQuery) throws SQLException {
		PreparedStatement elemStmt = this.getConnection().prepareStatement(sqlQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet res = elemStmt.executeQuery();
		return res;
	}
	
	@Override
	public void finalize() {
		try {
			this.dbConn.close();
		} catch (SQLException e) {
			//ignore
		}
	}


}
