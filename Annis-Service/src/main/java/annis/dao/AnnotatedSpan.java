package annis.dao;

import java.util.List;

import annis.model.Annotation;

public class AnnotatedSpan {

	private long id;
	private String coveredText;
	private List<Annotation> annotations;

	public AnnotatedSpan(long id, String coveredText, List<Annotation> annotations) {
		super();
		this.id = id;
		this.coveredText = coveredText;
		this.annotations = annotations;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getCoveredText() {
		return coveredText;
	}
	
	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}
	
}
