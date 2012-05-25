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
package de.deutschdiachrondigital.dddquery;

import annis.ql.node.ATextSearchNotEqualExpr;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import annis.exceptions.AnnisMappingException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.node.AAndExpr;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.AArityLingOp;
import annis.ql.node.ADirectDominanceSpec;
import annis.ql.node.ADirectPointingRelationSpec;
import annis.ql.node.ADirectPrecedenceSpec;
import annis.ql.node.ADirectSiblingSpec;
import annis.ql.node.ADominanceLingOp;
import annis.ql.node.AEdgeAnnotation;
import annis.ql.node.AEdgeSpec;
import annis.ql.node.AEqualAnnoValue;
import annis.ql.node.AExactOverlapLingOp;
import annis.ql.node.AImplicitAndExpr;
import annis.ql.node.AInclusionLingOp;
import annis.ql.node.AIndirectDominanceSpec;
import annis.ql.node.AIndirectPointingRelationSpec;
import annis.ql.node.AIndirectPrecedenceSpec;
import annis.ql.node.AIndirectSiblingSpec;
import annis.ql.node.ALeftAlignLingOp;
import annis.ql.node.ALeftLeftOrRight;
import annis.ql.node.ALeftOverlapLingOp;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.AMetaConstraintExpr;
import annis.ql.node.AOrExpr;
import annis.ql.node.AOverlapLingOp;
import annis.ql.node.APointingRelationLingOp;
import annis.ql.node.APrecedenceLingOp;
import annis.ql.node.ARangeDominanceSpec;
import annis.ql.node.ARangePrecedenceSpec;
import annis.ql.node.ARangeSpec;
import annis.ql.node.ARegexpTextSpec;
import annis.ql.node.ARightAlignLingOp;
import annis.ql.node.ARightLeftOrRight;
import annis.ql.node.ARightOverlapLingOp;
import annis.ql.node.ARootLingOp;
import annis.ql.node.ASameAnnotationGroupLingOp;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.ATokenArityLingOp;
import annis.ql.node.AUnequalAnnoValue;
import annis.ql.node.AWildTextSpec;
import annis.ql.node.PAnnoValue;
import annis.ql.node.PEdgeAnnotation;
import annis.ql.node.PExpr;
import annis.ql.node.PLingOp;
import annis.ql.node.PTextSpec;
import annis.ql.node.Start;
import annis.ql.node.Token;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.SearchExpressionCounter;

public class DddQueryMapper {

	private static Logger log = Logger.getLogger(DddQueryMapper.class);

	// subclass to allow easier testing
	public static class InternalMapper extends DepthFirstAdapter {

		private StringBuffer dddQuery;
		private SearchExpressionCounter counter;

		public InternalMapper() {
			this.counter = new SearchExpressionCounter();
			this.dddQuery = new StringBuffer();
		}

		@Override
		public void caseARootLingOp(ARootLingOp node) {
			writeMapping("$n", lhs(node), "[isRoot()]");
		}

		@Override
		public void caseATokenArityLingOp(ATokenArityLingOp node) {
			writeMapping("$n", lhs(node), "[tokenArity(");
			node.getRangeSpec().apply(this);
			writeMapping(")]");
		}

		@Override
		public void caseAArityLingOp(AArityLingOp node) {
			writeMapping("$n", lhs(node), "[arity(");
			node.getRangeSpec().apply(this);
			writeMapping(")]");
		}
		
		@Override
		public void caseARangeSpec(ARangeSpec node) {
			writeMapping(token(node.getMin()));
			if (node.getMax() != null) {
				writeMapping(", ", token(node.getMax()));
			}
		}

		@Override
		public void caseASameAnnotationGroupLingOp(ASameAnnotationGroupLingOp node) {
			throw new AnnisMappingException("can't map @ lingop");
		}

		public String getDddQuery() {
			return dddQuery.toString();
		}
		
		@Override
		public void inStart(Start node) {
			node.apply(counter);
		}

		// tested
		@Override
		public void caseATextSearchExpr(ATextSearchExpr node) {
			writeMapping("element()", "#(n", pos(node), ")");

			if (node.getTextSpec() != null) { 
				writeMapping("[. = ");
				node.getTextSpec().apply(this);
				writeMapping("]");
			} else {
				writeMapping("[isToken()]");
			}

			writeMapping("$n", pos(node));
		}

