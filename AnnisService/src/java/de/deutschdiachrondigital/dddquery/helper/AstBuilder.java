package de.deutschdiachrondigital.dddquery.helper;

import java.util.Arrays;

import de.deutschdiachrondigital.dddquery.node.AAbsolutePathType;
import de.deutschdiachrondigital.dddquery.node.AAlignedAxis;
import de.deutschdiachrondigital.dddquery.node.AAlignmentAxis;
import de.deutschdiachrondigital.dddquery.node.AAlignmentSpec;
import de.deutschdiachrondigital.dddquery.node.AAncestorAxis;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AAttributeAxis;
import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AChildAxis;
import de.deutschdiachrondigital.dddquery.node.AComparisonExpr;
import de.deutschdiachrondigital.dddquery.node.AContainedAxis;
import de.deutschdiachrondigital.dddquery.node.AContainingAxis;
import de.deutschdiachrondigital.dddquery.node.ADescendantAxis;
import de.deutschdiachrondigital.dddquery.node.ADivExpr;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementSpanAxis;
import de.deutschdiachrondigital.dddquery.node.AEndMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AExactSearchNodeTest;
import de.deutschdiachrondigital.dddquery.node.AFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AFollowingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AFunctionExpr;
import de.deutschdiachrondigital.dddquery.node.AGeComparison;
import de.deutschdiachrondigital.dddquery.node.AGtComparison;
import de.deutschdiachrondigital.dddquery.node.AIdivExpr;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyFollowingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyPrecedingAxis;
import de.deutschdiachrondigital.dddquery.node.AImmediatelyPrecedingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.ALayerAxis;
import de.deutschdiachrondigital.dddquery.node.ALeComparison;
import de.deutschdiachrondigital.dddquery.node.ALtComparison;
import de.deutschdiachrondigital.dddquery.node.AMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AMinusExpr;
import de.deutschdiachrondigital.dddquery.node.AModExpr;
import de.deutschdiachrondigital.dddquery.node.ANeComparison;
import de.deutschdiachrondigital.dddquery.node.ANumberLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.AOverlappingAxis;
import de.deutschdiachrondigital.dddquery.node.AOverlappingFollowingAxis;
import de.deutschdiachrondigital.dddquery.node.AOverlappingPrecedingAxis;
import de.deutschdiachrondigital.dddquery.node.AParentAxis;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.APlusExpr;
import de.deutschdiachrondigital.dddquery.node.APrecedingAxis;
import de.deutschdiachrondigital.dddquery.node.APrecedingSiblingAxis;
import de.deutschdiachrondigital.dddquery.node.APrefixAxis;
import de.deutschdiachrondigital.dddquery.node.ARegexpLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ARegexpSearchNodeTest;
import de.deutschdiachrondigital.dddquery.node.ARelativePathType;
import de.deutschdiachrondigital.dddquery.node.ASiblingAxis;
import de.deutschdiachrondigital.dddquery.node.AStartMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.AStringLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ASuffixAxis;
import de.deutschdiachrondigital.dddquery.node.ATimesExpr;
import de.deutschdiachrondigital.dddquery.node.AUnknownNodeTest;
import de.deutschdiachrondigital.dddquery.node.AVarrefNodeTest;
import de.deutschdiachrondigital.dddquery.node.AWholeTextAxis;
import de.deutschdiachrondigital.dddquery.node.EOF;
import de.deutschdiachrondigital.dddquery.node.PAxis;
import de.deutschdiachrondigital.dddquery.node.PComparison;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.PMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.PNodeTest;
import de.deutschdiachrondigital.dddquery.node.PPathType;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.node.TId;
import de.deutschdiachrondigital.dddquery.node.TNumber;
import de.deutschdiachrondigital.dddquery.node.TPattern;


public class AstBuilder {

	public Start newStart(PExpr expr) {
		Start n = new Start();
		n.setPExpr(expr);
		n.setEOF(new EOF());
		return n;
	}

	public TId newTId(String id) {
		return (id == null) ? (TId) null : new TId(id); 
	}

	private TPattern newTPattern(String pattern) {
		return (pattern == null) ? (TPattern) null : new TPattern(pattern);
	}

	private TNumber newTNumber(String number) {
		return (number == null) ? (TNumber) null : new TNumber(number);
	}
	
	public APathExpr newPathExpr(PPathType pathType, PStep[] steps) {
		APathExpr n = new APathExpr();
		n.setPathType(pathType);
		if (steps != null)
			n.setStep(Arrays.asList(steps));
		return n;
	}
	
