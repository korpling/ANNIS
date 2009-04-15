package ex.annis.ql.analysis;

import ex.annis.ql.node.Node;
import ex.annis.ql.node.Start;

public interface SemanticAnalysis {
	
	public abstract SemanticAnalysis analyze(Start start);

	public abstract boolean isValid();

	public abstract int getSearchExprPosition(Node node);

//	public abstract int getSearchExprCount();
//
//	public abstract String getDocumentName();

//	public abstract boolean isDocumentConstraint(AAnnotationSearchExpr node);
}