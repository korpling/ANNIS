import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


public class TestConnectionPooling {

//	@Test
	public void driverManagerDataSource() throws SQLException {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setUrl("jdbc:postgresql://localhost:5432/dddquery");
		ds.setUsername("dddquery");
		ds.setPassword("dddquery");
		
		Connection[] connections = {
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection()
		};
		
		for (Connection connection : connections) {
			connection.close();
		}
	}
	
//	@Test
	public void pgConnectionPoolDataSource() throws SQLException {
		PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
		ds.setServerName("localhost");
		ds.setDatabaseName("dddquery");
		ds.setUser("dddquery");
		ds.setPassword("dddquery");
		
		Connection[] connections = {
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection()
		};
		
		for (Connection connection : connections) {
			connection.close();
		}
	}
	
	@Test
	public void pgoolingDataSource() throws SQLException {
		org.postgresql.ds.PGPoolingDataSource ds = new org.postgresql.ds.PGPoolingDataSource();
		ds.setServerName("localhost");
		ds.setDatabaseName("dddquery");
		ds.setUser("dddquery");
		ds.setPassword("dddquery");
		ds.setInitialConnections(2);
		ds.setMaxConnections(4);
		
		Connection[] connections = {
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection(),
				ds.getConnection()
		};
		
		for (Connection connection : connections) {
			connection.close();
		}
	}
	
}
