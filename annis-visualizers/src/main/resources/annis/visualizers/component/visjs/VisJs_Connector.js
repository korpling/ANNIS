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
var sprLength;
var sprConstant;

var nNodes = json_nodes.length;
if (nNodes < 20){
	nodeDist = 100;
	sprLength = 200;
	sprConstant = 1.2;
} else if (nNodes >=20 && nNodes < 100){
	nodeDist = 150;
	sprLength = 200;
	sprConstant = 1.2;

} else if (nNodes >= 100 && nNodes < 400) {
	nodeDist = 200
	sprLength = 250;
	sprConstant = 0.8;
} else {
	nodeDist = 400
	sprLength = 250;
	sprConstant = 0.5;
};


var data = {
nodes: json_nodes,
edges: json_edges
};
var options = {
nodes:{
shape: "box" //,
/*fixed: {
	x: true
}*/
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



//var spinner = "<i class='fa fa-spinner fa-pulse'></i>";
//container.innerHTML += spinner;

visjscomponent = new vis.Network(container, data, options); 



$("div.vis-network div.vis-navigation div.vis-button.vis-up").css({"bottom": "50px", "left": "auto", "right": "55px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-down").css({"bottom": "10px", "left": "auto", "right": "55px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-left").css({"bottom": "10px", "left": "auto", "right": "95px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-right").css({"bottom": "10px", "left": "auto", "right": "15px"});

$("div.vis-network div.vis-navigation div.vis-button.vis-zoomIn").css({"bottom": "auto", "top": "10px", "left": "auto", "right": "15px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-zoomOut").css({"bottom": "auto", "top": "50px", "left": "auto", "right": "15px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-zoomExtends").css({"bottom": "auto", "top": "10px", "left": "auto", "right": "55px"});





//var initCanvasWidth = $(".vis-network canvas:first-child").width();
//var initCanvasHeight = $(".vis-network canvas:first-child").height();


/*visjscomponent.on("zoom", function (params) {
		//window.alert(JSON.stringify(visjscomponent.getScale(), null, 4));
	
		var canvasWidth = $(".vis-network canvas:first-child").width();
		var canvasHeight = $(".vis-network canvas:first-child").height();
		var minContainerWidth = $(div).parent().width();

       var zoomParams = JSON.stringify(params, null, 4);
      // window.alert(zoomParams);


       var zoomValues = JSON.parse(zoomParams);

       var direction = zoomValues.direction;
       var scale = zoomValues.scale;
       if (direction === '+') {
       	// visjscomponent.setSize(canvasWidth*(1+scale), canvasHeight*(1+scale));
       	 visjscomponent.setSize(initCanvasWidth*(scale*10), initCanvasHeight*(scale*10));
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
		//visjscomponent.fit();
		visjscomponent.focus("tok_1");

    });*/

   

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
