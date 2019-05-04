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
		var descriptor = Object.getOwnPropertyDescriptor;
		var properties = Object.getOwnPropertyNames;
		var define_prop = Object.defineProperty;

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
		 * Add a <style> element to the document head for styling elements used
		 * in the visualization of signals.
		 *
		 * @param conf
		 *            holds the config of the rst-visualization
		 *
		 */
		function injectStyles(conf) {
			var head = document.head;
			var style = document.createElement('style');

			var css = ( ".rst-node-id {"
					  + "  color: " + conf.nodeLabelColor + ";"
					  + "  position: relative;"
					  + "  margin: 10px 0;"
					  + "}"

					  + ".rst-signal-list {"
					  + "  opacity: 0;"
					  + "  position: absolute;"
					  + "  background: white;"
					  + "  bottom: 4px;"
					  + "  left: 0;"
					  + "  width: 120px;"
					  + "  list-style: none;"
					  + "  padding: 0;"
					  + "  box-shadow: 2px 2px 5px 2px #cccccc;"
					  + "  font-style: normal;"
					  + "  z-index: 1337;"
					  + "}"

					  + ".rst-signal-list-item {"
					  + "  padding: 2px 0;"
					  + "  border-bottom: 1px solid #cccccc;"
					  + "  text-align: left;"
					  + "  padding-left: 4px;"
					  + "  color: black;"
					  + "  background: white;"
					  + "  transition-duration: 0.5s;"
					  + "  transition-property: background;"
					  + "}"

					  + ".rst-signal-list-item:last-child {"
					  + "  border-bottom: none;"
					  + "}"

					  + ".rst-signal-list-item--highlighted {"
					  + "  background-color: yellow;"
					  + "}"

					  + ".rst-relation {"
					  + "  font-style: italic;"
					  + "  text-align: center;"
					  + "  position: absolute;"
					  + "  display: block;"
					  + "  white-space: nowrap;"
					  + "}"

					  + ".rst-relation:hover .rst-signal-list {"
					  + "  opacity: 1;"
					  + "  transition-duration: 0.3s;"
					  + "  transition-delay: 0.5s;"
					  + "  transition-property: opacity;"
					  + "}"

					  + ".badge {"
					  + "  width: 11px;"
					  + "  height: 11px;"
					  + "  font-size: 9px;"
					  + "  border: solid 1px;"
					  + "  border-radius: 8px;"
					  + "  padding: 0;"
					  + "  margin: 0 0 0 0.5em;"
					  + "  background-color: #f7f7f7;"
					  + "  color: black;"
					  + "  display: inline-block;"
					  + "  font-style: normal;"
					  + "  text-align: center;"
					  + "}"

					  + ".badge--highlighted {"
					  + "  background-color: yellow !important;"
					  + "}"

					  + ".rst-relation:hover .badge {"
					  + "  background-color: rgba(255, 255, 0, 0.5);"
					  + "  transition-duration: 0.3s;"
					  + "  transition-delay: 0.5s;"
					  + "  transition-property: background-color;"
					  + "}"

					  + ".rst-token--highlighted {"
					  + "  background-color: yellow !important;"
					  + "}"

					  + ".rst-token--semi-highlighted {"
					  + "  background-color: rgba(255, 255, 0, 0.2);"
					  + "}"

					  + ".show-all-tokens {"
					  + "  position: absolute;"
					  + "  top: 10px;"
					  + "  right: 10px;"
					  + "}"
					  );
			style.innerHTML = css;

			head.appendChild(style);
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

			// for every node, create a div absolutely positioned over the canvas sketch
			// that displays its ID and a list of any signals associated with it on hover
			for (var node in nodes) {
				var json = nodes[node];
				var nodeElt = document.createElement("div");
				var nodeIdElt = document.createElement("div");
				nodeIdElt.classList.add("rst-node-id");
				nodeElt.appendChild(nodeIdElt);
				container.appendChild(nodeElt);

				// add <p> with the node's ID (or ID range)
				if (json.data.sentence_left != undefined && json.data.sentence_right != undefined) {
					var eduRange = json.data.sentence
						? json.name
						: (json.data.sentence_left + " - " + json.data.sentence_right);
					nodeIdElt.appendChild(document.createTextNode(eduRange));
				}

				// add the sentence, if it exists
				var sentenceSpan = document.createElement("span");
				sentenceSpan.innerHTML = (json.data.sentence != undefined) ? json.data.sentence : "";
				nodeElt.appendChild(sentenceSpan);

				// set the div's label
				nodeElt.style.position = "absolute";
				nodeElt.style.top = json.pos.y + "px";
				nodeElt.style.left = json.pos.x + "px";
				nodeElt.style.textAlign = "center";
				nodeElt.style.fontSize = conf.labelSize + "px";
				nodeElt.style.width = conf.nodeWidth + "px";

				// extend the height of the container and canvas
				// if the node just placed lies beyond it
				var top = nodeElt.clientHeight + nodeElt.offsetTop;
				if (top > container.clientHeight) {
					container.style.height = top + 5 + "px";
					container.setAttribute("height", top +"px");
					canvas.setAttribute("height", top + "px");
				}
			}
		}

		/**
		 * A small circular icon that indicates how many signals are associated with a particular relation.
		 * @param {signals}
		 *            the list of signals associated with this node
		 */
		function createSignalBadge(signals) {
			var badge = document.createElement("div");
			badge.classList.add("badge");
			badge.innerText = signals.length;
			return badge;
		}

		/**
		 * A hovering list that appears when a relation is hovered over. Items represent individual signals and
		 * can be clicked.
		 *
		 * @param {conf}
		 *            holds the config of rst visualization
		 * @param {node}
		 *            the JS object representing the node currently being drawn in plotNodes
		 * @param {signals}
		 *            the list of signals associated with this node
		 * @param {badgeElt}
		 *            a reference to the badge element
		 * @param {tokenCount}
		 *            an array mapping from token index to number of times a signal has highlighted it
		 */
		function createSignalList(conf, node, signals, badgeElt, tokenCount) {
			var list = document.createElement("ul");
			list.classList.add("rst-signal-list");

			for (signal in signals) {
				var signalElt = createSignalListItem(conf, node, signals[signal], badgeElt, tokenCount);
				list.appendChild(signalElt);
			}

			return list;
		}

		/**
		 * Represents an individual signal.
		 *
		 * @param {conf}
		 *            holds the config of rst visualization
		 * @param {node}
		 *            the JS object representing the node currently being drawn in plotNodes
		 * @param {signal}
		 *            a JS object representing an individual signal
		 * @param {badgeElt}
		 *            a reference to the badge element
		 * @param {tokenCount}
		 *            an array mapping from token index to number of times a signal has highlighted it
		 */
		function createSignalListItem(conf, node, signal, badgeElt, tokenCount) {
			var elt = document.createElement("li");
			elt.classList.add("rst-signal-list-item");
			elt.innerHTML = signal.type + ", " + signal.subtype;

			elt.addEventListener("click", function() {
				var tokens = conf.containerElement.querySelectorAll("span.rst-token");
				var indexes = signal.indexes;
				var i;
				var itemNotPreviouslySelected = elt.classList.toggle("rst-signal-list-item--highlighted");

				// Ensure that the badge is yellow iff at least one signal item is selected
				if (itemNotPreviouslySelected) {
					badgeElt.classList.add("badge--highlighted");
				} else if (elt.parentNode.querySelectorAll(".rst-signal-list-item--highlighted").length === 0) {
					badgeElt.classList.remove("badge--highlighted");
				}

				// Need to be careful not to de-select a token if another signal was already selected that highlighted
				// the same token. We keep track of how many times a token has been highlighted by a signal in the
				// tokenCount array: when a signal highlights a token, the count for that token is increased by 1,
				// and when a signal "unhighlights" a token, it is only unhighlighted if no other tokens have
				// highlighted that token, i.e. if tokenCount[index] is 0.
				for (i = 0; i < indexes.length; i++) {
					var tokenListIndex = indexes[i] - 1;
					var existingCount = tokenCount[tokenListIndex];
					if (itemNotPreviouslySelected) {
						tokenCount[tokenListIndex] = existingCount ? existingCount + 1 : 1;
						tokens[tokenListIndex].classList.add("rst-token--highlighted");
					} else {
						tokenCount[tokenListIndex] -= 1;
						if (tokenCount[tokenListIndex] === 0) {
							tokens[tokenListIndex].classList.remove("rst-token--highlighted");
						}
					}
				}
			});

			return elt;
		}

		/**
		 * recursive helper that finds all signals
		 *
		 * @param {json}
		 *            the JS object representing the entire RST data structure
		 */
		function findAllSignals(json) {
			var signals = [];
			if (!json) {
				return signals;
			}

			if (json.data && json.data.signals) {
				signals = signals.concat(json.data.signals);
			}

			if (json.children) {
				for (var i = 0; i < json.children.length; i++) {
				   signals = signals.concat(findAllSignals(json.children[i]));
				}
			}

			return signals;
		}


		/**
		 * Create a button that semi-highlights all tokens associated with a signal when pressed.
		 *
		 * @param {conf}
		 *            holds the config of rst visualization
		 * @param {tokenCount}
		 *            an array mapping from token index to number of times a signal has highlighted it
		 */
		function createShowAllSignalsButton(conf, tokenCount) {
			var signals = findAllSignals(conf.json);
			if (signals.length === 0) {
				return null;
			}

			var button = document.createElement("button");
			button.classList.add("show-all-tokens");
			var onText = "Hide Signal Tokens";
			var offText  = "Show All Signal Tokens";
			button.innerText = offText;

			var tokens = conf.containerElement.querySelectorAll("span.rst-token");

			// This button, when activated, adds a slight highlight to all tokens that are associated with at least one
			// signal in the document. When it is deactivated, not only this slight highlight is removed, but all other
			// signal-related state is also removed. This includes badge, signal list item, and token selections, as
			// well as the state of tokenCount (see createSignalListItem).
			button.addEventListener("click", function() {
				if (button.innerText === offText) {
					signals.forEach(function(signal) {
						signal.indexes.forEach(function(i) {
							tokens[i - 1].classList.add("rst-token--semi-highlighted");
						});
					});
					button.innerText = onText;
				} else {
					var highlightedTokens = conf.containerElement.querySelectorAll("span.rst-token--semi-highlighted");
					for (i = 0; i < highlightedTokens.length; i++) {
						highlightedTokens[i].classList.remove("rst-token--semi-highlighted");
						highlightedTokens[i].classList.remove("rst-token--highlighted");
					}

					var highlightedSignals = conf.containerElement.querySelectorAll(".rst-signal-list-item--highlighted");
					for (i = 0; i < highlightedSignals.length; i++) {
						highlightedSignals[i].classList.remove("rst-signal-list-item--highlighted");
					}

					var highlightedBadges = conf.containerElement.querySelectorAll(".badge--highlighted");
					for (i = 0; i < highlightedSignals.length; i++) {
						highlightedBadges[i].classList.remove("badge--highlighted");
					}

					tokenCount = [];
					button.innerText = offText;
				}
			});
			return button;
		}

		/**
		 * Draws the edges.
		 */
		function plotEdges(config) {
			buildAdjazenzArray(config.json, config.adj);
			var nodes = config.nodes;
			var adj = config.adj;
			// keeps track of how many currently selected signals are highlighting a certain token
			var tokenCount = [];

			/**
			 * This function draws all edges. It takes into account the blur effect
			 * of canvas.
			 */
			config.context.translate(0.5, 0.5);

			for (var item in nodes) {
				drawHorizontalLine(nodes[item], config);
			}

			for (var i in adj) {

				for (var e in adj[i]) {

					var from = nodes[adj[i][e].from];
					var to = nodes[adj[i][e].to];
					var edgeType = adj[i][e].sType;
					var annotation = adj[i][e].annotation;

					if (edgeType === RST) {
						drawBezierCurve(from, to, config);
						plotRSTLabel(from, to, annotation, config, tokenCount);
					}

					if (edgeType === MULTINUC) {
						drawVerticalLine(from, to, config);
						plotMultinucLabels(from, annotation, config, tokenCount);
					}

					if (edgeType === DOMINANCE) {
						drawSpan(from, to, config);
					}
				}
			}

			var showSignalsButton = createShowAllSignalsButton(config, tokenCount);
			if (showSignalsButton) {
				// This is a little evil: we are relying on vaadin implementation facts to
				// attach this button to a div that will keep it in place during scroll.
				// If this button ever breaks in the future, look here.
				document.getElementById(config.container).parentNode.parentNode.parentNode.appendChild(showSignalsButton);
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

		function plotMultinucLabels(source, annotation, config, tokenCount) {
			var key = source.id + "::" + annotation, multinucLabels = config.multinucLabels;

			// check if this Label is already plotted
			if (multinucLabels[key]) {
				return;
			}

			source.children.forEach(function (child) {
				var label = document.createElement("label");
				label.classList.add("rst-relation");
				multinucLabels[key] = label;
				config.containerElement.appendChild(label);

				label.innerHTML = annotation;
				label.style.fontSize = config.labelSize + "px";
				label.style.color = config.edgeLabelColor;

				var labelPos = {
					x : child.pos.x,
					y : source.pos.y + config.subTreeOffset / 2
				}

				label.style.top = labelPos.y + "px";
				label.style.left = labelPos.x + "px";

				// add signal badge and list, if signals are present
				var signals = child.data.signals;
				if (signals && signals.length > 0) {
					var signalBadge = createSignalBadge(signals);
					var signalListElt = createSignalList(config, child, signals, signalBadge, tokenCount);
					label.appendChild(signalListElt);
					label.appendChild(signalBadge);
				}
			});
		}

		/**
		 *
		 */
		function plotRSTLabel(source, target, annotation, config, tokenCount) {
			var fromX = getTopCenter(config, source);
			var toX = getTopCenter(config, target);
			var from = source.pos;
			var label = document.createElement("label");
			label.classList.add("rst-relation");

			config.containerElement.appendChild(label);
			label.innerHTML = annotation;

			controlPoint = {};
			controlPoint.x = (source.pos.x + target.pos.x) / 2;
			controlPoint.y = from.y - 2 * config.dim;

			label.style.top = controlPoint.y + "px";
			label.style.left = controlPoint.x + "px";
			label.style.fontSize = config.labelSize + "px";
			label.style.width = label.clientWidth + "px";
			label.style.color = config.edgeLabelColor;

			// add signal badge and list, if signals are present
			var signals = source.data.signals;
			if (signals && signals.length > 0) {
				var signalBadge = createSignalBadge(signals);
				var signalListElt = createSignalList(config, target, signals, signalBadge, tokenCount);
				label.appendChild(signalListElt);
				label.appendChild(signalBadge);
			}
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
			 * Calls the necessary functions to plot edges and to place labels.
			 */
			this.init = function() {
				layoutTree(this.config);
				initWrapper(this.config);
				initCanvas(this.config);
				injectStyles(this.config);
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