    @Override
    public void caseATextSearchNotEqualExpr(ATextSearchNotEqualExpr node)
    {
      writeMapping("element()", "#(n", pos(node), ")");

			if (node.getTextSpec() != null)
      {
				writeMapping("[. != ");
				node.getTextSpec().apply(this);
				writeMapping("]");
			} 

			writeMapping("$n", pos(node));
    }



		@Override
		public void caseAAnyNodeSearchExpr(AAnyNodeSearchExpr node) {
			writeMapping("element()", "#(n", pos(node), ")", "$n", pos(node));
		}

		// tested
		@Override
		public void caseAWildTextSpec(AWildTextSpec node) {
			writeMapping("\"", token(node.getText()), "\"");
		}

		// tested
		@Override
		public void caseARegexpTextSpec(ARegexpTextSpec node) {
			writeMapping("r\"", token(node.getRegexp()), "\"");
		}

    @Override
    public void caseAEqualAnnoValue(AEqualAnnoValue node)
    {
      writeMapping(" = ");
      node.getTextSpec().apply(this);
    }
    
    @Override
    public void caseAUnequalAnnoValue(AUnequalAnnoValue node)
    {
      writeMapping(" != ");
      node.getTextSpec().apply(this);
    }

		// FIXME: Wert als Regexp
		@Override
		public void caseAAnnotationSearchExpr(AAnnotationSearchExpr node) {
			writeMapping("element()#(n", pos(node), ")", "[@");

			if (node.getAnnoNamespace() != null)
				writeMapping(token(node.getAnnoNamespace()), ":");

			writeMapping(token(node.getAnnoType()));

      PAnnoValue annoValue = node.getAnnoValue();
			if (annoValue != null)
      {
				annoValue.apply(this);
			}

			writeMapping("]$n", pos(node));
		}

		private String pos(PExpr expr) {
			return String.valueOf(counter.getPosition(expr));
		}

		@Override
		public void caseALinguisticConstraintExpr(ALinguisticConstraintExpr node) {
			node.getLingOp().apply(this);
		}

		@Override
		public void caseAExactOverlapLingOp(AExactOverlapLingOp node) {
			writeMapping("$n", lhs(node), "/matching-element::$n", rhs(node));
		}

		@Override
		public void caseALeftAlignLingOp(ALeftAlignLingOp node) {
			writeMapping("$n", lhs(node), "/left-align::$n", rhs(node));
		}

		@Override
		public void caseARightAlignLingOp(ARightAlignLingOp node) {
			writeMapping("$n", lhs(node), "/right-align::$n", rhs(node));
		}

		@Override
		public void caseAInclusionLingOp(AInclusionLingOp node) {
			writeMapping("$n", lhs(node), "/containing::$n", rhs(node));
		}

		@Override
		public void caseALeftOverlapLingOp(ALeftOverlapLingOp node) {
			writeMapping("$n", lhs(node), "/overlapping-following::$n", rhs(node));
		}

		@Override
		public void caseARightOverlapLingOp(ARightOverlapLingOp node) {
			writeMapping("$n", rhs(node), "/overlapping-following::$n", lhs(node));
		}

		@Override
		public void caseAOverlapLingOp(AOverlapLingOp node) {
//			writeMapping(
//					"(",
//					"$n", lhs(node), "/overlapping-following::$n", rhs(node),
//					" | ",
//					"$n", rhs(node), "/overlapping-following::$n", lhs(node),
//					")"
//			);
			writeMapping("$n", lhs(node), "/overlapping::$n", rhs(node));
		}

		@Override
		public void caseAPrecedenceLingOp(APrecedenceLingOp node) {
			try {
				node.getPrecedenceSpec().apply(this);
			} catch (NullPointerException e) {
				throw new RuntimeException("BUG: unknown precedence spec", e);
			}
		}

		// tested
		@Override
		public void caseADirectPrecedenceSpec(ADirectPrecedenceSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			writeMapping("$n", lhs(lingOp), "/immediately-following::$n", rhs(lingOp));
		}

		// tested
		@Override
		public void caseAIndirectPrecedenceSpec(AIndirectPrecedenceSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			writeMapping("$n", lhs(lingOp), "/following::$n", rhs(lingOp));
		}

		@Override
		public void caseARangePrecedenceSpec(ARangePrecedenceSpec node) {
			APrecedenceLingOp lingOp = (APrecedenceLingOp) node.parent();
			writeRangedAxis("following", (ARangeSpec) node.getRangeSpec(), lhs(lingOp), rhs(lingOp));
		}

