package annis.ql.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annis.exceptions.AnnisQLSemanticsException;
import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAndExpr;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.PExpr;
import annis.ql.node.Start;
import annis.ql.node.TDigits;


public class QueryValidator extends DepthFirstAdapter {

	private DnfTransformer dnfTransformer;
	
	public QueryValidator() {
		dnfTransformer = new DnfTransformer();
	}
	
	@Override
	public void caseStart(Start node) {
		Start normalized = (Start) node.clone();
		normalized.apply(dnfTransformer);
		normalized.getPExpr().apply(this);
	}
	
	@Override
	public void caseALinguisticConstraintExpr(ALinguisticConstraintExpr node) {
		throw new AnnisQLSemanticsException("Missing search expression");
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		Set<Integer> expected = new HashSet<Integer>();
		List<Set<Integer>> connectedNodeClasses = new ArrayList<Set<Integer>>();
		
		// determine the original positions of the search references in this alternative
		// and what nodes are connected by linguistic operators
		for (PExpr expr : node.getExpr()) {
			if (expr instanceof ATextSearchExpr || expr instanceof AAnnotationSearchExpr || expr instanceof AAnyNodeSearchExpr) {
				expected.add(dnfTransformer.getPosition(expr));
			}
			if (expr instanceof ALinguisticConstraintExpr) {
				ALinguisticConstraintExpr lingOp = (ALinguisticConstraintExpr) expr;
				
				// unary expression, can't connect two search expressions
				if (lingOp.getRhs() == null)
					continue;
				
				int lhs = digits(lingOp.getLhs());
				int rhs = digits(lingOp.getRhs());
				
				Set<Integer> connectedNodeClass = new HashSet<Integer>();
				connectedNodeClass.add(lhs);
				connectedNodeClass.add(rhs);
				
				connectedNodeClasses.add(connectedNodeClass);
			}
		}
		
		// only one search expression
		if (expected.size() == 1) {
			// assert that there are no binary lingops in this clause
			if (connectedNodeClasses.isEmpty())
				return;
			else {
				throw new AnnisQLSemanticsException("Variable not bound.");
			}
		}
		
		// more than one expression => there must be at least one binary lingop
		if (connectedNodeClasses.isEmpty()) {
			throw new AnnisQLSemanticsException("Variable not bound (use linguistic operators).");
		}
		
		// collapse list of sets of connected terms
		connectedNodeClasses = collapse(connectedNodeClasses);
		
		if (connectedNodeClasses.size() > 1) {
			HashSet<Integer> actual = new HashSet<Integer>();
			for (Set<Integer> group : connectedNodeClasses)
				actual.addAll(group);
			if ( ! actual.containsAll(expected) )
				throw new AnnisQLSemanticsException("Variable not bound.");
			else if ( ! expected.containsAll(actual) )
				throw new AnnisQLSemanticsException("Variable bound to non-existing node.");
			else
				throw new AnnisQLSemanticsException("Variable groups not connected.");
		}
		
		Set<Integer> actual = connectedNodeClasses.get(0);
		boolean connected = expected.size() == actual.size() && actual.containsAll(expected);
		
		if ( ! connected ) {
			if ( ! actual.containsAll(expected) )
				throw new AnnisQLSemanticsException("Variable not bound.");
			else
				throw new AnnisQLSemanticsException("Variable bound to non-existing node.");
		}
	}
	
	private List<Set<Integer>> collapse(List<Set<Integer>> classes) {
		int size = classes.size();
	
		if (size == 1)
			return classes;
		
		Set<Integer> first = classes.get(0);
		List<Integer> remove = new ArrayList<Integer>();
		for (int i = 1; i < size; ++i) {
			Set<Integer> next = classes.get(i);
			for (int ref : next) {
				if (first.contains(ref)) {
					remove.add(i);
					first.addAll(next);
					break;
				}
			}
		}
		for (int i = remove.size() - 1; i >= 0; --i) {
			Integer index = remove.get(i);
			classes.remove(classes.get(index));
		}
		
		if (classes.size() == size)
			return classes;
		
		return collapse(classes);
	}

	private int digits(TDigits lhs) {
		return Integer.parseInt(lhs.getText());
	}

}
