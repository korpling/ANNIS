package de.deutschdiachrondigital.dddquery.sql.model;

public class Literal implements JoinField {

	private String string;
	
	public Literal(String string) {
		this.string = string;
	}
	
	@Override
	public String toString() {
		return string;
	}

}