	public AAbsolutePathType newAbsolutePathType() {
		return new AAbsolutePathType();
	}
	
	public ARelativePathType newRelativePathType() {
		return new ARelativePathType();
	}

	public AStep newStep(PAxis axis, PNodeTest nodeTest) {
		return newStep(axis, nodeTest, null, (PExpr[]) null, null);
	}
	
//	public AStep newStep(PAxis axis, PNodeTest nodeTest, PMarkerSpec markerSpec, PExpr predicate, String variable) {
//		return newStep(axis, nodeTest, markerSpec, new PExpr[] { predicate}, variable);
//	}
//	
	public AStep newStep(PAxis axis, PNodeTest nodeTest, PMarkerSpec markerSpec, PExpr[] predicates, String variable) {
		AStep n = new AStep();
		n.setAxis(axis);
		n.setNodeTest(nodeTest);
		n.setMarkerSpec(markerSpec);
		if (predicates != null)
			n.setPredicates(Arrays.asList(predicates));
		n.setVariable(newTId(variable));
		return n;
	}
	
	public AChildAxis newChildAxis() {
		return new AChildAxis();
	}

	public PAxis newParentAxis() {
		return new AParentAxis();
	}

	public ADescendantAxis newDescendantAxis() {
		return new ADescendantAxis();
	}

	public PAxis newAncestorAxis() {
		return new AAncestorAxis();
	}
	
	public AMarkerSpec newMarkerSpec(String marker) {
		AMarkerSpec n = new AMarkerSpec();
		n.setMarker(newTId(marker));
		return n;
	}

	public AStartMarkerSpec newStartMarkerSpec(String marker) {
		AStartMarkerSpec n = new AStartMarkerSpec();
		n.setMarker(newTId(marker));
		return n;
	}

	public AEndMarkerSpec newEndMarkerSpec(String marker) {
		AEndMarkerSpec n = new AEndMarkerSpec();
		n.setMarker(newTId(marker));
		return n;
	}

	public ASiblingAxis newSiblingAxis() {
		return new ASiblingAxis();
	}

	public PAxis newFollowingAxis() {
		return new AFollowingAxis();
	}

	public PAxis newPrecedingAxis() {
		return new APrecedingAxis();
	}

	public PAxis newFollowingSiblingAxis() {
		return new AFollowingSiblingAxis();
	}

	public PAxis newPrecedingSiblingAxis() {
		return new APrecedingSiblingAxis();
	}

	public PAxis newImmediatelyFollowingSiblingAxis() {
		return new AImmediatelyFollowingSiblingAxis();
	}

	public PAxis newImmediatelyPrecedingSiblingAxis() {
		return new AImmediatelyPrecedingSiblingAxis();
	}

	public PAxis newImmediatelyFollowingAxis() {
		return new AImmediatelyFollowingAxis();
	}

	public PAxis newImmediatelyPrecedingAxis() {
		return new AImmediatelyPrecedingAxis();
	}

	public PAxis newContainedAxis() {
		return new AContainedAxis();
	}

	public PAxis newContainingAxis() {
		return new AContainingAxis();
	}

	public PAxis newPrefixAxis() {
		return new APrefixAxis();
	}

	public PAxis newSuffixAxis() {
		return new ASuffixAxis();
	}

	public PAxis newOverlappingAxis() {
		return new AOverlappingAxis();
	}

	public PAxis newOverlappingFollowingAxis() {
		return new AOverlappingFollowingAxis();
	}

	public PAxis newOverlappingPrecedingAxis() {
		return new AOverlappingPrecedingAxis();
	}

	public PAxis newWholeTextAxis() {
		return new AWholeTextAxis();
	}

	public PAxis newElementSpanAxis() {
		return new AElementSpanAxis();
	}

	public PAxis newAttributeAxis() {
		return new AAttributeAxis();
	}

	public PAxis newLayerAxis(String name) {
		ALayerAxis n = new ALayerAxis();
		n.setName(newTId(name));
		return n;
	}

	public AAlignmentSpec newAlignmentSpec(String role1, String role2, String greed1, String greed2) {
		AAlignmentSpec n = new AAlignmentSpec();
		n.setRole1(newTId(role1));
		n.setRole2(newTId(role2));
		n.setGreed1(newTId(greed1));
		n.setGreed2(newTId(greed2));
		return n;
	}

