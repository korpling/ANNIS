package annis.dao;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;


public class CountExtractor
{

  private String matchedNodesViewName;

  public String explain(JdbcTemplate jdbcTemplate, boolean analyze)
  {
   
    ParameterizedSingleColumnRowMapper<String> planRowMapper = 
      new ParameterizedSingleColumnRowMapper<String>();
    
    List<String> plan = jdbcTemplate.query((analyze ? "EXPLAIN ANALYZE " : "EXPLAIN ")
      + "\n" + getCountQuery(jdbcTemplate), planRowMapper);
    return StringUtils.join(plan, "\n"); 
  }

  public int queryCount(JdbcTemplate jdbcTemplate)
  {
    return jdbcTemplate.queryForInt(getCountQuery(jdbcTemplate));
  }

  private String getCountQuery(JdbcTemplate jdbcTemplate)
  {
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT count(*) FROM ");
    sql.append(matchedNodesViewName);
    sql.append(" AS solutions");

    return sql.toString();
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }



}
