package annis.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;

import annis.AnnisHomeTest;

@SuppressWarnings({ "unused", "serial" })
public class TestDataObject extends AnnisHomeTest {

	static class A extends DataObject {

		private String s;
		
		public A(String s) {
			this.s = s;
		}

	}
	
	static class B extends DataObject {
		
		private String s;
		private Collection<String> c;
		
		public B(String s, Collection<String> c) {
			this.s = s;
			this.c = c;
		}

	}
	
	static class C extends DataObject {
		private A a;

		public C(A a) {
			super();
			this.a = a;
		}
	}
	
	private static final String S1 = "S1";
	private static final String S2 = "S2";
	private static final Collection<String> C1 = Arrays.asList(S1);
	private static final Collection<String> C2 = Arrays.asList(S2);
	
	@Before
	public void setup() {
		// sanity checks
		assertThat("bad example data", S1, is(not(S2)));
		assertThat("bad example data", C1, is(not(C2)));
	}
	
	@Test
	public void equalsTrueA() {
		A a1 = new A(S1);
		A a2 = new A(S1);
		assertThat(a1, is(a2));
	}
	
	@Test
	public void equalsFalseA() {
		A a1 = new A(S1);
		A a2 = new A(S2);
		assertThat(a1, is(not(a2)));
	}
	
	@Test
	public void equalsTrueB() {
		B b1 = new B(S1, C1);
		B b2 = new B(S1, C1);
		assertThat(b1, is(b2));
	}
	
	@Test
	public void equalsFalseBWrongString() {
		B b1 = new B(S1, C1);
		B b2 = new B(S2, C1);
		assertThat(b1, is(not(b2)));
	}
	
	@Test
	public void equalsFalseBWrongCollection() {
		B b1 = new B(S1, C1);
		B b2 = new B(S1, C2);
		assertThat(b1, is(not(b2)));
	}
	
	@Test
	public void equalsFalseWrongType() {
		A a = new A(S1);
		B b = new B(S1, C1);
		assertFalse(a.equals(b));
	}
	
	@Test
	public void equalsFalseThisNull() {
		A a = new A(S1);
		C c1 = new C(null);
		C c2 = new C(a);
		assertFalse(c1.equals(c2));
	}
	
	// enforce equals contract: x.equals(null) == false for any x
	@Test
	public void equalsFalseOtherNull() {
		A a = new A(S1);
		C c1 = new C(a);
		C c2 = new C(null);
		assertFalse(c1.equals(c2));
	}
	
	@Test
	public void hashCodeA() {
		A a = new A(S1);
		int expected = new HashCodeBuilder().append(S1).toHashCode();
		assertThat(a.hashCode(), is(expected));
	}
	
	@Test
	public void hashCodeB() {
		B b = new B(S1, C1);
		// order of the field values is important, fields are sorted by name
		// first comes i=I1, then comes s=S1
		int expected = new HashCodeBuilder().append(C1).append(S1).toHashCode();
		assertThat(b.hashCode(), is(expected));
	}
	
}
