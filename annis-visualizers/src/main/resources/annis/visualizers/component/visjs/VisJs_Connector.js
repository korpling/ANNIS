//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

var div = this.getElement(); 
var container = div;

var self = this;
var strNodes =  this.getState().strNodes;
var strEdges = this.getState().strEdges;
var visjscomponent = null;


// Handle changes from the server-side
this.onStateChange = function(){
		
	// cleanup old graph
    if(typeof visjscomponent !== 'undefined' && visjscomponent != null){
    	visjscomponent.destroy();
    }
	
    strNodes = this.getState().strNodes;
    strEdges = this.getState().strEdges;
    this.init(strNodes, strEdges);
    };


this.init = function(strNodes, strEdges){
container.style.border = "thin solid green";

var json_nodes = JSON.parse(strNodes);
var json_edges = JSON.parse(strEdges);

var data = {
nodes: json_nodes,
edges: json_edges
};
var options = {
nodes:{
shape: "box"
},
edges: {
smooth: true,
arrows: {
to: {
enabled: true
}
}
},
layout: {
hierarchical:{
//direction: directionInput.value
}
},
physics: {
hierarchicalRepulsion: {
centralGravity: 0.05,
springLength: 100,
springConstant: 0.0007,
nodeDistance: 200,
damping: 0.04
},
maxVelocity: 27,
solver: 'hierarchicalRepulsion',
timestep: 0.5,
stabilization: {
iterations: 800
}
}
}
;
$(container).remove("canvas");
visjscomponent = new vis.Network(container, data, options); 

};

   
/*$(window).resize(function() {
    self.init(strNodes, strEdges);
  });*/
  
};
