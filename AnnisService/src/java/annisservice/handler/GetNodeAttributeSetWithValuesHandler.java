package annisservice.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import annisservice.ifaces.AnnisAttribute;
import annisservice.ifaces.AnnisAttributeSet;
import annisservice.objects.AnnisAttributeImpl;
import annisservice.objects.AnnisAttributeSetImpl;

public class GetNodeAttributeSetWithValuesHandler extends AnnisServiceHandler<AnnisAttributeSet> {

	private Logger log = Logger.getLogger(this.getClass());
	
	public GetNodeAttributeSetWithValuesHandler() {
		super("GET NODE ATTRIBUTES WITH VALUES");
	}

	public final String CORPUS_LIST = "corpusList";
	
	@SuppressWarnings("unchecked")
	@Override
	protected AnnisAttributeSet getResult(Map<String, Object> args) {
		List<Long> corpusList = (List<Long>) args.get(CORPUS_LIST);
		
		// FIXME: als PreparedStatement ändern
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT ns, attribute, value ");
		sql.append("FROM struct_annotation ");
		sql.append("WHERE attribute IS NOT NULL ");
		if ( ! corpusList.isEmpty() ) {
			sql.append("AND doc_ref IN ( SELECT DISTINCT doc_id FROM doc_2_corp d, corpus c1, corpus c2 WHERE d.corpus_ref = c1.id AND c1.pre >= c2.pre AND c1.post <= c2.post AND c2.id IN ( ");
			for (long corpus : corpusList) {
				sql.append(corpus);
				sql.append(", ");
			}
			sql.setLength(sql.length() - ", ".length());
			sql.append(") )");
		}
		sql.append("ORDER BY ns, attribute, value");
		log.info("query:\n" + sql);
		
		ResultSetExtractor resultSetExtractor = new ResultSetExtractor() {

			public Object extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				AnnisAttributeSet annisAttributeSet = new AnnisAttributeSetImpl();
				
				AnnisAttribute annisAttribute = null;
				
				while (rs.next()) {
					String ns = rs.getString("ns");
					String name = rs.getString("attribute");
					if (ns != null)
						name = ns + ":" + name;

					if (annisAttribute == null || ! name.equals(annisAttribute.getName())) {
						if (annisAttribute != null)
							log.debug("found an attribute: " + annisAttribute.getName() + " " + annisAttribute.getValueSet());
						annisAttribute = new AnnisAttributeImpl();
						annisAttribute.setName(name);
						annisAttributeSet.add(annisAttribute);
					}
					
					annisAttribute.addValue(rs.getString("value"));
				}
				
				return annisAttributeSet;
			}
			
		};
		
		AnnisAttributeSet result = (AnnisAttributeSet) getSimpleJdbcTemplate().getJdbcOperations().query(sql.toString(), resultSetExtractor);

		log.info("Found " + result.size() + " node attributes.");

		return result;
		
	}

}
