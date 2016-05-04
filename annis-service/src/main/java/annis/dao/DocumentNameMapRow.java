package annis.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class DocumentNameMapRow implements ParameterizedRowMapper<String>
{

	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException
	{		
		return rs.getString("document_name");
	}
	
}
