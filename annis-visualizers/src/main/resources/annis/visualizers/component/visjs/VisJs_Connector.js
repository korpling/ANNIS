//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {




this.init = function(n){
var e = this.getElement();
e.innerHTML = "<div id='mynetwork'>container</div>";
var container = document.getElementById('mynetwork');
container.style.border = "thin solid green";

/*var nodes = 
[{"id":"sTok1","label":"id=sTok1\nIs","color":"#CCFF99","x":100,"level":1}
,{"id":"sTok2","label":"id=sTok2\nthis","color":"#CCFF99","x":200,"level":1}
,{"id":"sTok3","label":"id=sTok3\nexample","color":"#CCFF99","x":300,"level":1}
,{"id":"sTok4","label":"id=sTok4\nmore","color":"#CCFF99","x":400,"level":1}
,{"id":"sTok5","label":"id=sTok5\ncomplicated","color":"#CCFF99","x":500,"level":1}
,{"id":"sTok6","label":"id=sTok6\nthan","color":"#CCFF99","x":600,"level":1}
,{"id":"sTok7","label":"id=sTok7\nit","color":"#CCFF99","x":700,"level":1}
,{"id":"sTok8","label":"id=sTok8\nappears","color":"#CCFF99","x":800,"level":1}
,{"id":"sTok9","label":"id=sTok9\nto","color":"#CCFF99","x":900,"level":1}
,{"id":"sTok10","label":"id=sTok10\nbe","color":"#CCFF99","x":1000,"level":1}
,{"id":"sTok11","label":"id=sTok11\n?","color":"#CCFF99","x":1100,"level":1}
];*/
var nodes = JSON.parse(n);
window.alert(nodes[0].id);

var edges = [];
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
visjscomponent.draw();   

}

 

// Handle changes from the server-side
this.onStateChange = function(){
        this.init();
    }
   
  
};