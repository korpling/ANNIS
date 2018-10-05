function SentStructure(json_object) {
	this.dep_colors = {}; 
	//{"-PUNCT-":"grey","SUBJ":"red","DET":"blue","OBJA":"green","ATTR":"yellow",
	//	"AUX":"orange", "PP":"maroon","PN":"Cyan","GMOD":"BlueViolet","OBJD":"Teal","NEB":"SteelBlue",
	//	"KONJ":"Lime","KON":"Crimson","CJ":"Hotpink","APP":"Darkorange","prep":"Cyan","pobj":"Maroon",
	//	"det":"blue","dobj":"Teal","poss":"Darkorange"}

	var theObj = this;
	this.random_id = Math.random().toString().substring(2);
	this.events = [];
	this.points = [];
	this.all_nodes = {};
	$(window).resize(function() {theObj.setDimensions()});
	this.get_node_widths = new Promise(function(resolve,reject) {
		var drawing_stuff = {"sentences":[],"Alignments":[],"length":0}
		var svg = d3.select("body").append("svg")
			.attr("id","svg" + theObj.random_id)
			.attr("class","computer")
			.attr("width",500).attr("height",40).style("visibility","hidden");
		Object.keys(json_object.segments).forEach(function(seg,i) {
			var group1 = svg.append("g").attr("class","tokens");
			var group2 = svg.append("g").attr("class","dep");
			json_object.segments[seg].forEach(function(node) {
				group1.append("text").text(node.type).attr("id",node.id);
				group2.append("text").text(node.dep).attr("id",node.id);
			});
		});
		resolve();
	});

	this.set_lengths = function() {
		var node_dict = {};
		var dep_dict = {};
		theObj.length = 0;
		theObj.max_tokens = 0;
		var g = $("#svg" + theObj.random_id + " g");
			g.each(function(i) {
			var type = $(this).attr("class");
			var segment = $("text",$(this));
			var len = 0;
			var num = 0;
			var that = this;
			segment.each(function(j) {
				var nodeW = this.getBBox().width+4;
				len += nodeW;
				num++;
				if (type == "tokens") {node_dict[$(this).attr("id")] = {"nodeWidth":nodeW};}
				else if (type == "dep") { dep_dict[$(this).attr("id")] = {"nodeWidth":nodeW};}
			});
			if (len>theObj.length) {theObj.length = len;}
			if (num>theObj.max_tokens) {theObj.max_tokens = num;}
		});
		Object.keys(json_object.segments).forEach(function(key) {
			json_object.segments[key].forEach(function(node) {
				node.nodeWidth = node_dict[node.id].nodeWidth;
				node.depWidth = dep_dict[node.id].nodeWidth;
			});
		});
		this.root = json_object;
	}

	this.ready = function(call_b) {
		this.get_node_widths.then(function() {
			theObj.set_lengths();
		}).then(function() {
			$("#svg" + theObj.random_id + ".computer").remove(); call_b(theObj);
		})
	};

	//~ --------------- build the svg ground ----------------------------
	this.setBase = function(def_values) {
		theObj.left = theObj.left||200;
		theObj.right = theObj.right||200;
		theObj.top = theObj.top||200;
		theObj.bottom = theObj.bottom||200;
		theObj.xSpacing = theObj.xSpacing||3;
		theObj.ySpacing = theObj.ySpacing||300;
		theObj.xOffset = theObj.xOffset||0;
		theObj.xOffsetRange = theObj.xOffsetRange||"all";
		theObj.hover_color = theObj.hover_color||"Gainsboro";
		theObj.mark_color = theObj.mark_color||"#FFFF72";
		theObj.important_color = theObj.important_color||"#337ab7";
		theObj.default_color = theObj.default_color||"white"
		theObj.font_size = theObj.font_size||12;
		theObj.display_alignments = theObj.display_alignments||true;
		theObj.display_dependencies = theObj.display_dependencies||true;
		theObj.display_pos = theObj.display_pos||true;
		theObj.text_color = theObj.text_color||"black";
		theObj.link_opacity = theObj.link_opacity||0.5;

		theObj.container = theObj.container||"body";
		theObj.background_color = theObj.background_color||"white";

		//taking the values from constructor
		var that = this;
		d3.keys(def_values).forEach(function(new_val) {
			theObj[new_val] = def_values[new_val];
		});

		if ($(theObj.container).find("#graph-style" + theObj.random_id).length>0) {
			$(theObj.container).find("#graph-style" + theObj.random_id).remove();
		}
		theObj.svg = d3.select(theObj.container).append("svg");
		theObj.svg.attr("class","graph")
			.style("background-color",theObj.background_color);
		$(theObj.container).find("svg.graph").append(
			$("<style id=\"graph-style" + theObj.random_id + "\"></style>").text(theObj.getStylesheet())
		);
		//console.log("new style (set base): #graph-style" + theObj.random_id);

		theObj.setDimensions();
		
		// disable drag and zoom for ANNIS
		theObj.drag = d3.behavior.drag()
			.origin(function(d) { return d; })
			.on("dragstart",function(d,i) {d3.event.sourceEvent.stopPropagation();})
			.on("drag", function(d,i) {
				/*d.arc.label.x += d3.event.dx;
				d.arc.label.y += d3.event.dy;
				d3.select(".link-desc > text.target_"+d.dep_to+".source_"+d.id)
					.attr("transform", function(d,i){
						return "translate(" + [ (d.arc.label.x-d.depWidth/2-4),d.arc.label.y ] + ")" });
				d3.select(".link-desc > rect.target_"+d.dep_to+".source_"+d.id)
					.attr("transform", function(d,i){
						return "translate(" + [ (d.arc.label.x-d.depWidth/2-4),d.arc.label.y ] + ")" });
				d3.select(".link-desc > path.target_"+d.dep_to+".source_"+d.id)
					.attr("d", function() {
				return "M" + d.arc.arctop.x+ "," + d.arc.arctop.y + 
				"L" + (d.arc.label.x) + "," + (d.arc.label.y);});*/
			});
		theObj.zoom = d3.behavior.zoom()
			.scaleExtent([0.5, 10])
			.on("zoom", function() {
				//theObj.svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");    
			});
		theObj.svg = theObj.svg.call(theObj.zoom).append("g");
	}
	this.getStylesheet = function() {
		return "g.nodes rect.hov {fill:"+theObj.hover_color+";}"+
"g.nodes rect.marked {fill:"+theObj.mark_color+";}"+
"g.node-plates rect.important {fill:"+theObj.important_color+";}"+
"g.node-text text {fill:"+theObj.text_color+";}"+
"g.node-text text.important {fill:white;}"+
"g.link * {visibility:"+(theObj.display_dependencies?"visible":"hidden")+";}"+
"g.pos-tags text {visibility:"+(theObj.display_pos?"visible":"hidden")+";}"+
"g.link.link-path path {fill: none;stroke: #666;stroke-width: 3px;opacity: "+String(theObj.link_opacity)+";}"+
"g.link.hidden-link path {fill:none;stroke:#EEEEEE;stroke-width: 16px;opacity:0;}"+
"g.link-desc rect {fill:#e6e6e6;opacity:1;}"+
"g.link-desc text {text-transform:uppercase;}"+
"defs.link.marker-arrows marker { stroke: #666;fill: white;}"+
"g.link.path-text text {font: 12px sans-serif;stroke: none;pointer-events: none;text-shadow: 0 0.4px 0 rgba(250,250,250,0.3), -0.4px 0 0 rgba(250,250,250,0.3);opacity:0.7;}"+
"g.token-alignments path.important {stroke: MediumTurquoise;}"+
"g.token-alignments path {visibility:"+(theObj.display_alignments?"visible":"hidden")+";stroke-width: 3;stroke: #666;opacity:0.3;}"+
"g.nodes rect {fill: "+theObj.default_color+";}"+
"g.link *.locked,g.pos-tags text.locked,g.token-alignments path.locked {visibility:visible;}"+
"g.link *.hov,g.pos-tags text.hov,g.token-alignments path.hov {visibility:visible;}"+
"g.link *.marked,g.pos-tags text.marked,g.token-alignments path.marked {visibility:visible;}"+
"g.link *.important,g.pos-tags text.important,g.token-alignments path.important {visibility:visible;}"+
"text {font-family: Tahoma, Geneva, sans-serif;font-size:"+String(parseInt(theObj.font_size))+"px;pointer-events: none;text-shadow: 0 0.4px 0 rgba(250,250,250,0.3), -0.4px 0 0 rgba(250,250,250,0.3);}";
	}
	this.setProperties = function(props) {
		d3.keys(props).forEach(function(new_val) {
			if (new_val == "xSpacing" || new_val == "ySpacing" || new_val == "xOffset") {
				theObj[new_val] = parseFloat(props[new_val])
			}
			else {
				theObj[new_val] = props[new_val];
			}
		});
		if ($(theObj.container).find("#graph-style" + theObj.random_id).length>0) {
			$(theObj.container).find("#graph-style" + theObj.random_id).remove();
		}
		$(theObj.container).find("svg.graph").append(
			$("<style id=\"graph-style" + theObj.random_id + "\"></style>").text(theObj.getStylesheet())
		);
		//console.log("new style (set properties): #graph-style" + theObj.random_id);
	}
	this.setDimensions = function() {
		if (this.svgWidth == undefined) {this.svgWidth = $(this.container).width();}
		//this.length+(this.max_tokens*this.xSpacing)+(this.left+this.right);
		if (this.svgHeight == undefined) {this.svgHeight = $(this.container).height();}
		//this.top+ (this.ySpacing*(Object.keys(this.root.segments).length-1))+this.bottom;

		d3.select(this.container).select("svg.graph")
			.attr("width", this.svgWidth)
			.attr("height", this.svgHeight);
	}
	this.addEvent = function(evt,selector,evt_caller) {
		this.events.push(function() {d3.select(theObj.container).selectAll(selector).on(evt,evt_caller)});
	}
	this.bindEvents = function() {
		this.events.forEach(function(event_func) {
			event_func();
		});
	}

	//~ --------------- Nodes ----------------------------
	this.getNodes = function(sentence,yOffset) {
		var xstart = this.left;
		//parts = (1/(Object.keys(this.root.segments).length+3))*this.svgHeight;
		//ystart=2*parts + (parts * yOffset);
		var ystart = this.top + yOffset * this.ySpacing;
		var dist = xstart;
		var nodes = {};

		if (this.xOffsetRange == "all") {
			dist += this.xOffset;
		}
		else if (yOffset==this.xOffsetRange){
			dist += this.xOffset;
		}
		var that = this;

		//sentence.sort(function(a,b) {
		//	return b.id < a.id? 
		//	1:(b.id > a.id? -1 : 0);});

		sentence.forEach(function (node,i) {
			try {if (theObj.points[yOffset].nodes[node.id] != undefined) nodes[node.id] = theObj.points[yOffset].nodes[node.id]
				else throw "no entry yet!"
			}
			catch (err) {
				nodes[node.id] = {id: node.id, name: node.type, color: node.color, x:dist, y:ystart, nodeWidth:node.nodeWidth,
				aligns:[],dists:[], pos:node.upos||node.pos, important:node.highlight}
			}
			nodes[node.id].dists = [];
			nodes[node.id].aligns = [];
			nodes[node.id].x = dist;
			nodes[node.id].y = ystart;
			that.all_nodes[node.id] = nodes[node.id];
			dist += nodes[node.id].nodeWidth+that.xSpacing;
			});
		this.nodes = nodes;
	}

	//~ --------------- Links ----------------------------
	this.getLinks = function(sentence) {
		var links = [];
		var obj = this;
		sentence.forEach(function (link) {
			if (link.dep_to != null) {
				try {
					if (obj.nodes[link.id].important||obj.nodes[link.dep_to].important) {
						link.important = true;
					};
				}
				catch(err) {}
				link.source = obj.nodes[link.id];
				link.target = obj.nodes[link.dep_to];
				try {
					dist = (link.source.x+link.source.nodeWidth/2)-(link.target.x+link.target.nodeWidth/2);
					obj.nodes[link.dep_to].dists.push(dist);
					links.push(link);
				}
				catch (err) {}
			}
		});
		this.links = links;
	}

	//~ --------------- Alignments ----------------------------
	this.getAlignments = function() {
		var links = [];
		var Align_object = this;
		this.root.alignments.forEach(function(alignment,i) {
			//try {link=theObj.alignments[i]}
			var link = {};
			//if words are aligned to the important parts, they are also highlighted
			if (Align_object.all_nodes[alignment[1]] && Align_object.all_nodes[alignment[1]]) {

				if (Align_object.all_nodes[alignment[1]].important ||Align_object.all_nodes[alignment[0]].important) {
					Align_object.all_nodes[alignment[0]].important = true;
					link.important = true;
				};

				Align_object.all_nodes[alignment[0]].aligns.push(alignment[1]);
				Align_object.all_nodes[alignment[1]].aligns.push(alignment[0]);
				//~ all_nodes[alignment[1]].aligns.push(alignment[0]);
				link.source = Align_object.all_nodes[alignment[0]];
				link.target = Align_object.all_nodes[alignment[1]];
				link.label = alignment[2];
				var check = true;
				links.forEach(function(l) {
					if (l.source.id == link.target.id && link.source.id == l.target.id) {check = false}
					if (l.source.id == link.source.id && link.target.id == l.target.id) {check = false}
				});
				if (check) {links.push(link);}
			}
		});
		try {
			if (theObj.alignments.length == links.length) {
				theObj.alignments.forEach(function(e,i) {
					links[i].locked = e.locked;
				})
			}
		}
		catch (err) {}
		this.alignments = links;
		//this.all_nodes=all_nodes;
	}

	//~ --------------- draw the graphs from nodes,links and alignments ----------------------------
	this.drawGraphs = function() {
		this.svg.selectAll("*").remove();
		theObj.orientationUp=true;
		var sents = Object.keys(theObj.root.segments);
		sents.forEach(function(key,i) {
			theObj.getNodes(theObj.root.segments[key],i);
			theObj.getLinks(theObj.root.segments[key]);
			theObj.points[i] = {nodes:theObj.nodes,links:theObj.links};
		});
		try {
			theObj.getAlignments();
		}
		catch(err) {console.log(err);}
		this.points.forEach(function(p) {
			theObj.buildNodes(p.nodes);
			theObj.buildLinks(p.links);
			//theObj.orientationUp = false; // orientation down from the second sentence (TODO control by parameter)
		});
		try {
			theObj.drawAlignments();
		}
		catch(err) {console.log(err)}
		this.bindEvents();
	}

	//~ --------------- draw the nodes ----------------------------
	this.buildNodes = function(nodes) {
		var dep_colors = this.dep_colors;
		var nodes = d3.values(nodes);
		var that = this;
		var dep_up = this.orientationUp;
		//label rectangle
		this.node_label = this.svg.append("g").attr("class","nodes node-plates")
			.selectAll("rect")
			.data(nodes)
		.enter().append("rect")
			.attr("id",function(d) {return "_"+d.id;})
			.attr("class",function(d) {
				var cl_str = "";
				var aligned = that.all_nodes[d.id].aligns;
				aligned.forEach(function(al) {
					cl_str += " aligned_to"+al;
				});
				return cl_str;
			})
			.classed("important",function(d) {return d.important})
			.classed("marked",function(d) {return d.marked})
			.attr("width",function(d) {return d.nodeWidth})
			.attr("height", that.font_size*2)
			.attr("rx",3.5)
			.attr("ry",3.5);
		//lable text
		this.node_text = this.svg.append("g").attr("class","nodes node-text")
			.selectAll("text")
			.data(nodes)
		.enter().append("text")
			.attr("id",function(d) {return "_"+d.id;})
			.style("fill", function(d) {return d.color;})
			.attr("class",function(d) {return d.important == true?"important":null})
			.classed("marked",function(d) {return d.marked})
			.attr("y", that.font_size+(that.font_size/2))
			.attr("x", function(d) {return d.nodeWidth/2})
			.style("text-anchor", "middle")
			.text(function(d) { return d.name; });
		//pos-tags
		this.pos_tags = this.svg.append("g").attr("class","pos-tags")
			.selectAll("text")
			.data(nodes)
		.enter().append("text")
			.attr("id",function(d) {return "_"+d.id})
			.attr("class",function(d) {
				var cl_str = "";
				var aligned = that.all_nodes[d.id].aligns;
				aligned.forEach(function(al) {
					cl_str += " aligned_to"+al;
				});
				return cl_str;
			})
			.classed("important",function(d) {return d.important})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			//.style("visibility","hidden")
			.style("text-anchor", "middle")
			.attr("x", function(d) {return d.nodeWidth/2})
			.attr("y", function() {return (dep_up) ? that.font_size*3:that.font_size*-0.5})
			.style("font-size",9)
			.text(function(d) {return d.pos; });

		this.node_label.attr("transform", transform);
		this.node_text.attr("transform", transform);
		this.pos_tags.attr("transform",transform);

		function transform(d) {
			return "translate(" + d.x + "," + d.y + ")";
		}
	}

	//~ --------------- draw the links ----------------------------
	this.buildLinks = function(links) {
		var dep_colors = this.dep_colors;
		var that = this;
		var dep_up = this.orientationUp;

		// arrow styling
		this.marker = this.svg.append("defs").attr("class","link marker-arrows")
			.selectAll("marker")
			.data(links)
			.enter().append("marker")
			.attr("class",function(d) {
				var cl_str = "target_"+d.dep_to+" source_"+d.id;
				[d.dep_to,d.id].forEach(function(id) {
					var aligned = that.all_nodes[id].aligns;
					aligned.forEach(function(al) {
						cl_str += " aligned_to"+al;
					});
				});
				return cl_str;
			})
			.classed("important",function(d) {return d.important})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			.attr("id",function(d) {return "marker"+d.id})
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 5)
			.attr("refY", 0)
			.attr("markerWidth", 4.5)
			.attr("markerHeight", 4.5)
			.attr("orient", "auto")
			.append("path")
			.style("stroke",function(d) {return dep_colors[d.dep]||"#666";})
			//.style("fill",function(d) {return dep_colors[d.dep]||"#666";})
			.attr("d", "M0,-3L7,0L0,2L0,-3"); //pfeil form

		//link path
		this.path = this.svg.append("g").attr("class","link link-path")
			.selectAll("path")
			.data(links)
		.enter().append("path")
			.attr("class",function(d) {
				cl_str = "target_" + d.dep_to + " source_" + d.id;
				[d.dep_to,d.id].forEach(function(id) {
					aligned = that.all_nodes[id].aligns;
					aligned.forEach(function(al) {
						cl_str += " aligned_to" + al;
					});
				});
				return cl_str;
			})
			.classed("important",function(d) {return d.important})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			//.style("visibility",function(d) {return d.important?"visible":"hidden"})
			.style("stroke",function(d) {return dep_colors[d.dep]||"#666";})
			.attr("marker-end", function(d) {return "url(#marker"+d.id+")"})
			.attr("d",function(d) {paths = linkArc(d); d.arc = paths;return d.arc.link;});
		//bigger invisible path for mouseover events
		this.hidden_path = this.svg.append("g").attr("class", "link hidden-link")
			.selectAll("path")
			.data(links)
		.enter().append("path")
			//.style("visibility",function(d) {return d.important?"visible":"hidden"})
			.attr("class",function(d) {return d.important == true ? "target_" + d.dep_to + " source_" + d.id + " important" : "target_" + d.dep_to + " source_" + d.id });
		this.guide_labels = this.svg.append("g").attr("class","link link-desc")
			.selectAll("path")
			.data(links)
			.enter().append("path")
			.attr("class",function(d) {
				cl_str="target_"+d.dep_to+" source_"+d.id;
				[d.dep_to,d.id].forEach(function(id) {
					aligned = that.all_nodes[id].aligns;
					aligned.forEach(function(al) {
						cl_str += " aligned_to" + al;
					});
				});
				return cl_str;
			})
			.classed("important",function(d) {return d.important})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			.style("stroke-width",1)
			.style("stroke","black")
			.style("opacity",0.6)
			//.style("visibility",function(d) {return d.important?"visible":"hidden"});

		//lable rectangle for path
		this.edge_label = this.svg.append("g").attr("class","link link-desc path-rect-lable")
			.selectAll("rect")
			.data(links)
		.enter().append("rect")
			.attr("class",function(d) {
				cl_str="target_"+d.dep_to+" source_"+d.id;
				[d.dep_to,d.id].forEach(function(id) {
					aligned=that.all_nodes[id].aligns;
					aligned.forEach(function(al) {
						cl_str += " aligned_to" + al;
					});
				});
				return cl_str;
			})
			.attr("width", function(d) {return d.depWidth+8})
			.attr("height", 14)
			.classed("important",function(d) {return d.important})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			//.style("visibility",function(d) {return d.important?"visible":"hidden"})
			.attr("rx",1.5)
			.attr("ry",1.5)
			.call(that.drag);

		//text for path lable
		this.path_text = this.svg.append("g").attr("class","link link-desc path-text")
			.selectAll("text")
			.data(links)
			.enter().append("text")
			//.style("visibility",function(d) {return d.important?"visible":"hidden"})
			.attr("class",function(d) {
				cl_str = "target_" + d.dep_to + " source_" + d.id;
					[d.dep_to,d.id].forEach(function(id) {
					aligned=that.all_nodes[id].aligns;
					aligned.forEach(function(al) {
						cl_str += " aligned_to" + al;
					});
				});
				return cl_str;
			})
			.classed("important",function(d) {return d.important})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			.text(function(d) {return d.dep;})
			.style("font-size","8px")
			.attr("x", function(d) {return d.depWidth/2+4})
			.attr("y", "1.3em")
			.attr("text-anchor", "middle");

		eliminate_overlap();
		this.path.data().forEach(function(d,i) {
			d3.select(that.hidden_path[0][i]).attr("d",d.arc.link);
			d3.select(that.edge_label[0][i]).attr("transform","translate("+(d.arc.label.x-d.depWidth/2-4)+","+d.arc.label.y+")");
			d3.select(that.path_text[0][i]).attr("transform","translate("+(d.arc.label.x-d.depWidth/2-4)+","+d.arc.label.y+")");
			d3.select(that.guide_labels[0][i]).attr("d",function() {
				return "M" + d.arc.arctop.x+ "," + d.arc.arctop.y + 
				"L" + (d.arc.label.x) + "," + (d.arc.label.y);});
		});
		//this.force = d3.layout.force().size([theObj.svgWidth,theObj.svgHeight])
		//	.nodes(theObj.edge_label.data());
		//this.force.gravity(0);
		//this.force.charge(-3);
		//this.force.on("tick",force_step);
		//this.force.start();

		//~ --------------- function to set the path trajectory of links ----------------------------
		function linkArc(d) {
			var dx = Math.abs((d.target.x+d.target.nodeWidth/2) - (d.source.x+d.source.nodeWidth/2));
			var paths = {arctop:{x:0,y:0},label:{x:0,y:0}};
			var source_y = dep_up? d.source.y : d.source.y+theObj.font_size*2;
			var source_x = d.source.x + (d.source.nodeWidth/2);
			d.dep_up = dep_up;
			var target_y = dep_up? d.target.y-3 : d.target.y+theObj.font_size*2+3;
			var target_x = d.target.x;
			  
			var dr;
			if (dx > (2/3)*that.svgWidth) { dr = dx*1.3;}
			//else if (dx < 50) {dr = 50;}
			else {dr = dx/1.5;};
			  
			var back = dep_up? 0:1;
			var forw = dep_up? 1:0;
			var dist_left = d.target.dists.filter(function(e) {return e>0;}).sort(function(a,b) {return a-b}); 
			var dist_right = d.target.dists.filter(function(e) {return e<0;}).sort(function(a,b) {return b-a});
			  
			//arrow to left
			if (d.source.x >= d.target.x) {
				var place = dist_left.indexOf(dx)+1;
				var offset = d.target.nodeWidth - (place*((d.target.nodeWidth/2)/(dist_left.length+1)));
				var halfway = -1*Math.abs((target_x+offset-source_x)/2);
	// 			console.log(d.source.name+" --> "+d.target.name);
				paths.link = "M" + source_x + "," + source_y + "A" + dr + "," + dr + " 0 0,"+back+" " + (target_x+offset+3) + "," + target_y;
				paths.arctop.x = source_x+halfway;

			}
			//arrow to right
			else {
				var place = dist_right.indexOf((0-dx))+1;
				var offset = 0 + (place*((d.target.nodeWidth/2)/(dist_right.length+1)));
				var halfway = Math.abs((target_x+offset-source_x)/2);
	// 			console.log(d.source.name+" --> "+d.target.name);
				paths.arctop.x = source_x+halfway;
				paths.link = "M" + source_x + "," + source_y + "A" + dr + "," + dr + " 0 0,"+forw+" " + (target_x+offset-3) + "," + target_y;
			}

			if (dep_up) { //formula to get maximum distance of the arc 
				elev = -1*(dr - Math.sqrt(dr*dr - (halfway*halfway) ));
				paths.arctop.y = source_y  + elev;
			}
			else {
				elev = (dr - Math.sqrt(dr*dr - (halfway*halfway) ));
				paths.arctop.y = source_y  + elev; 
			}
			paths.label.x = paths.arctop.x+(150/halfway);
			paths.label.y = source_y+elev+(elev>0?27:-20)+(150/elev);
			return paths; 
		}

		function eliminate_overlap() {
			that.path.data().sort(function(a,b) {
				var distx = a.arc.label.x-b.arc.label.x;
				var disty = a.arc.label.y-b.arc.label.y;
				if (Math.abs(distx)<=50 && Math.abs(disty)<=22) {
					if (a.dep_up) {a.arc.label.y-=10;}
					else {a.arc.label.yi += 10;}
					if (Math.abs(distx) <= 50) {
						a.arc.label.x -= distx*-0.1;
						b.arc.label.x += distx*-0.1;
					}
					eliminate_overlap();
				}
				return distx;
			});
		}
	}

	//~ --------------- draw the Alignments ----------------------------
	this.drawAlignments = function() {

		function draw_line(d) {
	// 		console.log((d.source.x+37)+","+d.source.y+"-->"+ (d.target.x+37) + "," + d.target.y);
			if (d.source.y < d.target.y) {
				// draw lines from left to right so that textPath labels are right-side up
				if (d.source.x + d.source.nodeWidth/2 < d.target.x + d.target.nodeWidth/2) {
					return "M" + (d.source.x+d.source.nodeWidth/2) + "," + (d.source.y+theObj.font_size*2) + "L" + (d.target.x+d.target.nodeWidth/2) + "," + (d.target.y);
				} else {
					return "M" + (d.target.x+d.target.nodeWidth/2) + "," + (d.target.y) + "L" + (d.source.x+d.source.nodeWidth/2) + "," + (d.source.y+theObj.font_size*2);
				}
			} else {
				if (d.source.x + d.source.nodeWidth/2 < d.target.x + d.target.nodeWidth/2) {
					return "M" + (d.source.x+d.source.nodeWidth/2) + "," + (d.source.y) + "L" + (d.target.x+d.target.nodeWidth/2) + "," + (d.target.y+theObj.font_size*2);
				} else {
					return "M" + (d.target.x+d.target.nodeWidth/2) + "," + (d.target.y+theObj.font_size*2) + "L" + (d.source.x+d.source.nodeWidth/2) + "," + (d.source.y);
				}
			}
		}

		this.align_path = this.svg.append("g").attr("class","token-alignments")
			.selectAll("path")
			.data(this.alignments)
		.enter().append("path")
			.attr("class",function(d) {return d.important?"target_"+d.target.id+" source_"+d.source.id+" important":"target_"+d.target.id+" source_"+d.source.id})
			.attr("id", function(d) {return "label_" + theObj.random_id + "_" + d.source.id + "_" + d.target.id;})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			.attr("class", "token-alignment")
			.attr("d",draw_line)
			.each(function(d) { d.length = this.getTotalLength();});

		this.align_path_labels = this.svg.append("g").attr("class","token-alignment-labels")
			.selectAll("text")
			.data(this.alignments)
		.enter().append("text")
			.attr("x", function(d) {return d.length * 0.25;})
			.attr("dy", theObj.font_size)
			.append("textPath")
			.attr("xlink:href", function(d) {return "#label_" + theObj.random_id + "_" + d.source.id + "_" + d.target.id;})
			.attr("class",function(d) {return d.important?"target_"+d.target.id+" source_"+d.source.id+" important":"target_"+d.target.id+" source_"+d.source.id})
			.classed("locked",function(d) {return d.locked})
			.classed("marked",function(d) {return d.marked})
			.text(function(d) {return d.label;});
	}

	//~ --------------- show all the dependent links of a node ----------------------------
	this.show_dependencies = function(node) {
		var id = node.id;
		d3.selectAll("rect#_"+id+":not(.important):not(.marked)").classed("hov",true);
		d3.selectAll("rect.aligned_to"+id+":not(.important):not(.marked)").classed("hov",true);
		d3.selectAll(".link .source_"+id+":not(.important):not(.locked), .link .target_"+id+":not(.important):not(.locked), .link .aligned_to"+id+":not(.important):not(.locked)")
			.classed("hov",true);
		d3.selectAll(".target_"+id+":not(.important):not(.locked), .source_"+id+":not(.important):not(.locked)")
			.classed("hov",true);
	}
	this.hide_dependencies = function(node) {
		var id = node.id;
		d3.selectAll("rect#_"+id+":not(.important)").classed("hov",false);
		d3.selectAll("rect.aligned_to"+id+":not(.important)").classed("hov",false);
		d3.selectAll(".link-desc .source_"+id+":not(.important):not(.locked), .link-desc .target_"+id+":not(.important):not(.locked), .link .aligned_to"+id+":not(.important):not(.locked)")
			.classed("hov",false);

		d3.selectAll(".target_"+id+":not(.important):not(.locked), .source_"+id+":not(.important):not(.locked)")
			.classed("hov",false);
	}

	//~ --------------- change "marked" attribute on a node and all its dependent links ----------------------------
	this.mark = function(node) {
		if (!d3.select("rect#_"+node.id).classed("marked")) {
			d3.selectAll("rect#_"+node.id+":not(.important), rect.aligned_to"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = true; return true;});
			d3.selectAll("g.pos-tags text#_"+node.id+":not(.important), g.pos-tags text.aligned_to"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = true; return true;});
			d3.selectAll(".link .source_"+node.id+":not(.important), .link .target_"+node.id+":not(.important), .link .aligned_to"+id+":not(.important)")
				.classed("marked",function(d) {d.marked = true; return true;});
			d3.selectAll(".target_"+node.id+":not(.important), .source_"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = true; return true;});
		}
		else {
			d3.selectAll("rect#_"+node.id+":not(.important), rect.aligned_to"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = false; return false;});
			d3.selectAll("g.pos-tags text#_"+node.id+":not(.important), g.pos-tags text.aligned_to"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = false; return false;});
			d3.selectAll(".link-desc .source_"+node.id+":not(.important), .link-desc .target_"+node.id+":not(.important), .link .aligned_to"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = false; return false;});

			d3.selectAll(".target_"+node.id+":not(.important), .source_"+node.id+":not(.important)")
				.classed("marked",function(d) {d.marked = false; return false;});
		}
	}
}

