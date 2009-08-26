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
import de.deutschdiachrondigital.dddquery.node.AEdgeTypeSpec;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementSpanAxis;
import de.deutschdiachrondigital.dddquery.node.AEndMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.AEqComparison;
import de.deutschdiachrondigital.dddquery.node.AExactEdgeAnnotation;
import de.deutschdiachrondigital.dddquery.node.AExactSearchNodeTest;
import de.deutschdiachrondigital.dddquery.node.AExistanceEdgeAnnotation;
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
import de.deutschdiachrondigital.dddquery.node.AMetaNodeTest;
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
import de.deutschdiachrondigital.dddquery.node.AQuotedText;
import de.deutschdiachrondigital.dddquery.node.ARangeSpec;
import de.deutschdiachrondigital.dddquery.node.ARegexpEdgeAnnotation;
import de.deutschdiachrondigital.dddquery.node.ARegexpLiteralExpr;
import de.deutschdiachrondigital.dddquery.node.ARegexpQuotedText;
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
import de.deutschdiachrondigital.dddquery.node.PEdgeAnnotation;
import de.deutschdiachrondigital.dddquery.node.PEdgeTypeSpec;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.PMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.PNodeTest;
import de.deutschdiachrondigital.dddquery.node.PPathType;
import de.deutschdiachrondigital.dddquery.node.PQuotedText;
import de.deutschdiachrondigital.dddquery.node.PRangeSpec;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.node.TId;
import de.deutschdiachrondigital.dddquery.node.TNumber;
import de.deutschdiachrondigital.dddquery.node.TPattern;

// FIXME: move to test
public class AstBuilder {

	public static Start newStart(PExpr expr) {
		Start n = new Start();
		n.setPExpr(expr);
		n.setEOF(new EOF());
		return n;
	}

	public static TId newTId(String id) {
		return (id == null) ? (TId) null : new TId(id); 
	}

	private static TPattern newTPattern(String pattern) {
		return (pattern == null) ? (TPattern) null : new TPattern(pattern);
	}

	private static TNumber newTNumber(String number) {
		return (number == null) ? (TNumber) null : new TNumber(number);
	}
	
	public static APathExpr newPathExpr(PStep... steps) {
		return newPathExpr(null, steps);
	}
	
	public static APathExpr newPathExpr(PPathType pathType, PStep... steps) {
		APathExpr n = new APathExpr();
		n.setPathType(pathType);
		if (steps != null)
			n.setStep(Arrays.asList(steps));
		return n;
	}
	
	public static AAbsolutePathType newAbsolutePathType() {
		return new AAbsolutePathType();
	}
	
	public static ARelativePathType newRelativePathType() {
		return new ARelativePathType();
	}

	public static AStep newStep() {
		return newStep(null, null);
	}
	
	public static AStep newStep(PAxis axis, PNodeTest nodeTest) {
		return newStep(axis, nodeTest, null, (PExpr[]) null, null);
	}
	
	public static AStep newStep(PAxis axis, PNodeTest nodeTest, String variable, PExpr... predicates) {
		return newStep(axis, nodeTest, null, predicates, variable);
	}
	
//	public static AStep newStep(PAxis axis, PNodeTest nodeTest, PMarkerSpec markerSpec, PExpr predicate, String variable) {
//		return newStep(axis, nodeTest, markerSpec, new PExpr[] { predicate}, variable);
//	}
//	
	public static AStep newStep(PAxis axis, PNodeTest nodeTest, PMarkerSpec markerSpec, PExpr[] predicates, String variable) {
		AStep n = new AStep();
		n.setAxis(axis);
		n.setNodeTest(nodeTest);
		n.setMarkerSpec(markerSpec);
		if (predicates != null)
			n.setPredicates(Arrays.asList(predicates));
		n.setVariable(newTId(variable));
		return n;
	}
	
	public static AStep newStep(PAxis axis, PNodeTest nodeTest, PExpr... predicates) {
		return newStep(axis, nodeTest, null, predicates, null);
	}
	
	// FIXME: remove me
	public static AChildAxis newChildAxis(PEdgeAnnotation... edgeAnnotations) {
		return newChildAxis("d", edgeAnnotations);
	}
	
