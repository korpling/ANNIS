package ex.annis.ql.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import ex.annis.ql.helper.Ast2String;
import ex.annis.ql.node.AAndExpr;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.AAnyNodeSearchExpr;
import ex.annis.ql.node.ALinguisticConstraintExpr;
import ex.annis.ql.node.ATextSearchExpr;
import ex.annis.ql.node.Node;
import ex.annis.ql.node.PExpr;
import ex.annis.ql.node.Start;
import ex.annis.ql.node.TDigits;

public class SemanticAnalysisImpl extends DepthFirstAdapter {

	private DnfNormalizer dnfNormalizer;
	private SearchExpressionCounter expressionCounter;
	
	private class SearchReference {
		int id;
		boolean marked;
		List<SearchReference> neighbors;
		
		public SearchReference(int id) {
			this.id = id;
			this.marked = false;
			this.neighbors = new ArrayList<SearchReference>();
		}
		
		@Override
		public String toString() {
			return "#" + id + (marked ? "+" : "-");
		}
	}
	
	private Vector<SearchReference> searchReferences = new Vector<SearchReference>();

	Vector<Node> searchExpressions;
	
	public class Error {
		public String message;
		public String originalQuery;
		public String normalizedForm;
		public String badClause;
		public int badClauseNumber;
	}
	
	private Error error;
	private boolean valid = false;
	
	public SemanticAnalysisImpl() {
		searchExpressions = new Vector<Node>();
	}
	
	@Override
	public void caseStart(Start node) {
		Start normalized = (Start) node.clone();
		normalized.apply(dnfNormalizer);
		normalized.getPExpr().apply(this);
		
		node.apply(expressionCounter);
	}
	
	@Override
	public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
		valid = true;
	}
	
	@Override
	public void caseATextSearchExpr(ATextSearchExpr node) {
		valid = true;
	}
	
	@Override
	public void caseALinguisticConstraintExpr(ALinguisticConstraintExpr node) {
		valid = false;
		setError("Missing search expression", new Ast2String().toString(node));
	}
	
	@Override
	public void caseAAnyNodeSearchExpr(AAnyNodeSearchExpr node) {
		valid = true;
	}
	
	@Override
	public void caseAAndExpr(AAndExpr node) {
		valid = validateClause(node);
		if (error != null)
			error.badClauseNumber = 1;
	}
	
	private boolean validateClause(AAndExpr node) {
		Set<Integer> expected = new HashSet<Integer>();
		List<Set<Integer>> connectedNodeClasses = new ArrayList<Set<Integer>>();
		
		// determine the original positions of the search references in this alternative
		// and what nodes are connected by linguistic operators
		for (PExpr expr : node.getExpr()) {
			if (expr instanceof ATextSearchExpr || expr instanceof AAnnotationSearchExpr || expr instanceof AAnyNodeSearchExpr) {
				expected.add(dnfNormalizer.getPosition(expr));
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
				return true;
			else {
				setError("Variable not bound.", new Ast2String().toString(node));
				return false;
			}
		}
		
		// more than one expression => there must be at least one binary lingop
		if (connectedNodeClasses.isEmpty()) {
			setError("Variable not bound (use linguistic operators).", new Ast2String().toString(node));
			return false;
		}
		
		// collapse list of sets of connected terms
		connectedNodeClasses = collapse(connectedNodeClasses);
		
		if (connectedNodeClasses.size() > 1) {
			HashSet<Integer> actual = new HashSet<Integer>();
			for (Set<Integer> group : connectedNodeClasses)
				actual.addAll(group);
			if ( ! actual.containsAll(expected) )
				setError("Variable not bound.", new Ast2String().toString(node));
			else if ( ! expected.containsAll(actual) )
				setError("Variable bound to non-existing node.", new Ast2String().toString(node));
			else
				setError("Variable groups not connected.", new Ast2String().toString(node));
			return false;
		}
		
		Set<Integer> actual = connectedNodeClasses.get(0);
		boolean connected = expected.size() == actual.size() && actual.containsAll(expected);
		
		if ( ! connected ) {
			if ( ! actual.containsAll(expected) )
				setError("Variable not bound.", new Ast2String().toString(node));
			else
				setError("Variable bound to non-existing node.", new Ast2String().toString(node));
		}
		
		return connected;
	}
	
	private List<Set<Integer>> collapse(List<Set<Integer>> classes) {
//		List<Set<Integer>> classes = (List<Set<Integer>>) ((ArrayList<Set<Integer>>) classes1).clone();
		
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

	private boolean isConnected() {
		
		// visit all refs connected to the first
		Queue<SearchReference> toCheck = new LinkedList<SearchReference>();
		toCheck.offer(searchReferences.get(0));
		
		while ( ! toCheck.isEmpty() ) {
			SearchReference node = toCheck.remove();
			node.marked = true;
			for (SearchReference neighbor : node.neighbors) {
				if ( ! neighbor.marked ) 
					toCheck.offer(neighbor);
			}
		}
		
		// check for unmarked refs
		for (SearchReference node : searchReferences)
			if ( ! node.marked )
				return false;
		
		// none unmarked -> all refs connected
		return true;
	}

	public DnfNormalizer getDnfNormalizer() {
		return dnfNormalizer;
	}

	public void setDnfNormalizer(DnfNormalizer dnfNormalizer) {
		this.dnfNormalizer = dnfNormalizer;
	}
	
	private void setError(String message, String badClause) {
		error = new Error();
		error.message = message;
		error.badClause = badClause;
	}

	public Error getError() {
		return error;
	}

	public boolean isValid() {
		return valid;
	}
	
	public int getPosition(PExpr expr) {
		return expressionCounter.getPosition(expr);
	}

	public SearchExpressionCounter getExpressionCounter() {
		return expressionCounter;
	}

	public void setExpressionCounter(SearchExpressionCounter expressionCounter) {
		this.expressionCounter = expressionCounter;
	}

}
