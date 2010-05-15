package annis.ql.parser;

import java.util.Arrays;
import java.util.List;

import de.deutschdiachrondigital.dddquery.node.ACommonAncestorAxis;
import de.deutschdiachrondigital.dddquery.node.ASiblingAxis;

import annis.ql.node.AAndExpr;
import annis.ql.node.AAnnotationSearchExpr;
import annis.ql.node.AAnyNodeSearchExpr;
import annis.ql.node.AArityLingOp;
import annis.ql.node.ADirectDominanceSpec;
import annis.ql.node.ADirectPrecedenceSpec;
import annis.ql.node.ADominanceLingOp;
import annis.ql.node.AEdgeAnnotation;
import annis.ql.node.AEdgeSpec;
import annis.ql.node.AEqualAnnoValue;
import annis.ql.node.AExactOverlapLingOp;
import annis.ql.node.AImplicitAndExpr;
import annis.ql.node.AInclusionLingOp;
import annis.ql.node.AIndirectDominanceSpec;
import annis.ql.node.AIndirectPrecedenceSpec;
import annis.ql.node.ALeftAlignLingOp;
import annis.ql.node.ALeftOverlapLingOp;
import annis.ql.node.ALinguisticConstraintExpr;
import annis.ql.node.AMetaConstraintExpr;
import annis.ql.node.AOrExpr;
import annis.ql.node.APrecedenceLingOp;
import annis.ql.node.ARangeDominanceSpec;
import annis.ql.node.ARangePrecedenceSpec;
import annis.ql.node.ARangeSpec;
import annis.ql.node.ARegexpTextSpec;
import annis.ql.node.ARightAlignLingOp;
import annis.ql.node.ARootLingOp;
import annis.ql.node.ASameAnnotationGroupLingOp;
import annis.ql.node.ASiblingLingOp;
import annis.ql.node.ATextSearchExpr;
import annis.ql.node.ATokenArityLingOp;
import annis.ql.node.AUnequalAnnoValue;
import annis.ql.node.AWildTextSpec;
import annis.ql.node.EOF;
import annis.ql.node.PAnnoValue;
import annis.ql.node.PDominanceSpec;
import annis.ql.node.PEdgeAnnotation;
import annis.ql.node.PExpr;
import annis.ql.node.PLingOp;
import annis.ql.node.PPrecedenceSpec;
import annis.ql.node.PRangeSpec;
import annis.ql.node.PTextSpec;
import annis.ql.node.Start;
import annis.ql.node.TDigits;
import annis.ql.node.TId;
import annis.ql.node.TRegexp;
import annis.ql.node.TText;


public class AstBuilder {

	public static Start newStart(PExpr expr) {
		Start n = new Start();
		n.setPExpr(expr);
		
		n.setEOF(new EOF());
		return n;
	}
	
	public static AMetaConstraintExpr newMetaConstraintExpr(String namespace, String name, String value) {
		AMetaConstraintExpr n = new AMetaConstraintExpr();
		n.setNamespace(newTId(namespace));
		n.setName(newTId(name));
		n.setValue(newEqualAnnoValue(newWildTextSpec(value)));
		return n;
	}
	
	public static AAnyNodeSearchExpr newAnyNodeSearchExpr() {
		return new AAnyNodeSearchExpr();
	}
	
	public static AAnnotationSearchExpr newAnnotationSearchExpr(String type, PAnnoValue value) {
		AAnnotationSearchExpr n = new AAnnotationSearchExpr();
		
		n.setAnnoType(newTId(type));
		if (value != null)
			n.setAnnoValue(value);
		
		return n;
	}
	
	public static AAnnotationSearchExpr newAnnotationSearchExpr(String type) {
		return newAnnotationSearchExpr(type, null);
	}

	public static ATextSearchExpr newTextSearchExpr(PTextSpec textSpec) {
		ATextSearchExpr n = new ATextSearchExpr();
		
		n.setTextSpec(textSpec);
		
		return n;
	}
	
	public static ALinguisticConstraintExpr newLinguisticConstraintExpr() {
		return newLinguisticConstraintExpr(null, null, null);
	}
	
	public static ALinguisticConstraintExpr newLinguisticConstraintExpr(PLingOp operator, String lhs) {
		return newLinguisticConstraintExpr(operator, lhs, null);
	}

	public static ALinguisticConstraintExpr newLinguisticConstraintExpr(PLingOp operator, String lhs, String rhs) {
		ALinguisticConstraintExpr n = new ALinguisticConstraintExpr();
		
		n.setLingOp(operator);
		n.setLhs(newTDigits(lhs));
		n.setRhs(newTDigits(rhs));
			
		return n;
	}
	
	public static AAndExpr newAndExpr(PExpr... exprs) {
		AAndExpr n = new AAndExpr();
		n.setExpr(Arrays.asList(exprs));
		return n;
	}
	
	public static AOrExpr newOrExpr(PExpr... exprs) {
		AOrExpr n = new AOrExpr();
		n.setExpr(Arrays.asList(exprs));
		return n;
	}

	public static AImplicitAndExpr newImplicitAndExpr(List<PExpr> factors) {
		AImplicitAndExpr n = new AImplicitAndExpr();
		
		n.setExpr(factors);
		
		return n;
	}

