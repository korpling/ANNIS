//Connector
window.annis_visualizers_component_sentstructurejs_SentStructureJsComponent = function() {

var container = this.getElement();

$(container).css({"width": "100%", "height": "100%"});

container.style.border = "thin solid light blue";
container.style.display = "inline-block";

var vis_id =  this.getState().containerId;

var sent_vis;
var sent_vis_padding = 10;

// handle changes from the server-side
this.onStateChange = function() {
		
	// cleanup old graph
    if (typeof sentstructure !== 'undefined' && sentstructure != null) {
    	sentstructure.destroy();
    }

    this.init(JSON.parse(this.getState().jsonStr));
};


this.init = function(input) {
	sent_vis = new SentStructure(input);

	sent_vis.setBase({container: "#" + vis_id});

	sent_vis.ready(function() {
		sent_vis.setProperties({
			left: sent_vis_padding,
			right: sent_vis_padding,
			top: sent_vis_padding,
			bottom: sent_vis_padding,
			xSpacing: 25,
			ySpacing: 150,
			display_dependencies: false,
			display_alignments: true,
			display_pos: false
		});

		sent_vis.drawGraphs();
		
		// disable default resizing function
		sent_vis.setDimensions = function() {};

		// get size of rendered svg
		var svgBBox = $(container).find('svg')[0].getBBox();
		
		// aspect ratio including padding
		var svgAspectRatio = (svgBBox.width + sent_vis_padding * 2) / (svgBBox.height + sent_vis_padding * 2);

		var	containerWidth = $(container).width();

		// if svg is not as wide as container, don't scale up
		// (only scale down if too wide)
		if (svgBBox.width < containerWidth) {
			containerWidth = svgBBox.width;
		}

		// set container height to correspond to full height of svg 
		// scaled down to preserve aspect ratio in container width
		$(container).css({"height": Math.ceil((containerWidth + sent_vis_padding * 2) / svgAspectRatio)});
		
		// set svg height/width to container size and viewBox to rendered size
		$(container).find('svg')[0].setAttribute("width", containerWidth);
		$(container).find('svg')[0].setAttribute("height", 
				Math.ceil((containerWidth + sent_vis_padding * 2) / svgAspectRatio));
		$(container).find('svg')[0].setAttribute("viewBox", "0 0 " 
				+ (svgBBox.width + sent_vis_padding * 2) + " "
				+ (svgBBox.height + sent_vis_padding * 2));
	});
};

};
