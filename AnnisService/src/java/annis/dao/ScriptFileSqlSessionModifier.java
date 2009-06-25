package annis.dao;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.administration.SpringAnnisAdministrationDao;
import annis.ql.parser.QueryData;

public class ScriptFileSqlSessionModifier implements SqlSessionModifier {

	// this SQL script will be executed before the query runs
	private String scriptFile;
	
	// dependencies
	private SpringAnnisAdministrationDao administrationDao;

	// execute SQL from scriptFile
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate,
			QueryData queryData) {
		administrationDao.executeSqlFromScript(scriptFile);
	}

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	public SpringAnnisAdministrationDao getAdministrationDao() {
		return administrationDao;
	}

	public void setAdministrationDao(SpringAnnisAdministrationDao administrationDao) {
		this.administrationDao = administrationDao;
	}

}
