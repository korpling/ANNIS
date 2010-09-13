package annis.sqlgen;



import annis.model.AnnisNode;
import org.apache.log4j.Logger;


public class DefaultTableAccessStrategy extends TableAccessStrategy
{

	private Logger log = Logger.getLogger(this.getClass());


	public DefaultTableAccessStrategy() {
		super();
	}
	
	public DefaultTableAccessStrategy(AnnisNode node) {
		super(node);
	}
	
	
	///// Getter / Setter
  
}
