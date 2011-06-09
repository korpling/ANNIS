/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.deutschdiachrondigital.dddquery.parser;

import annis.sqlgen.UnknownExpressionException;
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

public class DnfTransformer extends DepthFirstAdapter {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public List<PExpr> listClauses(Start statement) throws UnknownExpressionException {

		// return list of clauses
		List<PExpr> clauses = new ArrayList<PExpr>();
		PExpr expr = statement.getPExpr();
		
		// many clauses: root is or
		if (expr instanceof AOrExpr) {
			List<PExpr> children = ((AOrExpr) expr).getExpr();
			for (PExpr child : children)
				clauses.add(isClause(child));
			
		// only one clause: root is single path or and
		} else
			clauses.add(isClause(expr));
		
		return clauses;
	}
	
	// test if PExpr is a Path or a AND; throw an exception if not
	private PExpr isClause(PExpr expr) throws UnknownExpressionException {
		if (expr instanceof APathExpr || expr instanceof AAndExpr)
			return expr;
		else
			throw new UnknownExpressionException("can't determine clause from: " + expr.getClass().getName());
	}

	@Override
	public void caseStart(Start node) {
		PExpr inDnf = normalize(node.getPExpr());
		node.setPExpr(inDnf);
		
		log.debug("dnf is: " + new Ast2String().toString(inDnf));
	}
	
	public PExpr normalize(PExpr expr) {
		if (expr instanceof APathExpr)
			return normalize((APathExpr) expr);
		else if (expr instanceof AOrExpr)
			return normalize((AOrExpr) expr);
		else if (expr instanceof AAndExpr)
			return normalize((AAndExpr) expr);
		
		throw new UnknownExpressionException("only paths and boolean combinations can be toplevel expressions: " + expr.getClass());
	}
	
	public PExpr normalize(APathExpr node) {
		return (PExpr) node.clone();
	}
	
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
		// XXX: Bug in SableCC: es müsste sein AAndExpr.setExpr(List<? extends PExpr> exprs)
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
