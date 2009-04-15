

public class A {

	private B b;

	@Override
	public String toString() {
		return "a=" + super.toString() + "; " + b.toString();
	}
	
	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}
	
}
