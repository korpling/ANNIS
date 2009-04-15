package de.deutschdiachrondigital.dddquery.sql.preprocessors;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.helper.Ast2String;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;

public class DisjunctiveNormalformNormalizer extends DepthFirstAdapter {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void caseStart(Start node) {
		PExpr inDnf = normalize(node.getPExpr());
		node.setPExpr(inDnf);
		
		log.debug("dnf is: " + new Ast2String().toString(inDnf));
	}
	
	// only look at toplevel paths, ie traversal of tree should stop here
	@Override
	public void caseAPathExpr(APathExpr node) {
		return;
	}

	public PExpr normalize(PExpr expr) {
		if (expr instanceof APathExpr)
			return normalize((APathExpr) expr);
		else if (expr instanceof AOrExpr)
			return normalize((AOrExpr) expr);
		else if (expr instanceof AAndExpr)
			return normalize((AAndExpr) expr);
		throw new RuntimeException("only paths and boolean combinations can be toplevel expression: " + expr.getClass());
	}
	
	public PExpr normalize(APathExpr node) {
		return (PExpr) node.clone();
	}
	
	@SuppressWarnings("unchecked")
	public PExpr normalize(AOrExpr node) {
		List<PExpr> normalizedChildren = new ArrayList<PExpr>();
		for (PExpr expr : node.getExpr())
			normalizedChildren.add(normalize(expr));
		
		List<PExpr> children = new ArrayList<PExpr>();
		for (PExpr expr : normalizedChildren) {
			if (expr instanceof AOrExpr) {
				log.debug("inlining nested OR expression");

				AOrExpr or = (AOrExpr) expr;
				for (PExpr expr2 : or.getExpr())
					children.add(expr2);
			} else
				children.add(expr);
		}
		
		AOrExpr result = new AOrExpr();
		result.setExpr(children);
		return result;
	}

	@SuppressWarnings("unchecked")
	public PExpr normalize(AAndExpr node) {
		List<PExpr> normalizedChildren = new ArrayList<PExpr>();
		for (PExpr expr : node.getExpr())
			normalizedChildren.add(normalize(expr));
		
		List<AOrExpr> ors = new ArrayList<AOrExpr>();
		List<PExpr> children = new ArrayList<PExpr>();
		for (PExpr expr : normalizedChildren) {
			if (expr instanceof AAndExpr) {
				log.debug("inlining nested AND expression");

				AAndExpr and = (AAndExpr) expr;
				for (PExpr expr2 : and.getExpr())
					children.add(expr2);
			} else if (expr instanceof AOrExpr) {
				ors.add((AOrExpr) expr);
			} else
				children.add(expr);
		}
		
		AAndExpr result = new AAndExpr();
		result.setExpr(children);
		if (ors.isEmpty())
			return result;
		
		List<AAndExpr> ands = new ArrayList<AAndExpr>();
		ands.add(result);

		List<AAndExpr> distributed = distribute(ors, ands);
		// FIXME: Bug in SableCC: es müsste sein AAndExpr.setExpr(List<? extends PExpr> exprs)
		List<PExpr> alternatives = new ArrayList<PExpr>();
		alternatives.addAll(distributed);
		
		AOrExpr or = new AOrExpr();
		or.setExpr(alternatives);
		return or;
	}
	
	public List<AAndExpr> distribute(List<AOrExpr> ors, List<AAndExpr> ands) {
		List<AAndExpr> res = new ArrayList<AAndExpr>();
		
		AOrExpr or = ors.get(ors.size() - 1);
		ors.remove(or);
		
		log.debug("distributing alternative");
		
		for (PExpr expr : or.getExpr()) {			
			for (AAndExpr and : ands) {
				List<PExpr> alternative = new ArrayList<PExpr>();
				alternative.add((PExpr) expr.clone());
				
				for (PExpr factor : and.getExpr())
					alternative.add((PExpr) factor.clone());
				
				AAndExpr newAnd = new AAndExpr();
				newAnd.setExpr(alternative);
				res.add((AAndExpr) normalize(newAnd));
			}
		}
		
		if (ors.isEmpty())
			return res;
		else
			return distribute(ors, res);
	}
	
}
