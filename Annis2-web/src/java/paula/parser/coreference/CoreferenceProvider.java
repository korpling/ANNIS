package paula.parser.coreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

public abstract class CoreferenceProvider {

	public abstract void feedNode(Node node);

	public abstract List<Token> getTokenList();

	public abstract Map<String, String> getReferenceMap();

	public abstract Set<Reference> getReferenceSet();

	public class Reference {
		public String text = "";
		public List<String> tokenIdList = new ArrayList<String>();
		public String id;
		public String refersTo = "";
		public String type = "";
	}
	
	public class Token {
		public String text = "";
		public String id;
		public String type;
	}
}