	public static AChildAxis newChildAxis(String edgeType, PEdgeAnnotation... edgeAnnotations) {
		return newChildAxis(edgeType, null, edgeAnnotations);

	}
	
	public static AChildAxis newChildAxis(String edgeType, String name, PEdgeAnnotation... edgeAnnotations) {
		AChildAxis n = new AChildAxis();
		n.setEdgeTypeSpec(newEdgeTypeSpec(edgeType, name));
		n.setEdgeAnnotation(Arrays.asList(edgeAnnotations));
		return n;
	}
	
	public static PEdgeTypeSpec newEdgeTypeSpec(String edgeType, String name) {
		AEdgeTypeSpec n = new AEdgeTypeSpec();
		n.setEdgeType(newTId(edgeType));
		n.setName(newTId(name));
		return n;
	}
	
	public static PAxis newParentAxis() {
		return new AParentAxis();
	}
	
	public static ARangeSpec newRangeSpec(int distance) {
		return newRangeSpec(distance, null);
	}
	
	public static ARangeSpec newRangeSpec(int min, Integer max) {
		ARangeSpec n = new ARangeSpec();
		n.setMin(newTNumber(String.valueOf(min)));
		if (max != null)
			n.setMax(newTNumber(String.valueOf(max)));
		return n;
	}

	// FIXME: remove me
	public static ADescendantAxis newDescendantAxis() {
		return newDescendantAxis("d", (String) null);
	}
	
	public static ADescendantAxis newDescendantAxis(String edgeType, String name) {
		return newDescendantAxis(edgeType, name, null);
	}
	
	public static ADescendantAxis newDescendantAxis(String edgeType) {
		return newDescendantAxis(edgeType, (PRangeSpec) null);
	}
	
	public static ADescendantAxis newDescendantAxis(String edgeType, PRangeSpec rangeSpec) {
		return newDescendantAxis(edgeType, null, rangeSpec);
	}
	
	public static ADescendantAxis newDescendantAxis(String edgeType, String name, PRangeSpec rangeSpec) {
		ADescendantAxis n = new ADescendantAxis();
		n.setRangeSpec(rangeSpec);
		n.setEdgeTypeSpec(newEdgeTypeSpec(edgeType, name));
		return n;
	}

	public static PAxis newAncestorAxis() {
		return new AAncestorAxis();
	}
	
	public static AMarkerSpec newMarkerSpec(String marker) {
		AMarkerSpec n = new AMarkerSpec();
		n.setMarker(newTId(marker));
		return n;
	}

	public static AStartMarkerSpec newStartMarkerSpec(String marker) {
		AStartMarkerSpec n = new AStartMarkerSpec();
		n.setMarker(newTId(marker));
		return n;
	}

	public static AEndMarkerSpec newEndMarkerSpec(String marker) {
		AEndMarkerSpec n = new AEndMarkerSpec();
		n.setMarker(newTId(marker));
		return n;
	}

	public static ASiblingAxis newSiblingAxis() {
		return new ASiblingAxis();
	}
	
	public static AFollowingAxis newFollowingAxis(PRangeSpec rangeSpec) {
		AFollowingAxis n = new AFollowingAxis();
		n.setRangeSpec(rangeSpec);
		return n;
	}

	public static AFollowingAxis newFollowingAxis() {
		return newFollowingAxis(null);
	}

	public static PAxis newPrecedingAxis() {
		return new APrecedingAxis();
	}

	public static PAxis newFollowingSiblingAxis() {
		return new AFollowingSiblingAxis();
	}

	public static PAxis newPrecedingSiblingAxis() {
		return new APrecedingSiblingAxis();
	}

	public static PAxis newImmediatelyFollowingSiblingAxis() {
		return new AImmediatelyFollowingSiblingAxis();
	}

	public static PAxis newImmediatelyPrecedingSiblingAxis() {
		return new AImmediatelyPrecedingSiblingAxis();
	}

	public static PAxis newImmediatelyFollowingAxis() {
		return new AImmediatelyFollowingAxis();
	}

