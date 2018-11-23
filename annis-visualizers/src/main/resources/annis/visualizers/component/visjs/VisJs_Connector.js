//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

var container = this.getElement(); 

$(container).css({"width": "100%", "height": "100%"});
var	containerWidth = $(container).width();
var	containerHeight =  $(container).height();


container.style.border = "thin solid light blue";
container.style.display = "inline-block";


var strNodes =  this.getState().strNodes;
var strEdges = this.getState().strEdges;
var visjscomponent = null;


// handle changes from the server-side
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

var json_nodes = JSON.parse(strNodes);
var json_edges = JSON.parse(strEdges);


var nodeDist;
var sprLength;
var sprConstant;

var nNodes = json_nodes.length;


if (nNodes < 20){
	nodeDist = 120;
	sprConstant = 1.2;
	sprLength = 120;
	} else if (nNodes >=20 && nNodes < 100){
	nodeDist = 150;
	sprConstant = 1.3;
	sprLength = 250;
	} else if (nNodes >= 100 && nNodes < 400) {
	nodeDist = 180;
	sprConstant = 0.9;
	sprLength = 180;
	} else if (nNodes >= 400 && nNodes < 800) {
		nodeDist = 200;
		sprConstant = 0.6;
		sprLength = 200;
	} else {
		nodeDist = 250;
		sprConstant = 0.3;
		sprLength = 230;
};



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
interaction: {
  navigationButtons: true,
  keyboard: true
        },
layout: {
hierarchical:{
     direction: 'UD',
     sortMethod: 'directed'
}
} ,
physics: {
hierarchicalRepulsion: {
centralGravity: 0.8,
springLength: sprLength,
springConstant: sprConstant,
nodeDistance: nodeDist,
damping: 0.2
},
maxVelocity: 50,
minVelocity: 1,
solver: 'hierarchicalRepulsion',
timestep: 0.5,
stabilization: {
iterations: 1000
}
}
}
;


visjscomponent = new vis.Network(container, data, options); 



$("div.vis-network div.vis-navigation div.vis-button.vis-up").css({"bottom": "50px", "left": "auto", "right": "55px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-down").css({"bottom": "10px", "left": "auto", "right": "55px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-left").css({"bottom": "10px", "left": "auto", "right": "95px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-right").css({"bottom": "10px", "left": "auto", "right": "15px"});

$("div.vis-network div.vis-navigation div.vis-button.vis-zoomIn").css({"bottom": "auto", "top": "10px", "left": "auto", "right": "15px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-zoomOut").css({"bottom": "auto", "top": "50px", "left": "auto", "right": "15px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-zoomExtends").css({"bottom": "auto", "top": "10px", "left": "auto", "right": "55px"});

  
};


window.addEventListener("resize", function(){
	$(container).css({"width": "100%", "height": "100%"});
	containerWidth = $(container).width();
	containerHeight =  $(container).height();
	visjscomponent.setSize(containerWidth, containerHeight);
	visjscomponent.fit();


}); 



};