	public PAxis newAlignmentAxis(AAlignmentSpec alignmentSpec) {
		AAlignmentAxis n = new AAlignmentAxis();
		n.setAlignmentSpec(alignmentSpec);
		return n;
	}

	public PAxis newAlignedAxis(AAlignmentSpec alignmentSpec) {
		AAlignedAxis n = new AAlignedAxis();
		n.setAlignmentSpec(alignmentSpec);
		return n;
	}

	public PNodeTest newVarrefNodeTest(String variable) {
		AVarrefNodeTest n = new AVarrefNodeTest();
		n.setVariable(newTId(variable));
		return n;
	}

	public AOrExpr newOrExpr(PExpr[] exprs) {
		AOrExpr n = new AOrExpr();
		if (exprs != null)
			n.setExpr(Arrays.asList(exprs));
		return n;
	}

	public AAndExpr newAndExpr(PExpr[] exprs) {
		AAndExpr n = new AAndExpr();
		if (exprs != null)
			n.setExpr(Arrays.asList(exprs));
		return n;
	}

	public ANumberLiteralExpr newNumberLiteralExpr(int i) {
		return newNumberLiteralExpr(String.valueOf(i));
	}

	public ANumberLiteralExpr newNumberLiteralExpr(String number) {
		ANumberLiteralExpr n = new ANumberLiteralExpr();
		n.setNumber(newTNumber(number));
		return n;
	}
	
	public AStringLiteralExpr newStringLiteralExpr(String string) {
		AStringLiteralExpr n = new AStringLiteralExpr();
		n.setString(newTPattern(string));
		return n;
	}
	
	public ARegexpLiteralExpr newRegexpLiteralExpr(String regexp) {
		ARegexpLiteralExpr n = new ARegexpLiteralExpr();
		n.setRegexp(newTPattern(regexp));
		return n;
	}

	public AComparisonExpr newComparisonExpr(PComparison comp, PExpr lhs, PExpr rhs) {
		AComparisonExpr n = new AComparisonExpr();
		n.setComparison(comp);
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public AEqComparison newEqComparison() {
		return new AEqComparison();
	}
	
	public ANeComparison newNeComparison() {
		return new ANeComparison();
	}
	
	public ALtComparison newLtComparison() {
		return new ALtComparison();
	}
	
	public ALeComparison newLeComparison() {
		return new ALeComparison();
	}
	
	public AGtComparison newGtComparison() {
		return new AGtComparison();
	}
	
	public AGeComparison newGeComparison() {
		return new AGeComparison();
	}
	
	public PExpr newPlusExpr(PExpr lhs, PExpr rhs) {
		APlusExpr n = new APlusExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public PExpr newMinusExpr(PExpr lhs, PExpr rhs) {
		AMinusExpr n = new AMinusExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public PExpr newTimesExpr(PExpr lhs, PExpr rhs) {
		ATimesExpr n = new ATimesExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public PExpr newDivExpr(PExpr lhs, PExpr rhs) {
		ADivExpr n = new ADivExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public PExpr newIdivExpr(PExpr lhs, PExpr rhs) {
		AIdivExpr n = new AIdivExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public PExpr newModExpr(PExpr lhs, PExpr rhs) {
		AModExpr n = new AModExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public PExpr newFunctionExpr(String name, PExpr[] args) {
		AFunctionExpr n = new AFunctionExpr();
		n.setName(newTId(name));
		if (args != null)
			n.setArgs(Arrays.asList(args));
		return n;
	}

	public AUnknownNodeTest newUnknownNodeTest(String namespace, String name) {
		AUnknownNodeTest n = new AUnknownNodeTest();
		n.setName(newTId(name));
		n.setNamespace(newTId(namespace));
		return n;
	}
	
	public AElementNodeTest newElementNodeTest(String name) {
		AElementNodeTest n = new AElementNodeTest();
		n.setName(newTId(name));
		return n;
	}

	public AAttributeNodeTest newAttributeNodeTest(String name) {
		AAttributeNodeTest n = new AAttributeNodeTest();
		n.setName(newTId(name));
		return n;
	}

	public AExactSearchNodeTest newExactSearchNodeTest(String pattern) {
		AExactSearchNodeTest n = new AExactSearchNodeTest();
		n.setPattern(newTPattern(pattern));
		return n;
	}

	public ARegexpSearchNodeTest newRegexpSearchNodeTest(String pattern) {
		ARegexpSearchNodeTest n = new ARegexpSearchNodeTest();
		n.setPattern(newTPattern(pattern));
		return n;
	}

}