	public static PAxis newImmediatelyPrecedingAxis() {
		return new AImmediatelyPrecedingAxis();
	}

	public static PAxis newContainedAxis() {
		return new AContainedAxis();
	}

	public static PAxis newContainingAxis() {
		return new AContainingAxis();
	}

	public static PAxis newPrefixAxis() {
		return new APrefixAxis();
	}

	public static PAxis newSuffixAxis() {
		return new ASuffixAxis();
	}

	public static PAxis newOverlappingAxis() {
		return new AOverlappingAxis();
	}

	public static PAxis newOverlappingFollowingAxis() {
		return new AOverlappingFollowingAxis();
	}

	public static PAxis newOverlappingPrecedingAxis() {
		return new AOverlappingPrecedingAxis();
	}

	public static PAxis newWholeTextAxis() {
		return new AWholeTextAxis();
	}

	public static PAxis newElementSpanAxis() {
		return new AElementSpanAxis();
	}

	public static PAxis newAttributeAxis() {
		return new AAttributeAxis();
	}

	public static PAxis newLayerAxis(String name) {
		ALayerAxis n = new ALayerAxis();
		n.setName(newTId(name));
		return n;
	}

	public static AAlignmentSpec newAlignmentSpec(String role1, String role2, String greed1, String greed2) {
		AAlignmentSpec n = new AAlignmentSpec();
		n.setRole1(newTId(role1));
		n.setRole2(newTId(role2));
		n.setGreed1(newTId(greed1));
		n.setGreed2(newTId(greed2));
		return n;
	}

	public static PAxis newAlignmentAxis(AAlignmentSpec alignmentSpec) {
		AAlignmentAxis n = new AAlignmentAxis();
		n.setAlignmentSpec(alignmentSpec);
		return n;
	}

	public static PAxis newAlignedAxis(AAlignmentSpec alignmentSpec) {
		AAlignedAxis n = new AAlignedAxis();
		n.setAlignmentSpec(alignmentSpec);
		return n;
	}

	public static PNodeTest newVarrefNodeTest(String variable) {
		AVarrefNodeTest n = new AVarrefNodeTest();
		n.setVariable(newTId(variable));
		return n;
	}

	public static AOrExpr newOrExpr(PExpr... exprs) {
		AOrExpr n = new AOrExpr();
		if (exprs != null)
			n.setExpr(Arrays.asList(exprs));
		return n;
	}

	public static AAndExpr newAndExpr(PExpr... exprs) {
		AAndExpr n = new AAndExpr();
		if (exprs != null)
			n.setExpr(Arrays.asList(exprs));
		return n;
	}

	public static ANumberLiteralExpr newNumberLiteralExpr(int i) {
		return newNumberLiteralExpr(String.valueOf(i));
	}

	public static ANumberLiteralExpr newNumberLiteralExpr(String number) {
		ANumberLiteralExpr n = new ANumberLiteralExpr();
		n.setNumber(newTNumber(number));
		return n;
	}
	
	public static AStringLiteralExpr newStringLiteralExpr(String string) {
		AStringLiteralExpr n = new AStringLiteralExpr();
		n.setString(newTPattern(string));
		return n;
	}
	
	public static ARegexpLiteralExpr newRegexpLiteralExpr(String regexp) {
		ARegexpLiteralExpr n = new ARegexpLiteralExpr();
		n.setRegexp(newTPattern(regexp));
		return n;
	}

