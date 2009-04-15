package ex.annis.ql.helper;

import java.util.List;

import ex.annis.ql.node.AAndExpr;
import ex.annis.ql.node.AAnnotationSearchExpr;
import ex.annis.ql.node.AArityLingOp;
import ex.annis.ql.node.ADirectDominanceSpec;
import ex.annis.ql.node.ADirectPrecedenceSpec;
import ex.annis.ql.node.ADocumentConstraintExpr;
import ex.annis.ql.node.ADominanceLingOp;
import ex.annis.ql.node.AEdgeDominanceSpec;
import ex.annis.ql.node.AExactOverlapLingOp;
import ex.annis.ql.node.AImplicitAndExpr;
import ex.annis.ql.node.AInclusionLingOp;
import ex.annis.ql.node.AIndirectDominanceSpec;
import ex.annis.ql.node.AIndirectPrecedenceSpec;
import ex.annis.ql.node.ALeftAlignLingOp;
import ex.annis.ql.node.ALeftLeafDominanceSpec;
import ex.annis.ql.node.ALeftOverlapLingOp;
import ex.annis.ql.node.ALinguisticConstraintExpr;
import ex.annis.ql.node.AOrExpr;
import ex.annis.ql.node.APrecedenceLingOp;
import ex.annis.ql.node.ARangeDominanceSpec;
import ex.annis.ql.node.ARangePrecedenceSpec;
import ex.annis.ql.node.ARangeSpec;
import ex.annis.ql.node.ARegexpTextSpec;
import ex.annis.ql.node.ARightAlignLingOp;
import ex.annis.ql.node.ARightLeafDominanceSpec;
import ex.annis.ql.node.ARootLingOp;
import ex.annis.ql.node.ASameAnnotationGroupLingOp;
import ex.annis.ql.node.ASiblingAndPrecedenceLingOp;
import ex.annis.ql.node.ASiblingLingOp;
import ex.annis.ql.node.ATextSearchExpr;
import ex.annis.ql.node.ATokenArityLingOp;
import ex.annis.ql.node.AWildTextSpec;
import ex.annis.ql.node.EOF;
import ex.annis.ql.node.PDominanceSpec;
import ex.annis.ql.node.PExpr;
import ex.annis.ql.node.PLingOp;
import ex.annis.ql.node.PPrecedenceSpec;
import ex.annis.ql.node.PRangeSpec;
import ex.annis.ql.node.PTextSpec;
import ex.annis.ql.node.Start;
import ex.annis.ql.node.TDigits;
import ex.annis.ql.node.TId;
import ex.annis.ql.node.TRegexp;
import ex.annis.ql.node.TText;

public class AstBuilder {

	public Start newStart(PExpr expr) {
		Start n = new Start();
		n.setPExpr(expr);
		
		n.setEOF(new EOF());
		return n;
	}
	
	public ADocumentConstraintExpr newDocumentConstraintExpr(PTextSpec name) {
		ADocumentConstraintExpr n = new ADocumentConstraintExpr();
		
		n.setName(name);
		
		return n;
	}
	
	public AAnnotationSearchExpr newAnnotationSearchExpr(String type, PTextSpec value) {
		AAnnotationSearchExpr n = new AAnnotationSearchExpr();
		
		n.setAnnoType(newTId(type));
		if (value != null)
			n.setAnnoValue(value);
		
		return n;
	}
	
	public AAnnotationSearchExpr newAnnotationSearchExpr(String type) {
		return newAnnotationSearchExpr(type, null);
	}

	public ATextSearchExpr newTextSearchExpr(PTextSpec textSpec) {
		ATextSearchExpr n = new ATextSearchExpr();
		
		n.setTextSpec(textSpec);
		
		return n;
	}
	
	public ALinguisticConstraintExpr newLinguisticConstraintExpr(PLingOp operator, String lhs) {
		return newLinguisticConstraintExpr(operator, lhs, null);
	}

	public ALinguisticConstraintExpr newLinguisticConstraintExpr(PLingOp operator, String lhs, String rhs) {
		ALinguisticConstraintExpr n = new ALinguisticConstraintExpr();
		
		n.setLingOp(operator);
		n.setLhs(newTDigits(lhs));
		n.setRhs(newTDigits(rhs));
			
		return n;
	}

