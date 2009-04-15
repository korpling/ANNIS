package annisservice.handler;

import java.util.Map;

import annisservice.AnnisResultSetBuilder;

import de.deutschdiachrondigital.dddquery.helper.QueryExecution;

public abstract class GetPaulaHandler extends AnnisServiceHandler<String> {

//	private Logger log = Logger.getLogger(this.getClass());
	
	public GetPaulaHandler() {
		super("GET PAULA");
	}
	
	public final String TEXT_ID = "textId";
	
	@SuppressWarnings("unchecked")
	@Override
	protected String getResult(Map<String, Object> args) {
		long textId = (Long) args.get(TEXT_ID);
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT\n"); 
		sb.append("	'{-1}', rank3.pre, rank3.post,rank3.parent, rank3.edge, rank3.edge_value, struct_annotation.*, rank3.edge_type, rank3.edge_name\n");
		sb.append("FROM\n");
		sb.append("	struct\n");
		sb.append("	JOIN rank rank1 ON (rank1.struct_ref = struct.id)\n");           
		sb.append("	JOIN rank_text_ref rank2 ON (rank2.level = 0 AND rank2.text_ref = struct.text_ref AND rank2.pre <= rank1.pre AND rank2.post >= rank1.post)\n");
		sb.append("	JOIN rank_annotation rank3 ON (rank3.pre >= rank2.pre AND rank3.pre <= rank1.pre AND rank3.post <= rank2.post AND rank3.post >= rank1.post)\n");
		sb.append("	JOIN struct_annotation ON (rank3.struct_ref = struct_annotation.id)\n");
		sb.append("WHERE\n");
		sb.append("	struct.text_ref = " + textId + " AND struct.token_index IS NOT NULL\n");
		sb.append("ORDER BY rank3.pre");
		
		return annisResultSetBuilder().buildResultSet(queryExecution().executeQuery(sb.toString())).iterator().next().getPaula();
	}
	
	protected abstract AnnisResultSetBuilder annisResultSetBuilder();
	protected abstract QueryExecution queryExecution();

}
