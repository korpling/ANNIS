(function (){

    /**
       the following inheritance method is copied from
       http://killdream.github.com/blog/2011/10/understanding-javascript-oop
     */
    // Aliases for the rather verbose methods on ES5
    var descriptor  = Object.getOwnPropertyDescriptor
    , properties  = Object.getOwnPropertyNames
    , define_prop = Object.defineProperty

    // (target:Object, source:Object) → Object
    // Copies properties from `source' to `target'
    function extend(target, source) {
        properties(source).forEach(function(key) {
            define_prop(target, key, descriptor(source, key))
        });

        return target;
    }

    var rst = function (config){

        this.json = config.json;

        this.container = {};

        this.adj = [];

        this.nodes = {};

        this.canvas = {};

        this.config = {
            siblingOffet : 200,
            subTreeOffset : 100,
            nodeWith : 60,
            container : config.container,
            wrapper :  config.wrapper || "wrapper"
        };
    }

    rst.buildAdjazenzArray = function()
        {
            function buildAdjazenzArrayHelper(json, adj) {
                var children = json.children;
                if (children && children.length > 0)
                {
                    for (var child in children)
                    {
                        adj.push({
                            from : json,
                            to : children[child]
                        });
                        buildAdjazenzArrayHelper(children[child], adj);
                    }
                }};
            buildAdjazenzArrayHelper(this.json, this.adj);
        },

    rst.layoutTree = function (f)
    {
        var lastChild;

        function layoutTreeHelper(json, height, config, nodes, f)
        {

            //build hashmap by the way, ugly
            nodes[json.id] = json;

            if (json.children && json.children.length > 0)
            {
                for(var item in json.children)
                {
                    layoutTreeHelper(json.children[item], height +
                                     config.subTreeOffset, config, nodes);
                }

                //center parent
                var left = json.children[0].pos.x,
                right = json.children[json.children.length -1].pos.x,
                pos = {
                    x : Math.floor((left + right) / 2),
                    y : height
                }
                json.pos = pos;

            }  else {
                // calc posititions of the segments
                json.pos = {x : 0, y : height};
                if (lastChild != undefined)
                {
                    json.pos.x += lastChild.pos.x +
                        config.siblingOffet;
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
                return 1 + (json.children.map(getDepthHelper)).reduce(function(x, y){
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
        var depth = this.getDepth(json) * this.config.subTreeOffset + this.config.subTreeOffset;
        return {x : right, y : depth};
    };

    rst.initWrapper = function()
    {
        var container = document.getElementById(this.config.container);

        if (!container)
        {
            container = document.createElement("div");
            container.setAttribute("id", this.config.container);
            document.getElementById(this.config.wrapper).appendChild(container);
        }

        dim = this.getDim(this.json);
        container.style.position = "relative";
        container.style.width = dim.x + "px";
        container.style.height = dim.y + "px";
        container.style.background = "#DCE5EE";
        this.container = container;
    };

    rst.plotNodes = function()
    {

        function plotNodesHelper(json, conf, container) {
            var elem = document.createElement("div");

            container.appendChild(elem);
            //set content
            if (json.data.sentence_left != undefined && json.data.sentence_right !=  undefined)
                elem.innerHTML = json.data.sentence_left + " - " + json.data.sentence_right + "</br>";

            elem.innerHTML += (json.data.sentence != undefined) ? json.data.sentence : "";

            elem.style.position = "absolute";
            elem.style.top = json.pos.y + "px";
            elem.style.left = json.pos.x + "px";
            elem.style.textAlign = "center";
            elem.style.width = conf.nodeWith;

            if (json.children != undefined)
            {
                for (var item in json.children)
                {
                    plotNodesHelper(json.children[item], conf, container);
                }
            }
        }

        plotNodesHelper(this.json, this.config, this.container);
    };

    rst.plotEdges = function(f)
    {
        this.buildAdjazenzArray(this.json);
        var adj = this.adj,
        nodes = this.nodes;

        for (var item in adj) {

            //this.drawLine(canvas, json, children[item]);
            var from = adj[item].from,
            to = adj[item].to;

            // check if this is not member of the invisible relations.
            if (!from.data.invisibleRelations[to.id])
            {
                this.drawRSTRelation(from, to);
            }

            if (to.data.edges.length > 0)
            {
                var edges = to.data.edges;
                for (var i = 0; i < edges.length; i++)
                {
                    var from = edges[i].from,
                    to = edges[i].to;
                    this.drawBezierCurve(nodes[from], nodes[to]);
                    this.plotEdgeLabel(nodes[from], nodes[to],
                                       edges[i].annotation);
                }
            }
        }
    };

    rst.getTopCenter = function(node)
    {
        return (((2* node.pos.x + this.config.nodeWith) / 2));
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
        this.canvas.style.background = "#DCE5EE";
    };

    rst.drawLine = function(source, target)
    {
        var ctx = this.canvas.getContext("2d"),
        fromPosX = this.getTopCenter(source),
        toPosX = this.getTopCenter(target);

        ctx.beginPath();
        ctx.fillStyle = "#000000";
        ctx.lineWidth = '1';
        ctx.moveTo(fromPosX, source.pos.y);
        ctx.lineTo(toPosX, target.pos.y);
        ctx.stroke();
    };

    rst.drawRSTRelation = function(source, target)
    {
        var ctx = this.canvas.getContext("2d"),
        mostLeftChild = this.getMostLeftNode(source).pos.x,
        mostRightChild = this.getMostRightNode(source).pos.x + this.config.nodeWith,
        targetCenterX = this.getTopCenter(target);

        // draw horizontal line
        ctx.beginPath();
        ctx.fillStyle = "#000000";
        ctx.lineWidth = '1';
        ctx.moveTo(mostLeftChild, source.pos.y);
        ctx.lineTo(mostRightChild, source.pos.y);
        ctx.stroke();

        // draw vertical line
        ctx.beginPath();
        ctx.fillStyle = "#000000";
        ctx.lineWidth = '1';
        ctx.moveTo(targetCenterX, source.pos.y);
        ctx.lineTo(targetCenterX, target.pos.y);
        ctx.stroke();
    };

    rst.drawBezierCurve = function(source, target)
    {
        var from = source.pos,
        to = target.pos,
        fromX = this.getTopCenter(source),
        toX = this.getTopCenter(target),
        dim = 15,
        ctx = this.canvas.getContext("2d"),
        controllPoint = {};

        if (fromX != toX)
        {
            controllPoint.x = (fromX + toX) / 2;
            controllPoint.y = from.y - 2 * dim;
        } else {
            controllPoint.x = fromX + 2 * dim;
            controllPoint.y = (from.y + to.y) / 2;
        }

        //draw line
        ctx.beginPath();
        ctx.moveTo(fromX, from.y);
        ctx.quadraticCurveTo(controllPoint.x, controllPoint.y, toX, to.y, toX, to.y);
        ctx.stroke();

        var headlen = 10;   // length of head in pixels
        var angle = Math.atan2(to.y - from.y, to.x - fromX);
        ctx.lineTo(toX - headlen*Math.cos(angle - Math.PI/6), to.y - headlen*Math.sin(angle - Math.PI/6));
        ctx.moveTo(toX, to.y);
        ctx.lineTo(toX - headlen*Math.cos(angle + Math.PI/6), to.y - headlen*Math.sin(angle + Math.PI/6));
        ctx.stroke();
    };

    rst.plotEdgeLabel = function(source, target, annotation)
    {
        var fromX = this.getTopCenter(source),
        toX = this.getTopCenter(target),
        label = document.createElement("label");


        this.container.appendChild(label);
        label.style.position = "absolute";
        label.innerHTML = annotation;

        labelPos = {
            x : (fromX + toX) / 2 - label.offsetWidth / 2,
            y : source.pos.y - 35
        };

        label.style.top = labelPos.y + "px";
        label.style.left = labelPos.x + "px";
    };

    window.$viz = function (config)
    {
        var viz = new rst(config);
        extend(viz, rst);
        console.log(Object.keys(viz));
        return viz;
    }
}());