	public AImplicitAndExpr newImplicitAndExpr(List<PExpr> factors) {
		AImplicitAndExpr n = new AImplicitAndExpr();
		
		n.setExpr(factors);
		
		return n;
	}

	public AWildTextSpec newWildTextSpec(String text) {
		AWildTextSpec n = new AWildTextSpec();
		
		n.setText(new TText(text));
		
		return n;
	}
	
	public ARegexpTextSpec newRegexpTextSpec(String regexp) {
		ARegexpTextSpec n = new ARegexpTextSpec();
		
		n.setRegexp(new TRegexp(regexp));
		
		return n;
	}
	
	public ARootLingOp newRootLingOp() {
		return new ARootLingOp();
	}
	
	public AArityLingOp newArityLingOp(PRangeSpec rangeSpec) {
		AArityLingOp n = new AArityLingOp();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public ATokenArityLingOp newTokenArityLingOp(PRangeSpec rangeSpec) {
		ATokenArityLingOp n = new ATokenArityLingOp();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public AExactOverlapLingOp newExactOverlapLingOp() {
		return new AExactOverlapLingOp();
	}
	
	public ALeftAlignLingOp newLeftAlignLingOp() {
		return new ALeftAlignLingOp();
	}
	
	public ARightAlignLingOp newRightAlignLingOp() {
		return new ARightAlignLingOp();
	}
	
	public AInclusionLingOp newInclusionLingOp() {
		return new AInclusionLingOp();
	}
	
	public ALeftOverlapLingOp newLeftOverlapLingOp() {
		return new ALeftOverlapLingOp();
	}
	
	public APrecedenceLingOp newPrecedenceLingOp(PPrecedenceSpec precedenceSpec) {
		APrecedenceLingOp n = new APrecedenceLingOp();
		
		n.setPrecedenceSpec(precedenceSpec);
		
		return n;
	}
	
	public ADominanceLingOp newDominanceLingOp(PDominanceSpec dominanceSpec) {
		ADominanceLingOp n = new ADominanceLingOp();
		
		n.setDominanceSpec(dominanceSpec);
		
		return n;
	}
	
	public ASiblingLingOp newSiblingLingOp() {
		return new ASiblingLingOp();
	}
	
	public ASiblingAndPrecedenceLingOp newSiblingAndPrecedenceLingOp() {
		return new ASiblingAndPrecedenceLingOp();
	}
	
	public ASameAnnotationGroupLingOp newSameAnnotationGroupLingOp() {
		return new ASameAnnotationGroupLingOp();
	}
	
	public ARangeSpec newRangeSpec(String min, String max) {
		ARangeSpec n = new ARangeSpec();
		
		n.setMin(newTDigits(min));
		n.setMax(newTDigits(max));
		
		return n;
	}
	
	public ARangeSpec newRangeSpec(String num) {
		return newRangeSpec(num, null);
	}

	public TId newTId(String id) {
		return (id == null) ? (TId) null : new TId(id); 
	}
	
	public TDigits newTDigits(String digits) {
		return (digits == null) ? (TDigits) null : new TDigits(digits);
	}
	
	public ADirectPrecedenceSpec newDirectPrecedenceSpec() {
		return new ADirectPrecedenceSpec();
	}
	
	public AIndirectPrecedenceSpec newIndirectPrecedenceSpec() {
		return new AIndirectPrecedenceSpec();
	}
	
	public ARangePrecedenceSpec newRangePrecedenceSpec(PRangeSpec rangeSpec) {
		ARangePrecedenceSpec n = new ARangePrecedenceSpec();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public ADirectDominanceSpec newDirectDominanceSpec() {
		return new ADirectDominanceSpec();
	}
	
	public AIndirectDominanceSpec newIndirectDominanceSpec() {
		return new AIndirectDominanceSpec();
	}
	
	public ARangeDominanceSpec newRangeDominanceSpec(PRangeSpec rangeSpec) {
		ARangeDominanceSpec n = new ARangeDominanceSpec();
		
		n.setRangeSpec(rangeSpec);
		
		return n;
	}
	
	public ALeftLeafDominanceSpec newLeftLeafDominanceSpec() {
		return new ALeftLeafDominanceSpec();
	}

	public ARightLeafDominanceSpec newRightLeafDominanceSpec() {
		return new ARightLeafDominanceSpec();
	}
	
}
