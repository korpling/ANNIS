/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function initSpaceTree(config) {
  // save this reference for reading inner fields in functions
  var saveThis = this;

  // is used for building unique ids for edge Labels
  var EDGE_LABEL_PREFIX = "edge_";

  // the visualization reference
  var st;

  // Override the hiding method, because we want to hide edge labels
  // too.
  $jit.ST.Label.HTML.implement({
    hideLabel: function(node, show) {
      node = $jit.util.splat(node);
      var st = show ? "" : "none", lab, that = this;
      $jit.util.each(node, function(n) {
        var lab = that.getLabel(n.id);
        if (lab) {
          lab.style.display = st;
        }
        lab = that.getLabel(EDGE_LABEL_PREFIX + n.id);
        if(lab) {
          lab.style.display = st;
        }
      });
    }
  });



  // Override label rendering, add edge labels. It assumes that,
  // there is only one pointing relation.
  $jit.ST.Label.HTML.implement({

    'plotLabel' : function(canvas, node, controller) {

      var id = node.id,
      fromNode = node,
      toNode = node.data.edges.length > 0 ? st.graph.getNode(node.data.edges[0].to) : {},
      tag = this.getLabel(id),
      tag_edge = this.getLabel(EDGE_LABEL_PREFIX + id);

      if(!tag && !(tag = document.getElementById(id))) {
        tag = document.createElement('div');
        var container = this.getLabelContainer();
        tag.id = id;
        tag.className = 'node';
        tag.style.position = 'absolute';
        controller.onCreateLabel(tag, node);
        container.appendChild(tag);
        this.labels[node.id] = tag;
      }

      this.placeLabel(tag, node, controller);

      if(!tag_edge
        && !(tag_edge = document.getElementById(EDGE_LABEL_PREFIX+id))
        && node.data.edges.length > 0)
        {
        tag_edge = document.createElement('div');
        var container = this.getLabelContainer();

        tag_edge.id = EDGE_LABEL_PREFIX+id;
        tag_edge.className = 'edge';
        tag_edge.style.position = 'absolute'
        container.appendChild(tag_edge);
        tag_edge.innerHTML = node.data.edges[0].annotation[0];
        // set styles
        tag_edge.style.align = node.getLabelData('textAlign');
        tag_edge.style.font = node.getLabelData('style') + ' ' + node.getLabelData('size') + 'px ' + node.getLabelData('family');

        this.labels[EDGE_LABEL_PREFIX+node.id] = tag_edge;
      }

      if (tag_edge)
      {
        this.placeEdgeLabel(tag_edge, fromNode, toNode, controller);
      }

    },

    'placeEdgeLabel' : function(tag, fromNode, toNode, controller)
    {
      var pos = fromNode.pos.getc(true),
      posToNode = toNode.pos.getc(true),
      config = this.viz.config,
      dim = config.Node,
      canvas = this.viz.canvas,
      w = fromNode.getData('width'),
      h = fromNode.getData('height'),
      radius = canvas.getSize(),
      labelPos, orn;

      var ox = canvas.translateOffsetX,
      oy = canvas.translateOffsetY,
      sx = canvas.scaleOffsetX,
      sy = canvas.scaleOffsetY,
      posx = Math.round(pos.x + posToNode.x) / 2 * sx + ox,
      posy = Math.round(pos.y + posToNode.y) / 2 * sy + oy;

      if(dim.align == "center") {
        // look, if the label have to be placed right or under
        // the edge
        if (fromNode._depth == toNode._depth)
        {
          labelPos= {
            x: Math.round(posx - (w / 2) + radius.width/2),
            y: Math.round(posy - 20 + radius.height/2)
          };
        } else
{
          labelPos= {
            x: Math.round(posx + 20 + radius.width/2),
            y: Math.round(posy + radius.height/2)
          };

        }
      } else if (dim.align == "left") {
        orn = config.orientation;
        if(orn == "bottom" || orn == "top") {
          labelPos= {
            x: Math.round(posx - w / 2 + radius.width/2),
            y: Math.round(posy + radius.height/2)
          };
        } else {
          labelPos= {
            x: Math.round(posx + radius.width/2),
            y: Math.round(posy - h / 2 + radius.height/2)
          };
        }
      } else if(dim.align == "right") {
        orn = config.orientation;
        if(orn == "bottom" || orn == "top") {
          labelPos= {
            x: Math.round(posx - w / 2 + radius.width/2),
            y: Math.round(posy - h + radius.height/2)
          };
        } else {
          labelPos= {
            x: Math.round(posx - w + radius.width/2),
            y: Math.round(posy - h / 2 + radius.height/2)
          };
        }
      } else throw "align: not implemented";



      var style = tag.style;
      style.left = labelPos.x + 'px';
      style.top  = labelPos.y + 'px';
      style.display = this.fitsInCanvas(labelPos, canvas)? '' : 'none';
      controller.onPlaceLabel(tag, fromNode);
    }
  });

  //implement an edge type
  $jit.ST.Plot.EdgeTypes.implement({
    'edgeLabel': {
      'render': function(adj, canvas) {
        var edges = adj.nodeTo.data.edges,
        invisibleRelations = adj.nodeFrom.data.invisibleRelations;
        fromNode = adj.nodeFrom,
        toNode = adj.nodeTo,
        isAlreadyConnected = false;


        for (var i = 0; i < edges.length; i++)
        {
          var edge = edges[i];
          var isPointingRel = edge.annotation.reduce(
            function(x,y)
            {
              return x && !(y == "span" || y == "multinuc");
            }, true);

          if(isPointingRel)
          {
            var orn = this.getOrientation(adj),
            node = st.graph.getNode(edge.to),
            child = st.graph.getNode(edge.from),
            dim = adj.getData('dim');
            this.edgeTypes.edgeLabel.drawArrow(node, child, dim,
              true, canvas,
              this.viz, orn);

            if (node === fromNode && child === toNode
              || node === toNode && child == fromNode)
              {
              isAlreadyConnected = true;
            }
          }
        }

        // if toNode is member of the invisibleRelation
        // property plot nothing
        for (var j = 0; j < invisibleRelations.length; j++)
        {
          if (invisibleRelations[j] === toNode.id)
          {
            return;
          }
        }

        if (!isAlreadyConnected)
        {
          this.edgeTypes.line.render.call(this, adj, canvas);
        }
      },
      'drawArrow' : function (node, child, dim, swap, canvas, viz, orn)
      {
        var ctx = canvas.getCtx();

        // invert edge direction
        if (swap) {
          var tmp = node;
          node = child;
          child = tmp;
        }

        var from = viz.geom.getEdge(node, 'end', orn),
        to = viz.geom.getEdge(child, 'end', orn);

        posNode = node.pos.getc(true);
        posChild = child.pos.getc(true);

        //TODO check orientation
        if (posNode.y == posChild.y)
        {
          if (posNode.x < posChild.x)
          {
            var tmp = from;
            from = to;
            to = tmp;
          }
        }
        else {
          to = viz.geom.getEdge(child, 'begin', orn);
        }

        //draw line
        ctx.beginPath();
        ctx.moveTo(from.x, from.y);
        ctx.bezierCurveTo(from.x, from.y, to.x, to.y, to.x, to.y);
        ctx.stroke();

        // draw arrow
        var vect = new $jit.Complex(to.x - from.x, to.y - from.y);
        vect.$scale(dim / vect.norm());
        var intermediatePoint = new $jit.Complex(to.x - vect.x, to.y - vect.y),
        normal = new $jit.Complex(-vect.y / 2, vect.x / 2),
        v1 = intermediatePoint.add(normal),
        v2 = intermediatePoint.$add(normal.$scale(-1));

        ctx.beginPath();
        ctx.moveTo(v1.x, v1.y);
        ctx.lineTo(v2.x, v2.y);
        ctx.lineTo(to.x, to.y);
        ctx.closePath();
        ctx.fill();
      }
    }
  });

  //This method is called on DOM label creation.
  //Use this method to add event handlers and styles to
  //your node.
  config['onCreateLabel'] = function(label, node){

    label.id = node.id;

    //set label styles
    var style = label.style;
    style.width = 60 + 'px';
    style.height = 17 + 'px';
    style.cursor = 'pointer';
    style.color = '#333';
    style.fontSize = '0.8em';
    style.textAlign= 'center';
    style.paddingTop = '3px';

    // mark the label with color
    if (node.data.color)
    {
      label.style.color = node.data.color;
    }

    // put the sentences or sentences number into the label
    var data = node.data;
    if(data.sentence)
    {
      label.innerHTML = data.sentence;
    }
    else if (data.sentence_left != undefined && data.sentence_right != undefined)
    {
      label.innerHTML = data.sentence_left + ' - ' + data.sentence_right;
    }
    else {
      label.innerHTML = node.name;
    }

    label.onclick = function(){
      st.onClick(node.id);
    };
  };

  //This method is called right before plotting
  //a node. It's useful for changing an individual node
  //style properties before plotting it.
  //The data properties prefixed with a dollar
  //sign will override the global node style properties.
  config['onBeforePlotNode'] = function(node){

    // if node contains a sentence
    if (node.data.sentence)
    {
      return;
    }

    //add some color to the nodes in the path between the
    //root node and the selected node.
    if (node.selected) {
      node.data.$color = "#ff7";
    } else {
      delete node.data.$color;
      //if the node belongs to the last plotted level
      if(!node.anySubnode("exist")) {
        //count children number
        var count = 0;
        node.eachSubnode(function(n) {
          count++;
        });
        //assign a node color based on
        //how many children it has
        node.data.$color = ['#aaa', '#baa', '#caa', '#daa', '#eaa', '#faa'][count];
      }
    }
  };

  //This method is called right before plotting
  //an edge. It's useful for changing an individual edge
  //style properties before plotting it.
  //Edge data proprties prefixed with a dollar sign will
  //override the Edge global style properties.
  config['onBeforePlotLine'] = function(adj){
    if (adj.nodeFrom.selected && adj.nodeTo.selected) {
      adj.data.$color = "#000";
      adj.data.$lineWidth = 3;
    }
    else {
      delete adj.data.$color;
      delete adj.data.$lineWidth;
    }
  };

  //set animation transition type
  config['transition'] = $jit.Trans.Quart.easeInOut;

  st = new $jit.ST(config)
  return st;
}
