package annis.ql.parser;

import annis.ql.node.ATextSearchNotEqualExpr;
import java.util.HashMap;
import java.util.Map;

import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.Node;



public class SearchExpressionCounter extends DepthFirstAdapter {

	private Map<Node, Integer> exprToPos;
	private Map<Integer, Node> posToExpr;
	private int count;
	
	public SearchExpressionCounter() {
		this.count = 0;
		this.exprToPos = new HashMap<Node, Integer>();
		this.posToExpr = new HashMap<Integer, Node>();
	}
	
	public int getCount() {
		return count;
	}
	
	public Node getSearchExpression(int i) {
		return posToExpr.get(i);
	}
	
	public int getPosition(Node expr) {
		if (exprToPos.containsKey(expr))
			return exprToPos.get(expr);
		else
			return -1;
	}
	
	public void mapSearchExpressionClone(Node clone, Node original) {
		if ( !
      ( original instanceof ATextSearchExpr
      || original instanceof ATextSearchNotEqualExpr
      || original instanceof AAnnotationSearchExpr || original instanceof AAnyNodeSearchExpr) )
			return;
		
		int pos = getPosition(original);
		if (pos == -1)
			throw new RuntimeException("BUG: Unknown search expression encountered.");
		
		exprToPos.put(clone, pos);
	}

	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		addSearchExpr(node);
	}
	
	@Override
	public void caseATextSearchExpr(ATextSearchExpr node) {
		addSearchExpr(node);
	}

  @Override
  public void caseATextSearchNotEqualExpr(ATextSearchNotEqualExpr node)
  {
    addSearchExpr(node);
  }

	@Override
	public void caseAAnyNodeSearchExpr(AAnyNodeSearchExpr node) {
		addSearchExpr(node);
	}

	private void addSearchExpr(Node node) {
		++count;
		exprToPos.put(node, count);
		posToExpr.put(count, node);
	}

}
