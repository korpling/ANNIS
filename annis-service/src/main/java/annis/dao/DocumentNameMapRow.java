package annis.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class DocumentNameMapRow implements RowMapper<String>
{

	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException
	{		
		return rs.getString("document_name");
	}
	
}