		private void writeRangedAxis(String axis, ARangeSpec rangeSpec, String lhs,
				String rhs) {
			String range = String.valueOf(token(rangeSpec.getMin()));
			if (rangeSpec.getMax() != null)
				range += ", " + String.valueOf(token(rangeSpec.getMax()));

			writeMapping("$n", lhs, "/" + axis + "(", range, ")::$n", rhs);
		}

		@Override
		public void caseAPointingRelationLingOp(APointingRelationLingOp node) {
			try {
				node.getPointingRelationSpec().apply(this);
			} catch (NullPointerException e) {
				throw new RuntimeException("BUG: unknown pointing relation spec");
			}
		}
				
		@Override
		public void caseADirectPointingRelationSpec(ADirectPointingRelationSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			String name = token(node.getName());
			if (node.getEdgeSpec() != null) {
				writeMapping("$n", lhs(lingOp), "/child[p,", name, "]");
				node.getEdgeSpec().apply(this);
				writeMapping("::$n", rhs(lingOp));
			} else
				writeMapping("$n", lhs(lingOp), "/child[p,", name, "]::$n", rhs(lingOp));
		}
				
		@Override
		public void caseAIndirectPointingRelationSpec(AIndirectPointingRelationSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			String name = token(node.getName());
			writeMapping("$n", lhs(lingOp), "/descendant[p,", name, "]::$n", rhs(lingOp));
		}

		@Override
		public void caseADominanceLingOp(ADominanceLingOp node) {
			try {
				node.getDominanceSpec().apply(this);
			} catch (NullPointerException e) {
				throw new RuntimeException("BUG: unknown dominance spec");
			}
		}

		@Override
		public void caseADirectDominanceSpec(ADirectDominanceSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			writeMapping("$n", lhs(lingOp), "/child[");
			if (node.getLeftOrRight() != null) {
				node.getLeftOrRight().apply(this);
			} else {
				writeMapping("d");
			}
			if (node.getName() != null) {
				writeMapping(", ", token(node.getName()));
			}
			writeMapping("]");
			if (node.getEdgeSpec() != null)
				node.getEdgeSpec().apply(this);
			writeMapping("::$n", rhs(lingOp));
		}
		
		@Override
		public void caseALeftLeftOrRight(ALeftLeftOrRight node) {
			writeMapping("l");
		}
		
		@Override
		public void caseARightLeftOrRight(ARightLeftOrRight node) {
			writeMapping("r");
		}

		@Override
		public void caseAIndirectDominanceSpec(AIndirectDominanceSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			writeMapping("$n", lhs(lingOp), "/descendant[d");
			if (node.getName() != null)
				writeMapping(", ", token(node.getName()));
			writeMapping("]::$n", rhs(lingOp));
		}

		@Override
		public void caseAEdgeSpec(AEdgeSpec node) {
			writeMapping("(");
			List<PEdgeAnnotation> edgeAnnotations = node.getEdgeAnnotation();
			for (PEdgeAnnotation edgeAnnotation : edgeAnnotations)
				edgeAnnotation.apply(this);
			if (edgeAnnotations.size() > 0)
				dddQuery.setLength(dddQuery.length() - " ".length());
			writeMapping(")");
		}

		@Override
		public void caseAEdgeAnnotation(AEdgeAnnotation node) {
			if (node.getNamespace() != null)
				writeMapping(token(node.getNamespace()), ":");
			writeMapping(token(node.getType()));
			if (node.getValue() != null) {
				node.getValue().apply(this);
			}
			writeMapping(" ");
		}

//		@Override
//		public void caseALeftLeafDominanceSpec(ALeftLeafDominanceSpec node) {
//			PLingOp lingOp = (PLingOp) node.parent();
//			writeMapping("$n", lhs(lingOp), "/left-child::$n", rhs(lingOp));
//		}

//		@Override
//		public void caseARightLeafDominanceSpec(ARightLeafDominanceSpec node) {
//			PLingOp lingOp = (PLingOp) node.parent();
//			writeMapping("$n", lhs(lingOp), "/right-child::$n", rhs(lingOp));
//		}

		@Override
		public void caseARangeDominanceSpec(ARangeDominanceSpec node) {
			ADominanceLingOp lingOp = (ADominanceLingOp) node.parent();
			String axis = "descendant[d" + (node.getName() != null ? ", " + token(node.getName()) : "") + "]";
			writeRangedAxis(axis, (ARangeSpec) node.getRangeSpec(), lhs(lingOp), rhs(lingOp));
		}

