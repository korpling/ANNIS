package annis.model;

import java.util.HashSet;
import java.util.Set;

public class Edge extends DataObject {

	// this class is sent to the front end
	private static final long serialVersionUID = 3127054835526596882L;

	public enum EdgeType { 
		COVERAGE			("c", "Coverage"),
		DOMINANCE			("d", "Dominance"), 
		POINTING_RELATION	("p", "Pointing Relation"), 
		UNKNOWN				(null, "UnknownEdgeType");
	
		private String type;
		private String name;
		
		private EdgeType(String type, String name) {
			this.type = type;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name + (type != null ? "(" + type + ")" : "");
		}
		
		public String getTypeChar() {
			return type;
		}
		
		// FIXME: test parseEdgeType()
		public static EdgeType parseEdgeType(String type) {
			if ("c".equals(type))
				return COVERAGE;
			else if ("d".equals(type))
				return DOMINANCE;
			else if ("p".equals(type))
				return POINTING_RELATION;
			else
				return UNKNOWN;
		}
	
	};
	
	private AnnisNode source;
	private AnnisNode destination;
	
	private long pre;
	private long post;
	private long component;
	private EdgeType edgeType;
	private String namespace;
	private String name;
	private long level;
	private Set<Annotation> annotations;

	public Edge() {
		annotations = new HashSet<Annotation>();
		edgeType = EdgeType.UNKNOWN;
	}
	
	public boolean addAnnotation(Annotation o) {
		return annotations.add(o);
	}
	
	@Override
	public String toString() {
		Long src = source != null ? source.getId() : null;
		long dst = destination.getId();
		String type = edgeType != null ? edgeType.toString() : null;
		String name = getQualifiedName();
		return src + "->" + dst + " " + name + " " + type;

//		return ":source -> :destination (:pre, :post) :type (:component, :name) level :level"
//		.replace(":source", source != null ? "node " + String.valueOf(source.getId()) : "NULL")
//			.replace(":source", source != null ? "node " + String.valueOf(source.getId()) : "NULL")
//			.replace(":destination", destination != null ? "node " + String.valueOf(destination.getId()) : "NULL")
//			.replace(":pre", String.valueOf(pre))
//			.replace(":post", String.valueOf(post))
//			.replace(":type", edgeType.toString())
//			.replace(":component", String.valueOf(component))
//			.replace(":name", getQualifiedName())
//			.replace(":level", String.valueOf(level));
	}

	public String getQualifiedName() {
		return AnnisNode.qName(namespace, name);
	}
	
//	// custom hashCode function to break object cycle that causes StackOverFlow on DataObject.hashCode
//	@Override
//	public int hashCode() {
//		return new HashCodeBuilder()
//			.append(pre).append(post).append(component).append(edgeType)
//			.append(getQualifiedName()).append(level).append(annotations)
//			.toHashCode();
//	}
	
	///// Getters / Setters
	
	public long getPre() {
		return pre;
	}

	public void setPre(long pre) {
		this.pre = pre;
	}
	
	public long getPost() {
		return post;
	}
	
	public void setPost(long post) {
		this.post = post;
	}
	
	public long getComponent() {
		return component;
	}
	
	public void setComponent(long component) {
		this.component = component;
	}
	
	public EdgeType getEdgeType() {
		return edgeType;
	}
	
	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getLevel() {
		return level;
	}
	
	public void setLevel(long level) {
		this.level = level;
	}

	public Set<Annotation> getAnnotations() {
		return annotations;
	}


	public AnnisNode getSource() {
		return source;
	}

	public void setSource(AnnisNode source) {
		this.source = source;
	}

	public AnnisNode getDestination() {
		return destination;
	}

	public void setDestination(AnnisNode destination) {
		this.destination = destination;
	}


}

