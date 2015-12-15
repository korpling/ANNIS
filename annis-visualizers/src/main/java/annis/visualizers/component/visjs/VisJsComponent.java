package annis.visualizers.component.visjs;



import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

import annis.libgui.visualizers.VisualizerInput;







import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

@JavaScript(
		  {
		    "vaadin://jquery.js",
		    "visjs/vis.min.js"
		  })
@StyleSheet(
  {
	  	"visjs/vis.min.css"
  })


public class VisJsComponent extends AbstractJavaScriptComponent
implements GraphTraverseHandler{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public VisJsComponent(VisualizerInput visInput){
		
		
		
	
	}


	@Override
	public boolean checkConstraint(GRAPH_TRAVERSE_TYPE arg0, String arg1,
			SRelation<SNode, SNode> arg2, SNode arg3, long arg4) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void nodeLeft(GRAPH_TRAVERSE_TYPE arg0, String arg1, SNode arg2,
			SRelation<SNode, SNode> arg3, SNode arg4, long arg5) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void nodeReached(GRAPH_TRAVERSE_TYPE arg0, String arg1, SNode arg2,
			SRelation<SNode, SNode> arg3, SNode arg4, long arg5) {
		// TODO Auto-generated method stub
		
	}

	
}
