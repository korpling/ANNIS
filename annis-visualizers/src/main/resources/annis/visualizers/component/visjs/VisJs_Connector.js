//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

this.init = function(visId, strNodes, strEdges){

var e = this.getElement();
e.innerHTML = "<div id=visId>container</div>";
var container = document.getElementById(visId);
//var container = e;
container.style.border = "thin solid green";

var nodes = JSON.parse(strNodes);
var edges = JSON.parse(strEdges);
var visjscomponent = null;

var data = {
nodes: nodes,
edges: edges
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

var visjscomponent = new vis.Network(container, data, options); 

}


// Handle changes from the server-side
this.onStateChange = function(){
	
	var visId = this.getState().visId;
    var nodes = this.getState().strNodes;
    var edges = this.getState().strEdges;
    this.init(visId, nodes, edges);
    }
   
  
};