function parse_CONLL(conllstr,indices) {
	if (indices == undefined) {indices = {};}
	var alignments = [];
	var inparr = [];
	outp_segment = [];
	conllstr.split("\n").forEach(function(sent) {
		inparr.push(sent.split(/\s+/))
	});
	inparr.forEach(function(word) {
		if (word[0] != "") { 
		obj = {};
		obj.lemma = word[indices.lemma||2];
		obj.type = word[indices.type||1];
		obj.upos = word[indices.pos||3];
		obj.dep = word[indices.dep||7];
		obj.dep_to = word[indices.depto||6];
		obj.id = word[indices.id||8];
		obj.highlight = false;
	try {
		word[indices.align||13].split("|").forEach(function(aligned_id) {
			alignments.push([obj.id,aligned_id]);
		});
	}
	catch (err) {console.log(err)}
		outp_segment.push(obj);
	}
	});
//	console.log({segment:outp_segment,alignments:alignments});
	return {segment:outp_segment,alignments:alignments}
}

// TODO: convert to class function
function getDownloadURI() {
	var svg = $("svg.graph")[0]; // TODO: use id
	var serializer = new XMLSerializer();
	var source = serializer.serializeToString(svg);

	//add name spaces.
	if(!source.match(/^<svg[^>]+xmlns="http\:\/\/www\.w3\.org\/2000\/svg"/)){
		source = source.replace(/^<svg/, '<svg xmlns="http://www.w3.org/2000/svg"');
	}
	if(!source.match(/^<svg[^>]+"http\:\/\/www\.w3\.org\/1999\/xlink"/)){
		source = source.replace(/^<svg/, '<svg xmlns:xlink="http://www.w3.org/1999/xlink"');
	}
	//add xml declaration
	source = '<?xml version="1.0" standalone="no"?>\r\n' + source;
	//convert svg source to URI data scheme.
	var url =  "data:image/svg+xml;charset=utf-8,"+encodeURIComponent(source);
	return url;
}