		@Override
		public void caseADirectSiblingSpec(ADirectSiblingSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			writeMapping("$n", lhs(lingOp), "/sibling");
			if (node.getName() != null) {
				writeMapping("[", token(node.getName()), "]");
			}
			if (node.getEdgeSpec() != null)
				node.getEdgeSpec().apply(this);
			writeMapping("::$n", rhs(lingOp));
		}
		
		@Override
		public void caseAIndirectSiblingSpec(AIndirectSiblingSpec node) {
			PLingOp lingOp = (PLingOp) node.parent();
			writeMapping("$n", lhs(lingOp), "/common-ancestor");
			if (node.getName() != null) {
				writeMapping("[", token(node.getName()), "]");
			}
			writeMapping("::$n", rhs(lingOp));
		}
		
//		@Override
//		public void caseASiblingAndPrecedenceLingOp(ASiblingAndPrecedenceLingOp node) {
//			writeMapping("$n", lhs(node), "/following-sibling::$n", rhs(node));
//		}

		@Override
		public void caseAOrExpr(AOrExpr node) {
			writeMapping("( ");
			for (PExpr expr : node.getExpr()) {
				expr.apply(this);
				writeMapping(" | ");
			}
			dddQuery.setLength(dddQuery.length() - " & ".length());
			writeMapping(" )");
		}

		@Override
		public void caseAAndExpr(AAndExpr node) {
			writeMapping("( ");
			for (PExpr expr : node.getExpr()) {
				expr.apply(this);
				writeMapping(" & ");
			}
			dddQuery.setLength(dddQuery.length() - " & ".length());
			writeMapping(" )");
		}

		@Override
		public void caseAMetaConstraintExpr(AMetaConstraintExpr node) {
			writeMapping("meta(");
			if (node.getNamespace() != null) {
				writeMapping(token(node.getNamespace()), ":");
			}
			writeMapping(token(node.getName()));
			node.getValue().apply(this);
			writeMapping(")");
		}
		
		@Deprecated
		@Override
		public void caseAImplicitAndExpr(AImplicitAndExpr node) {
			Iterator<PExpr> it = node.getExpr().iterator();

			// write first factor
			writeMapping("( ");
			it.next().apply(this);

			// write next alternatives
			while (it.hasNext()) {
				writeMapping(" ) & ( ");
				it.next().apply(this);
			}

			// write end
			writeMapping(" )");
		}

		private void writeMapping(String... mapping) {
			for (String part : mapping)
				dddQuery.append(part);
		}

		private String token(Token token) {
			return token != null ? token.getText().trim() : null;
		}

		private String lhs(PLingOp node) {
			return token(((ALinguisticConstraintExpr) node.parent()).getLhs());
		}

		private String rhs(PLingOp node) {
			return token(((ALinguisticConstraintExpr) node.parent()).getRhs());
		}
	}

	private AnnisParser annisParser;
	private InternalMapper internalMapper;

	public String translate(String annisQuery) throws AnnisQLSemanticsException {
		log.debug("translating ANNIS QL query: " + annisQuery);

		Start statement = getAnnisParser().parse(annisQuery);
		
		InternalMapper internalMapper = getInternalMapper();
		statement.apply(internalMapper);
		String dddQuery = internalMapper.getDddQuery();
		
		if (dddQuery.startsWith("( ") && dddQuery.endsWith(" )"))
			dddQuery = dddQuery.substring(2, dddQuery.length() - 2);
		
		log.debug("translated DddQuery is: " + dddQuery);
		
		return dddQuery;
	}
	
	public List<Long> translateCorpusList(String corpusList) {
		Validate.notNull(corpusList, "corpusList=null passed as argument");
		
		final ArrayList<Long> corpora = new ArrayList<Long>();
		
		final String[] splits = StringUtils.split(corpusList, " ");
		for (String split : splits)
			try {
				corpora.add(Long.parseLong(split));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		
		return corpora;
	}

	public AnnisParser getAnnisParser() {
		return annisParser;
	}

	public void setAnnisParser(AnnisParser annisParser) {
		this.annisParser = annisParser;
	}

	public InternalMapper getInternalMapper() {
		return internalMapper;
	}

	public void setInternalMapper(InternalMapper internalMapper) {
		this.internalMapper = internalMapper;
	}

}
