package annis.dao;

import org.springframework.jdbc.core.JdbcTemplate;


public class CountExtractor
{

  private String matchedNodesViewName;

  public int queryCount(JdbcTemplate jdbcTemplate)
  {
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT count(*) FROM ");
    sql.append(matchedNodesViewName);
    sql.append(" AS solutions");

    return jdbcTemplate.queryForInt(sql.toString());
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
