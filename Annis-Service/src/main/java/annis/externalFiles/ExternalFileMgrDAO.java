package annis.externalFiles;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.Assert;


/**
 * This class manages access to the data of the external file manager in 
 * database
 * @author Florian Zipser
 *
 */
public class ExternalFileMgrDAO
{
	private Logger log = Logger.getLogger(this.getClass());
	
	private SimpleJdbcTemplate simpleJdbcTemplate;

	//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"ExternalFileMgrDAO";		//Name dieses Tools
	
	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "): ";
	
	
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String	ERR_NO_FILENAME=			MSG_ERR + "Cannot insert the given ExternalFileObject, because the filename is set to null.";
	private static final String	ERR_NO_BRANCH=				MSG_ERR + "Cannot insert the given ExternalFileObject, because the branch is not set."; 

//	 ============================================== private Methoden ==============================================
	
	/**
	 * Checks weather an id exists in data source.
	 * @param id long - the id, for which should be searched
	 * @return true, if exists, else false
	 */
	public boolean hasId(Long id)
	{
		String sql = "SELECT count(id) FROM extData WHERE id = :id";
		SqlParameterSource args = new MapSqlParameterSource().addValue("id", id);
		int count = simpleJdbcTemplate.queryForInt(sql, args);
		return count > 0;
	}
	
	public long putExtFile(String filename, String branch, String mime) {
		ExtFileObjectDAO extFile = new ExtFileObjectImpl();
		
		extFile.setFileName(filename);
		extFile.setBranch(branch);
		extFile.setMime(mime);
		
		return putExtFile(extFile);
	}
	
	/**
	 * inserts a new entry with the given extFile -object in data source
	 * @param extFile
	 * @return a unique id to refer this object in data source
	 * @throws Exception
	 */
	public Long putExtFile(ExtFileObjectDAO extFile)
	{
		Assert.notNull(extFile.getFileName(), ERR_NO_FILENAME);
		Assert.notNull(extFile.getBranch(), ERR_NO_BRANCH);

		MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("filename", extFile.getFileName());
		args.addValue("origname", notNullString(extFile.getOrigFileName()));
		args.addValue("branch", extFile.getBranch());
		args.addValue("mime", notNullString(extFile.getMime()));
		args.addValue("comment", notNullString(extFile.getComment()));

		// FIXME: called during import but bypasses the staging area
		String insertSql = "" +
			"INSERT INTO extData(filename, orig_name, branch, mime, comment) " +
			"VALUES ( :filename, :origname, :branch, :mime, :comment )";
		simpleJdbcTemplate.update(insertSql, args);
		
		String selectSql = "SELECT id FROM extData WHERE filename = :filename AND branch = :branch";
		return simpleJdbcTemplate.queryForLong(selectSql, args);
	}
	
	/**
	 * Returns a ExtFileObjectDAO-object, which contains all informations corresponding
	 * to the given id.
	 * @param id long - unique identifier
	 * @return
	 * @throws Exception
	 */
	public ExtFileObjectDAO getExtFileObj(long id)
	{
		log.debug("Looking for binary file with id = " + id);
		
		String sql = "SELECT * FROM extData WHERE id = :id";
		SqlParameterSource args = new MapSqlParameterSource().addValue("id", id);
		ParameterizedRowMapper<ExtFileObjectDAO> rowMapper = new ParameterizedRowMapper<ExtFileObjectDAO>() {

			public ExtFileObjectDAO mapRow(ResultSet rs, int rowNum) throws SQLException {
				ExtFileObjectDAO extFile = new ExtFileObjectImpl();
				extFile.setID(rs.getLong("id"));
				extFile.setFileName(rs.getString("filename"));
				extFile.setOrigFileName(rs.getString("orig_name"));
				extFile.setBranch(rs.getString("branch"));
				extFile.setMime(rs.getString("mime"));
				extFile.setComment(rs.getString("comment"));
				extFile.setBranch(rs.getString("branch"));
				return extFile;
			}
			
		};
		return simpleJdbcTemplate.queryForObject(sql, rowMapper, args);
	}
	
	/**
	 * Deletes the entry which contains all informations corresponding
	 * to the given id.
	 * @param id long - unique identifier
	 * @throws Exception
	 */
	public void deleteExtFileObj(long id)
	{
		String sql = "DELETE FROM extDATA WHERE id = :id";
		SqlParameterSource args = new MapSqlParameterSource().addValue("id", id);
		simpleJdbcTemplate.update(sql, args);
	}
	
	///// Helper
	
	// XXX: Florian: Warum können die Werte im Schema nicht NULL sein?
	private String notNullString(String string) {
		return string != null ? string : "";
	}

	///// Getter / Setter
	public void setDataSource(DataSource dataSource) {
		simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
}
