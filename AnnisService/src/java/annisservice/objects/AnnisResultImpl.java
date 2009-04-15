package annisservice.objects;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import annisservice.ifaces.AnnisResult;
import annisservice.ifaces.AnnisToken;


public class AnnisResultImpl implements AnnisResult {
	private static final long serialVersionUID = 1648848837712346094L;

	String key;
	String paula;
	LinkedList<AnnisToken> tokenList;
	Map<Long, String> marker;
	Long textId;
	long corpusId;
	
	Set<String> tokenAnnotationLevelSet;
	Set<String> nonTokenAnnotationLevelSet;
	
	public AnnisResultImpl() {
		tokenList = new LinkedList<AnnisToken>();
		marker = new HashMap<Long, String>();
	}
	
	public void addToken(AnnisToken annisToken) {
		tokenList.add(annisToken);
	}

	public long getCorpusId() {
		return corpusId;
	}

	public String getMarkerId(Long nodeId) {
		return marker.get(nodeId);
	}

	public Long getNodeId(String markerId) {
		for (Entry<Long, String> entry : marker.entrySet())
			if (markerId.equals(entry.getValue()))
				return entry.getKey();
		return null;
	}

	public boolean hasMarker(String markerId) {
		return marker.containsValue(markerId);
	}

	public boolean hasNodeMarker(Long nodeId) {
		return marker.containsKey(nodeId);
	}

	public String getJSON() {
		StringBuffer json = new StringBuffer();
//		String k = getKey().replace("{", "").replace("}", "");
		String k = getStartNodeId() + "," + getEndNodeId();
		json.append("{\"_id\":\"" + k + "\", \"_textId\": \"" + this.textId + "\", \"_text\":\"" + this.getText().replace("\"", "\\\"") + "\"");

		//add annotation levels
		json.append(", \"_levels\": [");
		int c = 0;
		for(String level : this.getAnnotationLevelSet())
			json.append(((c++ > 0) ? ", " : "") + "\"" + level + "\"");
		json.append("]");

		//add a list of marked objects
		json.append(", \"_markedObjects\": [");
		c=0;
		for(Long id : this.marker.keySet()) {
			if(c++>0)
				json.append(", ");
			json.append(id);
		}
		json.append("]");

		//add token annotation levels
		json.append(", \"_tokenLevels\": [");
		c = 0;
		for(String level : this.getTokenAnnotationLevelSet())
			json.append(((c++ > 0) ? ", " : "") + "\"" + level + "\"");
		json.append("]");

		int tokenCount = 0;

		List<AnnisToken> tokenList = this.getTokenList();
		int matchStart = tokenList.size() - 1, matchEnd = 0;
		for(AnnisToken token : tokenList) {
			if(hasNodeMarker(token.getId())) {
				if(tokenCount > matchEnd)
					matchEnd = tokenCount;
				if(tokenCount < matchStart)
					matchStart = tokenCount;
			}
			String marker = hasNodeMarker(token.getId()) ? getMarkerId(token.getId()) : "";
			json.append(",\"" + tokenCount++ + "\":{\"_id\": " + token.getId() + ", \"_text\":\"" + ( token.getText() != null ? token.getText().replace("\"", "\\\"") : "" ) + "\", \"_marker\":\"" + marker + "\"");
			for(Map.Entry<String,String> annotation : token.entrySet())
				json.append(", \"" + annotation.getKey() + "\":\"" + annotation.getValue().replace("\"", "\\\"") + "\"");
			json.append("}");
		}
		json.append(", \"_matchStart\" : \"" + matchStart + "\"");
		json.append(", \"_matchEnd\" : \"" + matchEnd + "\"");
		json.append(", \"_corpusId\" : \"" + corpusId + "\"");
		json.append("}");
		return json.toString();
	}
	public long getStartNodeId() {
		return tokenList.getFirst().getId(); // FIXME: Sortierung?
	}

	public long getEndNodeId() {
		return tokenList.getLast().getId();
	}

	public List<AnnisToken> getTokenList() {
		// FIXME: test
		Collections.sort(tokenList, new Comparator<AnnisToken>() {

			public int compare(AnnisToken o1, AnnisToken o2) {
				return Long.signum(o1.getTokenIndex() - o2.getTokenIndex());
			}
			
		});
		return tokenList;
	}

	public void setTokenList(LinkedList<AnnisToken> tokenList) {
		this.tokenList = tokenList;
	}

	public Map<Long, String> getMarker() {
		return marker;
	}

	public void setMarker(Map<Long, String> marker) {
		this.marker = marker;
	}

	public long getTextId() {
		return textId;
	}

	public void setTextId(long textId) {
		this.textId = textId;
	}

	public String getPaula() {
		return paula;
	}

	public void setPaula(String paula) {
		this.paula = paula;
	}
	
	public void setAnnotationLevelSet(Set<String> nonTokenAnnotationLevelSet) {
		this.nonTokenAnnotationLevelSet = nonTokenAnnotationLevelSet;
	}
	
	public Set<String> getAnnotationLevelSet() {
		return nonTokenAnnotationLevelSet;
	}
	
	public void setTokenAnnotationLevelSet(Set<String> tokenAnnotationLevelSet) {
		this.tokenAnnotationLevelSet = tokenAnnotationLevelSet;
	}
	
	public Set<String> getTokenAnnotationLevelSet() {
		return tokenAnnotationLevelSet;
	}
	
	
	public void putMarker(String markerId, Long nodeId) {
		// TODO Auto-generated method stub
		
	}

	public void setCorpusId(long corpusId) {
		this.corpusId = corpusId;
	}

	public void setEndNodeId(long endNodeId) {
		// TODO Auto-generated method stub
		
	}

	public void setStartNodeId(long startNodeId) {
		// TODO Auto-generated method stub
		
	}

	private String getText() {
		StringBuffer out = new StringBuffer();
		for(AnnisToken t : getTokenList())
			out.append(t.getText() + " ");
		return out.toString();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
