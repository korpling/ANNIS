package de.deutschdiachrondigital.dddquery.sql.old;


public class TestPathQueryTranslator {

//	AstBuilder b;
//	PathQuery path;
//	PathQueryTranslator translator;
//	
//	@Before
//	public void setup() {
//		b = new AstBuilder();
//		path = new PathQuery();
//		translator = new PathQueryTranslator(path);
//	}
//	
//	// wenn kein target vorhanden ist, wird eins erstellt; dieses wird context im ersten schritt (inAPathExpr)
//	@Test
//	public void inARelativePathTypeNoTargetNoParent() throws ParserException, LexerException, IOException {
//		translator.inARelativePathType(null);
//		
//		AliasSet target = path.getTargetAliasSet();
//		assertThat(target, is(not(nullValue())));
//		assertThat(path.getAliasSets(), hasItem(target));
//	}
//	
//	// wenn kein target vorhanden ist, wird eins erstellt; dieses wird context im ersten schritt (inAPathExpr)
//	@Test
//	public void inARelativePathTypeParent() throws ParserException, LexerException, IOException {
//		AliasSet parent = new AliasSet(42);
//		path.setParentAliasSet(parent);
//		
//		translator.inARelativePathType(null);
//		
//		AliasSet target = path.getTargetAliasSet();
//		assertThat(target, sameInstance(parent));
//		assertThat(path.getAliasSets(), hasItem(parent));
//	}
//	
//	// wenn kein target vorhanden ist, wird eins erstellt; dieses wird context im ersten schritt (inAPathExpr)
//	@Test
//	public void inAPathExprTargetSet() throws ParserException, LexerException, IOException {
//		AliasSet target = path.newAliasSet();
//		translator.inARelativePathType(null);
//		assertThat(path.getTargetAliasSet(), sameInstance(target));
//	}
//	
//	// bei absoluten Pfaden ist der erste context ein Wurzelknoten (inAAbsolutePathType)
//	@Test
//	public void inAAbsolutePathType() {
//		translator.inAAbsolutePathType(null);
//		
//		AliasSet target = path.getTargetAliasSet();
//		assertThat(path.getConditions(), containsItem(new IsNullCondition(target.getColumn("rank", "parent"))));
//	}
//	
//	// Pfade in Prädikaten verändern nicht das target alias set
//	@Test
//	public void caseAPathExpr() {
//		parse("/child::element()[child::element()]").apply(translator);
//		assertThat(path.getTargetAliasSet().getId(), is(2));
//	}
//	
//	///// Achsen /////
//	
//	/**
//	 * Preconditions: 
//	 * - alias set
//	 * Result:
//	 * - a new alias set (old target becomes context)
//	 * - a join: rank1.pre = rank2.parent
//	 */
//	@Test
//	public void inAChildAxis() {
//		AliasSet context = path.newAliasSet();
//		AliasSet target = path.newAliasSet();
//		
//		translator.inAChildAxis(null);
//		
//		assertThat(path.getConditions(), containsItem(Join.eq(context.getColumn("rank", "pre"), target.getColumn("rank", "parent"))));
//	}
//	
//	@Test
//	public void inAParentAxis() {
//		AliasSet oldContext = path.newAliasSet();
//		AliasSet oldTarget = path.newAliasSet();
//		
//		translator.inAParentAxis(null);
//		AliasSet target = path.getTargetAliasSet();
//		
//		assertThat(oldTarget, sameInstance(path.getContextAliasSet()));
//		assertThat(oldTarget, not(sameInstance(target)));
//		assertThat(path.getConditions(), containsItems(
//				Join.eq(oldContext.getColumn("struct", "id"), oldTarget.getColumn("struct", "id")),
//				Join.eq(oldTarget.getColumn("rank", "parent"), target.getColumn("rank", "pre"))
//		));
//;	}
//	
//	@Test
//	public void inADescendantAxis() {
//		new AxisTest().runAxis("ADescendantAxis")
//			.expect().context("rank", "pre").op("<").target("rank", "pre")
//			.expect().context("rank", "post").op(">").target("rank", "post")
//			.test();
//	}
//	
//	@Test
//	public void inAAncestorAxis() {
//		new AxisTest().runAxis("AAncestorAxis")
//			.expect().aliasSet(-2, "struct", "id").op("=").target("struct", "id")
//			.expect().target("rank", "pre").op(">").context("rank", "pre")
//			.expect().target("rank", "post").op("<").context("rank", "post")
//			.test();
//	}
//	
//	@Test
//	public void inASiblingAxis() {
//		AliasSet context = path.newAliasSet();
//		AliasSet target = path.newAliasSet();
//		
//		translator.inASiblingAxis(null);
//		
//		assertThat(context, not(sameInstance(target)));
//		assertThat(path.getConditions(), 
//				containsItem(Join.eq(context.getColumn("rank", "parent"), target.getColumn("rank", "parent"))));
//	}
//	
//	@Test
//	public void inAFollowingAxis() {
//		new AxisTest().runAxis("AFollowingAxis")
//			.expect().context("struct", "text_ref").op("=").target("struct", "text_ref")
//			.expect().context("struct", "right").op("<").target("struct", "right")
//			.test();
//	}
//	
//	@Test
//	public void inAContainingAxis() {
//		new AxisTest().runAxis("AContainingAxis")
//			.expect().context("struct", "text_ref").op("=").target("struct", "text_ref")
//			.expect().context("struct", "left").op(">=").target("struct", "left")
//			.expect().context("struct", "right").op("<=").target("struct", "right")
//			.test();
//	}
//	
//	@Test
//	public void inAContainedAxis() {
//		new AxisTest().runAxis("AContainedAxis")
//			.expect().context("struct", "text_ref").op("=").target("struct", "text_ref")
//			.expect().context("struct", "left").op("<=").target("struct", "left")
//			.expect().context("struct", "right").op(">=").target("struct", "right")
//			.test();
//	}
//	
//	
//	@Test
//	public void inAAttributeAxis() {
//		AliasSet context = path.newAliasSet();
//		AliasSet target = path.newAliasSet();
//
//		translator.inAAttributeAxis(null);
//		
//		assertThat(path.getConditions(),
//				containsItem(Join.eq(context.getColumn("rank", "pre"), target.getColumn("rank", "pre"))));
//	}
//	
//	///// Knotentests /////
//	
//	/**
//	 * Preconditions:
//	 * - a target alias set
//	 * Result:
//	 * - a join: struct2.name = 'foo'
//	 */
////	@Test
////	public void inAKindNodeTestTypeElementNamed() {
////		AliasSet target = path.newAliasSet();
////		
////		AKindNodeTest node = b.newKindNodeTest(b.newElementNodeType(), "foo");
////		translator.inAKindNodeTest(node);
////		
////		assertThat(target, sameInstance(path.getTargetAliasSet()));
////		assertThat(path.getConditions(), containsItem(Join.eq(target.getColumn("struct", "name"), "'foo'")));
////	}
////	
////	/**
////	 * - element test with no name creates no condition
////	 */
////	@Test
////	public void inAKindNodeTestTypeElementNoName() {
////		AKindNodeTest node = b.newKindNodeTest(b.newElementNodeType(), null);
////		translator.inAKindNodeTest(node);
////		
////		assertThat(path.getConditions(), empty());
////	}
////	
////	/**
////	 * Precondition:
////	 * - alias set
////	 * Results:
////	 * - join: anno_ttribute1.name = 'foo'
////	 */
////	@Test
////	public void inAKindNodeTestTypeAttribute() {
////		AliasSet target = path.newAliasSet();
////		
////		AKindNodeTest node = b.newKindNodeTest(b.newAttributeNodeType(), "foo");
////		translator.inAKindNodeTest(node);
////		
////		assertThat(target, sameInstance(path.getTargetAliasSet()));
////		assertThat(path.getConditions(), containsItem(Join.eq(target.getColumn("anno_attribute", "name"), "'foo'")));
////	}
////	
////	@Test
////	public void inAKindNodeTestTypeAttributeNoName() {
////		AliasSet target = path.newAliasSet();
////		
////		AKindNodeTest node = b.newKindNodeTest(b.newAttributeNodeType(), null);
////		translator.inAKindNodeTest(node);
////		
////		assertThat(target.usesTable("anno"), is(true));
////	}
//	
//	/// wenn die tabelle anno_attribute verwendet wird, wird auch anno benötigt (outAStep)
//	@Test
//	public void outAStepAnnoAttributeJoin() {
//		AliasSet target = path.newAliasSet();
//		target.useTable("anno_attribute");
//		
//		translator.outAStep(b.newStep(null, null));
//		
//		assertThat(target.usesTable("anno"), is(true));
//	}
//	
//	/// wenn die tabelle anno verwendet wird, wird auch struct benötigt (outAStep)
//	@Test
//	public void outAStepAnnoJoin() {
//		AliasSet target = path.newAliasSet();
//		target.useTable("anno");
//		
//		translator.outAStep(b.newStep(null, null));
//		
//		assertThat(target.usesTable("struct"), is(true));
//	}
//	
//	// durch teilschritte des pfades ausgezeichnetete knotenmengen können markiert sein (inAMarkerSpec)
//	@Test
//	public void inAMarkerSpec() {
//		AliasSet target = path.newAliasSet();
//		
//		AMarkerSpec node = b.newMarkerSpec("marker");
//		translator.inAMarkerSpec(node);
//		
//		assertThat(target, sameInstance(path.getTargetAliasSet()));
//		assertThat(path.getMarkings(), hasEntry(target, "marker"));
//	}
//	
//	// der textwert eines knoten ist der element-span, es sei denn der pfad endet auf der achse attribute (inAStep)
//	@Test
//	public void outAStepTextValueElement() {
//		parse("/child::element()").apply(translator);
//		assertThat(path.getTextValue(), is(TextValue.Element));
//	}
//		
//	// der textwert eines knoten ist der element-span, es sei denn der pfad endet auf der achse attribute (inAStep)
//	@Test
//	public void outAStepTextValueChained() {
//		parse("/child::element()/attribute::attribute()").apply(translator);
//		assertThat(path.getTextValue(), is(TextValue.Attribute));
//	}
//	
//	// der textwert eines knoten ist der element-span, es sei denn der pfad endet auf der achse attribute (inAStep)
//	@Test
//	public void outAStepTextValueNoPredicates() {
//		parse("/child::element()[attribute::attribute()]").apply(translator);
//		assertThat(path.getTextValue(), is(TextValue.Element));
//	}
//	
//	public void outAStepTextValueContext() {
//		parse(".").apply(translator);
//		assertThat(path.getTextValue(), is(TextValue.Element));
//	}
//	
//	@Test
//	public void caseAComparisonExpr() {
//		PathQueryTranslator translator = new PathQueryTranslator(path) {
//
//			int i = 0;
//			String[] values = { "lhs", "rhs" };
//
//			@Override
//			public JoinField textValue(Node node) {
//				return new Literal(values[i++ % 2]);
//			}
//			
//			@Override
//			public void inAEqComparison(AEqComparison node) {
//				setComparison("joinOp");
//			}
//		};
//		translator.caseAComparisonExpr(b.newComparisonExpr(b.newEqComparison(), null, null));
//		assertThat(path.getConditions(), containsItem(new Join("joinOp", "lhs", "rhs")));
//	}
//	
//	@Test
//	public void inAEqComparison() {
//		translator.inAEqComparison(null);
//		assertThat(translator.getComparison(), is("="));
//	}
//
//	@Test
//	public void inANeComparison() {
//		translator.inANeComparison(null);
//		assertThat(translator.getComparison(), is("!="));
//	}
//
//	@Test
//	public void inALtComparison() {
//		translator.inALtComparison(null);
//		assertThat(translator.getComparison(), is("<"));
//	}
//
//	@Test
//	public void inALeComparison() {
//		translator.inALeComparison(null);
//		assertThat(translator.getComparison(), is("<="));
//	}
//
//	@Test
//	public void inAGtComparison() {
//		translator.inAGtComparison(null);
//		assertThat(translator.getComparison(), is(">"));
//	}
//
//	@Test
//	public void inAGeComparison() {
//		translator.inAGeComparison(null);
//		assertThat(translator.getComparison(), is(">="));
//	}
//	
//	@Test
//	public void textValueStringLiteral() {
//		AStringLiteralExpr node = b.newStringLiteralExpr("foo");
//		assertThat(translator.textValue(node).toString(), is("'foo'"));
//	}
//	
//	@Test
//	public void textValueStringLiteralEmpty() {
//		AStringLiteralExpr node = b.newStringLiteralExpr(null);
//		assertThat(translator.textValue(node).toString(), is("''"));
//	}
//	
//	@Test
//	public void textValueNumberLiteral() {
//		ANumberLiteralExpr node = b.newNumberLiteralExpr(1);
//		assertThat(translator.textValue(node).toString(), is("1"));
//	}
//	
//	@Test
//	public void textValuePathElement() {
//		APathExpr node = (APathExpr) lookup("child::element()", APathExpr.class);
//		
//		JoinField textValue = translator.textValue(node);
//		assertThat(textValue.toString(), is("struct2.span"));
//	
//		assertThat(path.getConditions(), containsItem(Join.eq("rank1.pre", "rank2.parent")));
//	}
//	
//	@Test
//	public void textValuePathAttribute() {
//		APathExpr node = (APathExpr) lookup("attribute::attribute(foo)", APathExpr.class);
//		
//		assertThat(translator.textValue(node).toString(), is("anno_attribute2.value"));
//		
//		assertThat(path.getConditions(), containsItem(Join.eq("anno_attribute2.name", "'foo'")));
//	}	
//
//	@Test
//	public void inAStepNewVariable() {
//		parse("/child::element()$var").apply(translator);
//		assertThat(translator.getVariables(), hasEntry("var", path.getTargetAliasSet()));
//	}
//	
//	@Test
//	public void inAStepKnownVariable() {
//		AliasSet target = new AliasSet(42);
//		translator.getVariables().put("var", target);
//		parse("/child::element()$var").apply(translator);
//		assertThat(path.getTargetAliasSet(), is(target));
//	}
//	
//	@Test
//	public void inAStepStartsWithVariable() {
//		AliasSet target = new AliasSet(42);
//		translator.getVariables().put("var", target);
//		parse("$var/child::element()").apply(translator);
//		assertThat(path.getContextAliasSet(), is(target));
//	}
//
//	/** */
////	@Test
//	public void example() throws ParserException, LexerException, IOException {
//		String input = "( ( *#(a1)[@sentence = 'S']$a1 ) & ( *#(a2)[@description = 'sentence']$a2 ) ) & ( ( $a1/containing::element()$a2 & $a1/contained::element()$a2 ) )";
////		String input = "( ( *#(a1)[@sentence = 'S']$a1 ) & ( *#(a2)[@description = 'sentence']$a2 ) ) & ( ( $a1/containing::element()$a2 & $a1/contained::element()$a2 ) )";
////		String input = "( ( *#(a1)[@sentence = 'S']$a1//TOKEN#(t1)$t1 ) & ( *#(a2)[@description = 'sentence']$a2//TOKEN#(t2)$t2 ) ) & ( ( $a1/containing::element()$a2 & $a1/contained::element()$a2 ) )";
////		String input = "*#(a1)[@sentence = 'S'][. = 'Hello world']$a1//TOKEN#(t1)$t1";
////		String input = "*#(a1)[@sentence = 'S']$a1/element-span::\"Hello World\"//TOKEN#(t1)$t1";
//// 		String input = "*#(a1)[@sentence = 'S']$a1//TOKEN#(t1)$t1";
////		String input = "child::element()#[attribute::attribute(description)]";
////		String input = "$v/$v/child::$v#";
////		String input = "/child::element()[/child::element()#(b)]";
////		String input = "child::element(start)#(a)[child::element(e)[attribute::attribute(a) = \"a\"] = \"e\"]";
////		String input = "child::element()#(a)[attribute::attribute(description)][attribute::attribute(sentence)]";
////		String input = "child::element()#(a)[attribute::attribute(description) = \"sentence\"][attribute::attribute(sentence) = \"S\"]";
////		String input = "child::element()#(a)[child::element() = \"Hello\"][child::element() = \"world\"]";
////		String input = "child::element()#(a)[child::element(bar)]/child::element(boink)/attribute::attribute(foo)";
//		Start start = parse(input);
//		PathQuery query = new PathQuery();
//		PathQueryTranslator translator = new PathQueryTranslator(query);
//		start.apply(translator);
//		System.out.println(translator.getPathQuery());
//	}
//	
//	private Start parse(String input) {
//		try {
//			Start start = (new Parser(new Lexer(new PushbackReader(new StringReader(input))))).parse();
//			start.apply(new SemanticAnalysis());
//			return start;
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
}