	public static AWildTextSpec newWildTextSpec(String text) {
		AWildTextSpec n = new AWildTextSpec();
		
		n.setText(new TText(text));
		
		return n;
	}
	
	public static ARegexpTextSpec newRegexpTextSpec(String regexp) {
		ARegexpTextSpec n = new ARegexpTextSpec();
		
		n.setRegexp(new TRegexp(regexp));
		
		return n;
	}

  public static AEqualAnnoValue newEqualAnnoValue(PTextSpec text) {
		AEqualAnnoValue n = new AEqualAnnoValue();
    n.setTextSpec(text);
	  return n;
	}

  public static AUnequalAnnoValue newUnequalAnnoValue(PTextSpec text) {
		AUnequalAnnoValue n = new AUnequalAnnoValue();
    n.setTextSpec(text);
	  return n;
	}
	
	public static ARootLingOp newRootLingOp() {
		return new ARootLingOp();
	}
	
	public static AArityLingOp newArityLingOp(PRangeSpec rangeSpec) {
		AArityLingOp n = new AArityLingOp();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public static ATokenArityLingOp newTokenArityLingOp(PRangeSpec rangeSpec) {
		ATokenArityLingOp n = new ATokenArityLingOp();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public static AExactOverlapLingOp newExactOverlapLingOp() {
		return new AExactOverlapLingOp();
	}
	
	public static ALeftAlignLingOp newLeftAlignLingOp() {
		return new ALeftAlignLingOp();
	}
	
	public static ARightAlignLingOp newRightAlignLingOp() {
		return new ARightAlignLingOp();
	}
	
	public static AInclusionLingOp newInclusionLingOp() {
		return new AInclusionLingOp();
	}
	
	public static ALeftOverlapLingOp newLeftOverlapLingOp() {
		return new ALeftOverlapLingOp();
	}
	
	public static APrecedenceLingOp newPrecedenceLingOp(PPrecedenceSpec precedenceSpec) {
		APrecedenceLingOp n = new APrecedenceLingOp();
		
		n.setPrecedenceSpec(precedenceSpec);
		
		return n;
	}
	
	public static ADominanceLingOp newDominanceLingOp(PDominanceSpec dominanceSpec) {
		ADominanceLingOp n = new ADominanceLingOp();
		n.setDominanceSpec(dominanceSpec);
		return n;
	}
	
	public static AEdgeSpec newEdgeSpec(PEdgeAnnotation... edgeAnnotations) {
		AEdgeSpec n = new AEdgeSpec();
		n.setEdgeAnnotation(Arrays.asList(edgeAnnotations));
		return n;
	}
	
	public static AEdgeAnnotation newEdgeAnnotation(String namespace, String name, PAnnoValue value) {
		AEdgeAnnotation n = new AEdgeAnnotation();
		n.setNamespace(newTId(namespace));
		n.setType(newTId(name));
		n.setValue(value);
		return n;
	}
	
	public static AEdgeAnnotation newEdgeAnnotation(String name, PAnnoValue value) {
		return newEdgeAnnotation(null, name, value);
	}
	
	public static AEdgeAnnotation newEdgeAnnotation(String namespace, String name) {
		return newEdgeAnnotation(namespace, name, null);
	}
	
	public static AEdgeAnnotation newEdgeAnnotation(String name) {
		return newEdgeAnnotation(null, name);
	}
	
	public static ASiblingLingOp newSiblingLingOp() {
		return new ASiblingLingOp();
	}
	
	public static ASameAnnotationGroupLingOp newSameAnnotationGroupLingOp() {
		return new ASameAnnotationGroupLingOp();
	}
	
	public static ARangeSpec newRangeSpec(String min, String max) {
		ARangeSpec n = new ARangeSpec();
		
		n.setMin(newTDigits(min));
		n.setMax(newTDigits(max));
		
		return n;
	}
	
	public static ARangeSpec newRangeSpec(String num) {
		return newRangeSpec(num, null);
	}

	public static TId newTId(String id) {
		return (id == null) ? (TId) null : new TId(id); 
	}
	
	public static TDigits newTDigits(String digits) {
		return (digits == null) ? (TDigits) null : new TDigits(digits);
	}
	
	public static ADirectPrecedenceSpec newDirectPrecedenceSpec() {
		return new ADirectPrecedenceSpec();
	}
	
	public static AIndirectPrecedenceSpec newIndirectPrecedenceSpec() {
		return new AIndirectPrecedenceSpec();
	}
	
	public static ARangePrecedenceSpec newRangePrecedenceSpec(PRangeSpec rangeSpec) {
		ARangePrecedenceSpec n = new ARangePrecedenceSpec();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public static ADirectDominanceSpec newDirectDominanceSpec() {
		return new ADirectDominanceSpec();
	}
	
	public static AIndirectDominanceSpec newIndirectDominanceSpec() {
		return new AIndirectDominanceSpec();
	}
	
	public static ARangeDominanceSpec newRangeDominanceSpec(PRangeSpec rangeSpec) {
		ARangeDominanceSpec n = new ARangeDominanceSpec();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}

	public static ASiblingAxis newSiblingAxis() {
		ASiblingAxis n = new ASiblingAxis();
		return n;
	}
	
	public static ACommonAncestorAxis newCommonAncestorAxis() {
		ACommonAncestorAxis n = new ACommonAncestorAxis();
		return n;
	}
	
}
