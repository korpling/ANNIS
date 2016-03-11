//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

var div = this.getElement(); 

var containerWidth = $(div).parent().width();
var containerHeight =  $(div).parent().height();

var parentWidthOld = containerWidth;
var parentHeightOld = containerHeight;
var parentWidthNew = parentWidthOld;
var parentHeightNew = parentHeightOld;


var container = div;
var minHeight = containerHeight;

$(container).css({"width": containerWidth, "height": containerHeight});

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

var initCanvasWidth = $(".vis-network canvas:first-child").width();
var initCanvasHeight = $(".vis-network canvas:first-child").height();


visjscomponent.on("zoom", function (params) {
		//window.alert(JSON.stringify(visjscomponent.getScale(), null, 4));
	
		var canvasWidth = $(".vis-network canvas:first-child").width();
		var canvasHeight = $(".vis-network canvas:first-child").height();
		var minContainerWidth = $(div).parent().width();

       var zoomParams = JSON.stringify(params, null, 4);
      // window.alert(zoomParams);


       var zoomValues = JSON.parse(zoomParams);

       var direction = zoomValues.direction;
       var scale = zoomValues.scale;
       if (direction === '+' && scale <= 1) {
       	 visjscomponent.setSize(canvasWidth*(1+scale), canvasHeight*(1+scale));
       //	 visjscomponent.setSize(initCanvasWidth*(scale*10), initCanvasHeight*(scale*10));
       }
       else if  (direction === '-'){
	       	if (canvasWidth*(1-scale) < minContainerWidth ||  canvasHeight*(1-scale) < minHeight){
	       	//if (initCanvasWidth*(scale*10) < minContainerWidth ||  initCanvasHeight*(scale*10) < minHeight){
	       			       	
	       		//prevent zoom out of graph to the size smaller then the container size 
	       		visjscomponent.setSize(minContainerWidth, minHeight);
	       		
	       	}
	       	else{
	       		visjscomponent.setSize(canvasWidth*(1-scale), canvasHeight*(1-scale));
	       		//visjscomponent.setSize(initCanvasWidth*(scale*10), initCanvasHeight*(scale*10));
	       	}     	 
 
 
       }
       		
		var newCanvasWidth = $(".vis-network canvas:first-child").width();
		var newCanvasHeight = $(".vis-network canvas:first-child").height();
		
		$(container).css({"width": Math.max(newCanvasWidth, minContainerWidth), "height": Math.max(newCanvasHeight, minHeight)});
		visjscomponent.fit();
		/*visjscomponent.moveTo({
					  position: {x:100, y:100}
					  });*/

    });

};



window.addEventListener("resize", function(){
	parentWidthNew = $(div).parent().width();
	parentHeightNew = $(div).parent().height();

	if (parentWidthNew > parentWidthOld || parentHeightNew > parentWidthOld){
		containerWidth = Math.max(parentWidthNew, $(container).width());
		containerHeight =  Math.max(parentHeightNew, $(container).height());
	}
	else{
		containerWidth = Math.min(parentWidthNew, $(container).width());
		containerHeight =  Math.min(parentHeightNew, $(container).height());
	}

 	minContainerWidth = containerWidth;	

	$(container).css({"width": containerWidth, "height": containerHeight});
	visjscomponent.setSize(containerWidth, containerHeight);
	visjscomponent.fit();

	
	parentWidthOld = parentWidthNew;
	parentHeightOld = parentHeightNew;
}); 


};
