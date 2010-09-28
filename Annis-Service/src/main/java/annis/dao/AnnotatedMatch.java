package annis.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class AnnotatedMatch extends ArrayList<AnnotatedSpan> {

	public AnnotatedMatch() {
		super();
	}
	
	public AnnotatedMatch(List<AnnotatedSpan> spans) {
		this();
		addAll(spans);
	}
	
	public AnnotatedMatch(AnnotatedSpan... spans) {
		this(Arrays.asList(spans));
	}
	
}
