(function () {

  /**
       the following inheritance method is copied from
       http://killdream.github.com/blog/2011/10/understanding-javascript-oop
   */
  // Aliases for the rather verbose methods on ES5
  var descriptor  = Object.getOwnPropertyDescriptor,
  properties  = Object.getOwnPropertyNames,
  define_prop = Object.defineProperty

  // (target:Object, source:Object) → Object
  // Copies properties from `source' to `target'
  function extend(target, source) {
    properties(source).forEach(function(key) {
      define_prop(target, key, descriptor(source, key))
    });

    return target;
  }

  var rst = function (config) {

    this.json = config.json;

    this.container = {};

    this.adj = [];

    this.nodes = {};

    this.multinucLabels = {};

    this.canvas = {};

    this.config = {
      siblingOffet : config.siblingOffset || 200 ,
      subTreeOffset : config.subTreeOffset || 100,
      nodeWidth : config.nodeWidth  || 60,
      labelSize : config.labelSize || 10,
      container : config.container || "container",
      background : config.background || "#FFFFFF",
      wrapper :  config.wrapper || "wrapper",
      padding : config.padding || "0",
      edgeLabelColor : config.edgeLabelColor || "#14fe14",
      nodeLabelColor : config.nodeLabelColor || "#fe0707"
    };
  }

  rst.buildAdjazenzArray = function()
  {
    function buildAdjazenzArrayHelper(json, adj) {

      if (json.pos != undefined
        && json.data != undefined
        && json.data.edges.length > 0)
        {
        adj.push(json.data.edges);
      }

      if (json.children && json.children.length > 0)
      {
        for (var child in json.children)
        {

          buildAdjazenzArrayHelper(json.children[child], adj);
        }
      }
    }

    buildAdjazenzArrayHelper(this.json, this.adj);
  },

  rst.layoutTree = function (f)
  {
    var lastChild;

    /**
     * If the root has at least one satellite, return true.
     */
    function hasSatellite(json)
    {
      var childrenArray = json["children"];

      for (var i in childrenArray)
      {
        var edges = childrenArray[i].data.edges;
        for (var e in edges)
        {
          if (edges[e].sType ==  "rst")
          {
            return true;
          }
        }
      }

      return false;
    }

    function layoutTreeHelper(json, height, config, nodes, f)
    {

      //build hashmap by the way, ugly, also exclude root
      if (json.data !== undefined)
      {
        nodes[json.id] = json;
      }

      if (json.children && json.children.length > 0)
      {
        for(var item in json.children)
        {
          if (json.data === undefined && !hasSatellite(json))
          {
            layoutTreeHelper(json.children[item], height, config, nodes);
          }
          else{
            layoutTreeHelper(json.children[item], height +
              config.subTreeOffset, config, nodes);
          }
        }

        // exclude root from calculation
        if (json.data !== undefined)
        {
          //center parent
          var left = json.children[0].pos.x,
          right = json.children[json.children.length -1].pos.x,
          pos = {
            x : Math.floor((left + right) / 2),
            y : height
          }

          json.pos = pos;
        }
      } else {
        // calc posititions of the segments
        json.pos = {
          x : 0,
          y : height
        };
        if (lastChild != undefined)
        {
          json.pos.x += lastChild.pos.x + config.siblingOffet;
        }
        lastChild = json;
      }
    }

    layoutTreeHelper(this.json, 0, this.config, this.nodes, f);
  };

  rst.getDepth = function(json)
  {
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
  };

  rst.getMostRightNode = function(json)
  {
    if (json.children == undefined || json.children.length == 0)
    {
      return json;
    }
    else
    {
      return this.getMostRightNode(json.children[json.children.length - 1]);
    }
  };

  rst.getMostLeftNode = function getMostLeftNode(json)
  {
    if (json.children == undefined || json.children.length == 0)
    {
      return json;
    }
    else
    {
      return this.getMostLeftNode(json.children[0]);
    }
  };

  rst.getDim = function(json)
  {
    var right = this.getMostRightNode(json).pos.x + this.config.siblingOffet;
    var depth = this.getDepth(json) * this.config.subTreeOffset;
    return {
      x : right,
      y : depth
    };
  };

  rst.initWrapper = function()
  {
    var container = document.getElementById(this.config.container),
    conf = this.config;

    if (!container)
    {
      container = document.createElement("div");
      container.setAttribute("id", this.config.container);
      document.getElementById(this.config.wrapper).appendChild(container);
    }

    dim = this.getDim(this.json);
    container.style.position = "relative";
    container.style.width = dim.x + "px";
    container.style.height = 0 + "px";
    container.style.background = conf.background;
    container.style.padding = conf.padding + "px";
    this.container = container;
  };

  rst.plotNodes = function()
  {
    var conf = this.config;
    var container = this.container;
    var canvas = this.canvas;

    for (var node in this.nodes)
    {
      var json = this.nodes[node];
      var elem = document.createElement("div");
      container.appendChild(elem);

      if (json.data.sentence_left != undefined && json.data.sentence_right !=  undefined)
      {
        elem.innerHTML = "<p style='color :" + conf.nodeLabelColor + ";'>"
        + (json.data.sentence_left + " - " + json.data.sentence_right) + "</p>";
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
      if  ( top > container.clientHeight)
      {
        container.style.height = top + "px";
        container.setAttribute("height", top + "px");
        canvas.setAttribute("height", top + "px");
      }
    }
  };

  rst.plotEdges = function()
  {
    this.buildAdjazenzArray();
    var adj = this.adj;
    var nodes = this.nodes;

    /**
     * This function draws all edges. It takes into account the blurr effect of
     * canvas.
     */
    this.context.translate(0.5, 0.5);

    for (var item in nodes)
    {
      this.drawHorizontalLine(nodes[item]);
    }

    for (var i in adj) {

      for (var e in adj[i])
      {

        var from = nodes[adj[i][e].from],
        to = nodes[adj[i][e].to],
        edgeType = adj[i][e].sType,
        annotation = adj[i][e].annotation;

        if (edgeType === "rst")
        {
          this.drawBezierCurve(from, to);
          this.plotRSTLabel(from, to, annotation);
        }

        if (edgeType === "multinuc") {
          this.drawVerticalLine(from, to);
          this.plotMultinucLabel(from, annotation);
        }

        if (edgeType === "edge")
        {
          this.drawSpan(from, to);
        }
      }
    }

    this.context.stroke();
  };

  rst.getTopCenter = function(node)
  {
    return (((2* node.pos.x + this.config.nodeWidth) / 2));
  };

  rst.initCanvas = function()
  {
    var wrapperElem = this.container;
    this.canvas = document.createElement('canvas');
    wrapperElem.appendChild(this.canvas);
    this.canvas.style.width = wrapperElem.style.width + "px";
    this.canvas.style.height = wrapperElem.style.height + "px";
    this.canvas.setAttribute("width", wrapperElem.style.width);
    this.canvas.setAttribute("height", wrapperElem.style.height);
    this.canvas.style.position = "relative";

    this.context = this.canvas.getContext("2d");
  };

  rst.drawVerticalLine = function(source, target)
  {
    fromPosX = this.getTopCenter(source),
    toPosX = this.getTopCenter(target);

    this.context.moveTo(fromPosX, source.pos.y);
    this.context.lineTo(toPosX, target.pos.y);
  };

  rst.drawHorizontalLine = function (source)
  {
    var mostLeftChild = this.getMostLeftNode(source).pos.x,
    mostRightChild = this.getMostRightNode(source).pos.x + this.config.nodeWidth;

    this.context.moveTo(mostLeftChild, source.pos.y);
    this.context.lineTo(mostRightChild, source.pos.y);
  };

  rst.drawSpan = function(source, target)
  {
    var targetCenterX = this.getTopCenter(target);

    // draw vertical line
    this.context.moveTo(targetCenterX, source.pos.y);
    this.context.lineTo(targetCenterX, target.pos.y);
  };

  rst.drawBezierCurve = function(source, target)
  {
    var from = source.pos,
    to = target.pos,
    fromX = this.getTopCenter(source),
    toX = this.getTopCenter(target),
    dim = 15,
    controllPoint = {};

    if (fromX != toX)
    {
      controllPoint.x = (fromX + toX) / 2;
      controllPoint.y = from.y - 2 * dim;
    } else {
      controllPoint.x = fromX + 2 * dim;
      controllPoint.y = (from.y + to.y) / 2;
    }

    //draw lines
    this.context.moveTo(fromX, from.y);
    this.context.quadraticCurveTo(controllPoint.x, controllPoint.y, toX, to.y, toX, to.y);


    var headlen = 10;   // length of head in pixels
    var angle = Math.atan2(to.y - from.y, to.x - fromX);
    this.context.lineTo(toX - headlen*Math.cos(angle - Math.PI/6), to.y - headlen*Math.sin(angle - Math.PI/6));
    this.context.moveTo(toX, to.y);
    this.context.lineTo(toX - headlen*Math.cos(angle + Math.PI/6), to.y - headlen*Math.sin(angle + Math.PI/6));
  };

  rst.plotMultinucLabel = function(source, annotation)
  {
    var key = source.id + "::" + annotation;

    //check if this Label is already plotted
    if (this.multinucLabels[key])
    {
      return;
    }

    var label = document.createElement("label");
    this.multinucLabels[key] = label;

    var firstChild = source.children[0],
    lastChild = source.children[source.children.length - 1];

    var fromX = firstChild.pos.x;
    toX = lastChild.pos.x,

    this.container.appendChild(label);
    label.style.position = "absolute";
    label.innerHTML = annotation;
    label.style.fontSize = this.config.labelSize + "px";
    label.style.color = this.config.edgeLabelColor;

    labelPos = {
      x : ((fromX + toX) / 2) + this.config.nodeWidth / source.children.length - (label.offsetWidth / 2),
      y : source.pos.y + this.config.subTreeOffset / 2
    };


    label.style.top = labelPos.y + "px";
    label.style.left = labelPos.x + "px";

  };

  rst.plotRSTLabel = function(source, target, annotation)
  {
    var fromX = this.getTopCenter(source),
    toX = this.getTopCenter(target),
    label = document.createElement("label");

    this.container.appendChild(label);
    label.style.position = "absolute";
    label.innerHTML = annotation;

    labelPos = {
      x : (fromX + toX) / 2 - (label.offsetWidth / 2),
      y : source.pos.y - 35
    };

    label.style.top = labelPos.y + "px";
    label.style.left = labelPos.x + "px";
    label.style.fontSize = this.config.labelSize + "px";
    label.style.color = this.config.edgeLabelColor;
  };

  window.$viz = function (config)
  {
    var viz = new rst(config);
    extend(viz, rst);
    return viz;
  }
}());