//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

var div = this.getElement(); 

var containerWidth = $(div).parent().width();
var containerHeight =  $(div).parent().height();
var container = div;
var minHeight = containerHeight;
//window.alert(minHeight);

$(container).css("width", containerWidth, "height", containerHeight);

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
var nNodes = json_nodes.length;
if (nNodes < 20){
	nodeDist = 100;
} else if (nNodes >=20 && nNodes < 100){
	nodeDist = 150
} else if (nNodes >= 100 && nNodes < 400) {
	nodeDist = 200
} else {
	nodeDist = 400
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
layout: {
hierarchical:{
}
},
physics: {
hierarchicalRepulsion: {
centralGravity: 0.05,
springLength: 100,
springConstant: 0.0007,
nodeDistance: nodeDist,
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
/*var canvasWidth = $(".vis-network canvas:first-child").width();
window.alert(canvasWidth);
var networkWidth = $(".vis-network").width();
window.alert(networkWidth);*/


visjscomponent.on("zoom", function (params) {
		var canvasWidth = $(".vis-network canvas:first-child").width();
		var canvasHeight = $(".vis-network canvas:first-child").height();
		var minContainerWidth = $(div).parent().width();

       var zoomParams = JSON.stringify(params, null, 4);
      // window.alert(zoomParams);

       var zoomValues = JSON.parse(zoomParams);

       var direction = zoomValues.direction;
       var scale = zoomValues.scale;
       if (direction === '+') {
       	 visjscomponent.setSize(canvasWidth*(1+scale), canvasHeight*(1+scale));
       }
       else if  (direction === '-'){
       	 visjscomponent.setSize(canvasWidth*(1-scale), canvasHeight*(1-scale));
 
       }
       
		
		var newCanvasWidth = $(".vis-network canvas:first-child").width();
		var newCanvasHeight = $(".vis-network canvas:first-child").height();
		//window.alert(newCanvasHeight);

		$(container).css({"width":Math.max(newCanvasWidth, minContainerWidth), "height": Math.max(newCanvasHeight, minHeight)});


    });

};



window.addEventListener("resize", function(){
 	containerWidth = $(div).parent().width();
 	minContainerWidth = containerWidth;
	//containerHeight =  $(div).parent().height();
	$(container).css("width", containerWidth);

	
}); 


};