	public static AComparisonExpr newComparisonExpr(PComparison comp, PExpr lhs, PExpr rhs) {
		AComparisonExpr n = new AComparisonExpr();
		n.setComparison(comp);
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static AEqComparison newEqComparison() {
		return new AEqComparison();
	}
	
	public static ANeComparison newNeComparison() {
		return new ANeComparison();
	}
	
	public static ALtComparison newLtComparison() {
		return new ALtComparison();
	}
	
	public static ALeComparison newLeComparison() {
		return new ALeComparison();
	}
	
	public static AGtComparison newGtComparison() {
		return new AGtComparison();
	}
	
	public static AGeComparison newGeComparison() {
		return new AGeComparison();
	}
	
	public static PExpr newPlusExpr(PExpr lhs, PExpr rhs) {
		APlusExpr n = new APlusExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static PExpr newMinusExpr(PExpr lhs, PExpr rhs) {
		AMinusExpr n = new AMinusExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static PExpr newTimesExpr(PExpr lhs, PExpr rhs) {
		ATimesExpr n = new ATimesExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static PExpr newDivExpr(PExpr lhs, PExpr rhs) {
		ADivExpr n = new ADivExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static PExpr newIdivExpr(PExpr lhs, PExpr rhs) {
		AIdivExpr n = new AIdivExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static PExpr newModExpr(PExpr lhs, PExpr rhs) {
		AModExpr n = new AModExpr();
		n.setLhs(lhs);
		n.setRhs(rhs);
		return n;
	}

	public static AFunctionExpr newFunctionExpr(String name, PExpr... args) {
		AFunctionExpr n = new AFunctionExpr();
		n.setName(newTId(name));
		if (args != null)
			n.setArgs(Arrays.asList(args));
		return n;
	}

	public static AUnknownNodeTest newUnknownNodeTest(String namespace, String name) {
		AUnknownNodeTest n = new AUnknownNodeTest();
		n.setName(newTId(name));
		n.setNamespace(newTId(namespace));
		return n;
	}

	public static AElementNodeTest newElementNodeTest() {
		return newElementNodeTest(null);
	}
	
	public static AElementNodeTest newElementNodeTest(String name) {
		AElementNodeTest n = new AElementNodeTest();
		n.setName(newTId(name));
		return n;
	}
	
	public static AAttributeNodeTest newAttributeNodeTest() {
		return newAttributeNodeTest(null);
	}

	public static AAttributeNodeTest newAttributeNodeTest(String name) {
		return newAttributeNodeTest(null, name);
	}

	public static AAttributeNodeTest newAttributeNodeTest(String namespace, String name) {
		AAttributeNodeTest n = new AAttributeNodeTest();
		n.setNamespace(newTId(namespace));
		n.setName(newTId(name));
		return n;
	}

	public static AExactSearchNodeTest newExactSearchNodeTest(String pattern) {
		AExactSearchNodeTest n = new AExactSearchNodeTest();
		n.setPattern(newTPattern(pattern));
		return n;
	}

	public static ARegexpSearchNodeTest newRegexpSearchNodeTest(String pattern) {
		ARegexpSearchNodeTest n = new ARegexpSearchNodeTest();
		n.setPattern(newTPattern(pattern));
		return n;
	}
	
	public static AExistanceEdgeAnnotation newExistanceEdgeAnnotation(String namespace, String type) {
		AExistanceEdgeAnnotation n = new AExistanceEdgeAnnotation();
		n.setNamespace(newTId(namespace));
		n.setType(newTId(type));
		return n;
	}

	public static AExactEdgeAnnotation newExactEdgeAnnotation(String namespace, String name, String value) {
		AExactEdgeAnnotation n = new AExactEdgeAnnotation();
		n.setNamespace(newTId(namespace));
		n.setType(newTId(name));
		n.setValue(newTPattern(value));
		return n;
	}

	public static ARegexpEdgeAnnotation newRegexpEdgeAnnotation(String namespace, String name, String value) {
		ARegexpEdgeAnnotation n = new ARegexpEdgeAnnotation();
		n.setNamespace(newTId(namespace));
		n.setType(newTId(name));
		n.setValue(newTPattern(value));
		return n;
	}
	
	public static AMetaNodeTest newMetaNodeTest(String namespace, String name, PQuotedText quotedText) {
		AMetaNodeTest n = new AMetaNodeTest();
		n.setNamespace(newTId(namespace));
		n.setName(newTId(name));
		n.setValue(quotedText);
		return n;
	}
	
	public static AQuotedText newQuotedText(String pattern) {
		AQuotedText n = new AQuotedText();
		n.setString(newTPattern(pattern));
		return n;
	}

	public static ARegexpQuotedText newRegexpQuotedText(String regexp) {
		ARegexpQuotedText n = new ARegexpQuotedText();
		n.setRegexp(newTPattern(regexp));
		return n;
	}

}