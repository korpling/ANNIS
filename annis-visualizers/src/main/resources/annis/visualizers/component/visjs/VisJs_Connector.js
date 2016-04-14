//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

var div = this.getElement(); 

var containerWidth = $(div).parent().width();
var containerHeight =  $(div).parent().height();

var container = div;


//$(container).css({"width": containerWidth, "height": containerHeight});

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

var json_nodes = JSON.parse(strNodes);
var json_edges = JSON.parse(strEdges);


var nodeDist;
var sprLength;
var sprConstant;

var nNodes = json_nodes.length;
if (nNodes < 20){
	nodeDist = 100;
	sprLength = 200;
	sprConstant = 1.15;
} else if (nNodes >=20 && nNodes < 100){
	nodeDist = 150;
	sprLength = 160;
	sprConstant = 0.55;

} else if (nNodes >= 100 && nNodes < 400) {
	nodeDist = 200
	sprLength = 140;
	sprConstant = 0.45;
} else {
	nodeDist = 400
	sprLength = 100;
	sprConstant = 0.4;
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
centralGravity: 0.0,
springLength: sprLength,
springConstant: sprConstant,
nodeDistance: nodeDist,
damping: 0.04
},
maxVelocity: 50,
minVelocity: 7,
solver: 'hierarchicalRepulsion',
timestep: 0.4,
stabilization: {
iterations: 700
}
}
}
;
$(container).remove("canvas");



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
