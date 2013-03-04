/**
 * Adds a new Rst constructor to the window object. Could be initialized with:
 * var viz = window.viz(config);
 * viz.init();
 *
 *
 */
( function() {

		/**
		 * the following inheritance method is copied from
		 * http://killdream.github.com/blog/2011/10/understanding-javascript-oop
		 */
		// Aliases for the rather verbose methods on ES5
		var descriptor = Object.getOwnPropertyDescriptor, properties = Object.getOwnPropertyNames, define_prop = Object.defineProperty

		// (target:Object, source:Object) → Object
		// Copies properties from `source' to `target'
		function extend(target, source) {
			properties(source).forEach(function(key) {
				define_prop(target, key, descriptor(source, key))
			});

			return target;
		}

		/**
		 * Converts a string to an int.
		 *
		 * @param value
		 *            if it is already an int it returns simply the value, otherwise the
		 *            value is parsed to int.
		 */
		function stringToInt(value) {

			if (( typeof value) === "string") {
				return parseInt(value);
			} else {
				return value;
			}
		}

		/**
		 * Builds an array of arrays of edges objects. This is a
		 * Depth-First-Algorithmus.
		 *
		 * @param json
		 *            the tree to traverse
		 * @param adj
		 *            holds the result. Must be initialized.
		 *
		 */
		function buildAdjazenzArray(json, adj) {
			function buildAdjazenzArrayHelper(json, adj) {

				if (json.pos != undefined && json.data != undefined && json.data.edges.length > 0) {
					adj.push(json.data.edges);
				}

				if (json.children && json.children.length > 0) {
					for (var child in json.children) {

						buildAdjazenzArrayHelper(json.children[child], adj);
					}
				}
			}

			buildAdjazenzArrayHelper(json, adj);
		}

		/**
		 * Gets depth of JSON Tree. It follows the children property. If no children
		 * property exists the height is 0.
		 *
		 * @returns int
		 */
		function getDepth(json) {
			function getDepthHelper(json) {

				if (json.children == undefined || json.children.length === 0)
					return 0;
				else
					return 1 + (json.children.map(getDepthHelper)).reduce(function(x, y) {
						if (x > y)
							return x;
						else
							return y;
					});
			}

			return getDepthHelper(json);
		}

		/**
		 * Calculates the dimensions of the plotted json tree.
		 *
		 * @param {config}
		 *            holds the config of the rst visualization
		 *
		 * @returns {x: width, y: height}
		 */
		function getDim(config) {
			var right = getMostRightNode(config.json).pos.x + config.siblingOffet;
			var depth = getDepth(config.json) * config.subTreeOffset;
			return {
				x : right,
				y : depth
			};
		}

		/**
		 * Get the most right children from a json tree. It follows the children
		 * property of the json object, which must be an array.
		 *
		 * @param {json}
		 *            this json tree is traversed
		 */
		function getMostRightNode(json) {
			if (json.children == undefined || json.children.length == 0) {
				return json;
			} else {
				return getMostRightNode(json.children[json.children.length - 1]);
			}
		}

		/**
		 * Get the most left children from a json tree. It follows the children
		 * property of the json object, which must be an array.
		 *
		 * @param {json}
		 *            this json tree is traversed
		 */
		function getMostLeftNode(json) {
			if (json.children == undefined || json.children.length == 0) {
				return json;
			} else {
				return getMostLeftNode(json.children[0]);
			}
		}

		/**
		 * Build an wrapper element from the container id, which is defined in the
		 * conf object. It uses the container property of conf. If the id is already
		 * defined in the dom, the element is not recreated and the containerElement
		 * property of the config object is set to this HTML element.
		 *
		 * @param conf
		 *            holds the config of the rst-visualization
		 *
		 */
		function initWrapper(conf) {
			var container = document.getElementById(conf.container);

			if (!container) {
				container = document.createElement("div");
				container.setAttribute("id", conf.container);
				document.getElementById(conf.wrapper).appendChild(container);
			}

			dim = getDim(conf);
			container.style.position = "relative";
			container.style.width = dim.x + "px";
			container.style.height = 0 + "px";
			container.style.background = conf.background;
			container.style.padding = conf.padding + "px";

			conf.containerElement = container;
		}

		/**
		 * This layouts the tree. It's based on a Depth-First algorithm and takes
		 * advantage of that this always reaches the leafs first. Leafs are always
		 * segments, so we could easily compute the distance between the segments
		 * and calculate afterwards the position of the parent node.
		 *
		 *
		 * @param {controller}
		 *            must contain a json property. As a side effect, it produces a
		 *            map which project id of nodes to the node javascript object.
		 *            This object is saved under the nodes property in the
		 *            controller.
		 *
		 */
		function layoutTree(controller) {
			var lastChild;

			/**
			 * If the root has at least one satellite, return true.
			 */
			function hasSatellite(json) {
				var childrenArray = json["children"];

				for (var i in childrenArray) {
					var edges = childrenArray[i].data.edges;
					for (var e in edges) {
						if (edges[e].sType == "rst") {
							return true;
						}
					}
				}

				return false;
			}

			function layoutTreeHelper(json, height, config, nodes) {

				// build hashmap by the way, ugly, also exclude root
				if (json.data !== undefined) {
					nodes[json.id] = json;
				}

				// if there exist children, step deeper
				if (json.children && json.children.length > 0) {
					for (var item in json.children) {
						if (json.data === undefined && !hasSatellite(json)) {
							layoutTreeHelper(json.children[item], height, config, nodes);
						} else {
							layoutTreeHelper(json.children[item], height + config.subTreeOffset, config, nodes);
						}
					}

					// exclude root from calculation
					if (json.data !== undefined) {
						// center parent
						var left = json.children[0].pos.x;
						var right = json.children[json.children.length - 1].pos.x;
						var pos = {
							x : Math.floor((left + right) / 2),
							y : height
						};

						json.pos = pos;
					}
				} else {
					// calc posititions of the segments
					json.pos = {
						x : 0,
						y : height
					};
					if (lastChild != undefined) {
						json.pos.x += lastChild.pos.x + config.siblingOffet;
					}
					lastChild = json;
				}
			}

			layoutTreeHelper(controller.json, 0, controller, controller.nodes);
		}

		/**
		 * Creates the HTML element canvas and set the height and width element from
		 * the wrapper element. The height is recalculate when the nodes are
		 * plotted, because at this state, we couldn't know the actual height of the
		 * sentences, which depends on the length of them.
		 *
		 * @param {config}
		 *            holds the config of rst visualization
		 */
		function initCanvas(config) {
			var wrapperElem = config.containerElement;

			config.canvas = document.createElement('canvas');
			wrapperElem.appendChild(config.canvas);
			config.canvas.style.width = wrapperElem.style.width + "px";
			config.canvas.style.height = wrapperElem.style.height + "px";
			config.canvas.setAttribute("width", wrapperElem.style.width);
			config.canvas.setAttribute("height", wrapperElem.style.height);
			config.canvas.style.position = "relative";

			// set get context
			config.context = config.canvas.getContext("2d");
		}

		/**
		 * This actually positions HTML elements on wrapper element. The HTML Id of
		 * the wrapper element is contained in the conf object
		 *
		 * @param {conf}
		 *            holds the config of rst visualization
		 */
		function plotNodes(conf) {
			var container = conf.containerElement;
			var canvas = conf.canvas;
			var nodes = conf.nodes;

			for (var node in nodes) {
				var json = nodes[node];
				var elem = document.createElement("div");
				container.appendChild(elem);

				if (json.data.sentence_left != undefined && json.data.sentence_right != undefined) {
					elem.innerHTML = "<p style='color :" + conf.nodeLabelColor + ";'>" + (json.data.sentence_left + " - " + json.data.sentence_right) + "</p>";
				}

				elem.innerHTML += (json.data.sentence != undefined) ? json.data.sentence : "";

				elem.style.position = "absolute";
				elem.style.top = json.pos.y + "px";
				elem.style.left = json.pos.x + "px";
				elem.style.textAlign = "center";
				elem.style.fontSize = conf.labelSize + "px";
				elem.style.width = conf.nodeWidth + "px";

				// get the deepest one, it's hacky
				var top = elem.clientHeight + elem.offsetTop;
				if (top > container.clientHeight) {
					container.style.height = top + 5 + "px";
					container.setAttribute("height", top +"px");
					canvas.setAttribute("height", top + "px");
				}
			}
		}

		/**
		 * Draws the edges.
		 */
		function plotEdges(config) {
			buildAdjazenzArray(config.json, config.adj);
			var nodes = config.nodes, adj = config.adj;

			/**
			 * This function draws all edges. It takes into account the blurr effect
			 * of canvas.
			 */
			config.context.translate(0.5, 0.5);

			for (var item in nodes) {
				drawHorizontalLine(nodes[item], config);
			}

			for (var i in adj) {

				for (var e in adj[i]) {

					var from = nodes[adj[i][e].from], to = nodes[adj[i][e].to], edgeType = adj[i][e].sType, annotation = adj[i][e].annotation;

					if (edgeType === RST) {
						drawBezierCurve(from, to, config);
						plotRSTLabel(from, to, annotation, config);
					}

					if (edgeType === MULTINUC) {
						drawVerticalLine(from, to, config);
						plotMultinucLabel(from, annotation, config);
					}

					if (edgeType === DOMINANCE) {
						drawSpan(from, to, config);
					}
				}
			}
		}

		/**
		 * Get the middle of a node.
		 */
		function getTopCenter(conf, node) {
			return (((2 * node.pos.x + conf.nodeWidth) / 2));
		}


		function drawVerticalLine(source, target, config) {
			var fromPosX = getTopCenter(config, source), toPosX = getTopCenter(config, target), context = config.context;

			context.beginPath();
			context.moveTo(fromPosX, source.pos.y);
			context.lineTo(toPosX, target.pos.y);
			context.closePath();
			context.stroke();
		}

		function drawHorizontalLine(source, config) {
			var mostLeftChild = getMostLeftNode(source).pos.x, context = config.context, mostRightChild = getMostRightNode(source).pos.x + config.nodeWidth;

			context.beginPath();
			context.moveTo(mostLeftChild, source.pos.y);
			context.lineTo(mostRightChild, source.pos.y);
			context.closePath();
			context.stroke();
		}

		function drawBezierCurve(source, target, config) {
			var from = source.pos, to = target.pos, context = config.context, fromX = getTopCenter(config, source), toX = getTopCenter(config, target), controllPoint = {};

			if (fromX != toX) {
				controllPoint.x = (fromX + toX) / 2;
				controllPoint.y = from.y - 2 * config.dim;
			} else {
				controllPoint.x = fromX + 2 * config.dim;
				controllPoint.y = (from.y + to.y) / 2;
			}

			// draw lines
			context.beginPath();
			context.moveTo(fromX, from.y);
			context.quadraticCurveTo(controllPoint.x, controllPoint.y, toX, to.y, toX, to.y);

			context.stroke();

			var headlen = 10;
			// length of head in pixels
			var angle = Math.atan2(to.y - controllPoint.y, to.x - controllPoint.x);

			context.beginPath();
			context.moveTo(toX, to.y);
			context.lineTo(toX - headlen * Math.cos(angle - Math.PI / 6), to.y - headlen * Math.sin(angle - Math.PI / 6));
			context.lineTo(toX - headlen * Math.cos(angle + Math.PI / 6), to.y - headlen * Math.sin(angle + Math.PI / 6));

			context.fill();
			context.closePath();
		}

		function drawSpan(source, target, config) {
			var targetCenterX = getTopCenter(config, target);

			// draw vertical line
			config.context.beginPath();
			config.context.moveTo(targetCenterX, source.pos.y);
			config.context.lineTo(targetCenterX, target.pos.y);
			config.context.closePath();
			config.context.stroke();
		}

		function plotMultinucLabel(source, annotation, config) {
			var key = source.id + "::" + annotation, multinucLabels = config.multinucLabels;

			// check if this Label is already plotted
			if (multinucLabels[key]) {
				return;
			}

			var label = document.createElement("label");
			multinucLabels[key] = label;

			var firstChild = source.children[0], lastChild = source.children[source.children.length - 1];

			var fromX = firstChild.pos.x;
			toX = lastChild.pos.x, config.containerElement.appendChild(label);
			label.style.position = "absolute";
			label.innerHTML = annotation;
			label.style.fontSize = config.labelSize + "px";
			label.style.fontStyle = "italic";
			label.style.color = config.edgeLabelColor;

			labelPos = {
				x : ((fromX + toX) / 2) + config.nodeWidth / source.children.length - (label.offsetWidth / 2),
				y : source.pos.y + config.subTreeOffset / 2
			}

			label.style.top = labelPos.y + "px";
			label.style.left = labelPos.x + "px";
		}

		/**
		 *
		 */
		function plotRSTLabel(source, target, annotation, config) {
			var fromX = getTopCenter(config, source), toX = getTopCenter(config, target), from = source.pos, label = document.createElement("label");

			config.containerElement.appendChild(label);
			label.style.position = "absolute";
			label.innerHTML = annotation;

			controllPoint = {};
			controllPoint.x = (source.pos.x + target.pos.x) / 2;
			controllPoint.y = from.y - 2 * config.dim;

			label.style.top = controllPoint.y + "px";
			label.style.left = controllPoint.x + "px";
			label.style.fontStyle = "italic";
			label.style.fontSize = config.labelSize + "px";
			label.style.textAlign = "center";
			label.style.width = label.clientWidth + "px";
			label.style.display = "block";
			label.style.color = config.edgeLabelColor;
		}

		var DOMINANCE = "edge";
		var RST = "rst";
		var MULTINUC = "multinuc";

		/**
		 *
		 */
		var Rst = function(config) {

			this.config = {

				// layout configuration
				siblingOffet : stringToInt(config.siblingOffset) || 100,
				subTreeOffset : stringToInt(config.subTreeOffset) || 100,
				nodeWidth : stringToInt(config.nodeWidth) || 60,
				labelSize : stringToInt(config.labelSize) || 10,
				container : config.container || "container",
				background : config.background || "#FFFFFF",
				wrapper : config.wrapper || "wrapper",
				padding : config.padding || "0",
				edgeLabelColor : config.edgeLabelColor || "#14fe14",
				nodeLabelColor : config.nodeLabelColor || "#fe0707",
				dim : stringToInt(config.dim) || 15,

				// this values are not configurable
				json : config.json,
				canvas : {},
				multinucLabels : {},
				nodes : {},
				adj : [],
				containerElement : {}
			};

			/**
			 * Calls the necassary functions to plot edges and to place labels.
			 */
			this.init = function() {
				layoutTree(this.config);
				initWrapper(this.config);
				initCanvas(this.config);
				plotNodes(this.config);
				plotEdges(this.config);
			}
		};

		/**
		 * Expose the constructor for new RST-Visualization.
		 */
		window.$viz = function(config) {
			return new Rst(config);

		}
	}());
