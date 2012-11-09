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
      posy = Math.round(pos.y + posToNode.y) / 2 * sy + oy - 20;

      if(dim.align == "center") {
        // look, if the label have to be placed right or under
        // the edge
        if (fromNode._depth == toNode._depth)
        {
          labelPos= {
            x: Math.round(posx - (w / 2) + radius.width/2),
            y: Math.round(posy - 20 + radius.height/2)
          };
        } else {
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

  // add some methods to Node
  $jit.Graph.Node.implement({
    // searching recursive for the most left/right child
    'getMostChild' : function(dir)
    {
      var children = this.getSubnodes([1, 1]);

      if (children.length == 0)
      {
        return this;
      }
      else {
        if ('left' === dir)
        {
          return children[0].getMostChild(dir);
        }
        else if ('right' === dir)
        {
          return children[children.length - 1].getMostChild(dir);
        } else {
          throw "direction not implemented";
        }
      }
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
          this.edgeTypes.edgeLabel.drawHorizontalEdge(fromNode, dim, canvas,
            this.viz, orn);
          this.edgeTypes.edgeLabel.drawVerticalEdge(toNode, dim, canvas,
            this.viz, orn);
        }
      },

      'drawHorizontalEdge' : function(node, dim, canvas, viz, orn)
      {
        var mostLeftChild = node.getMostChild('left'),
        mostRightChild = node.getMostChild('right'),
        posNode = node.pos.getc(true),
        posLeftChild = mostLeftChild.pos.getc(true),
        posRightChild = mostRightChild.pos.getc(true),
        w = node.getData('width') / 2,
        h = node.getData('height') / 2,
        ctx = canvas.getCtx();

        ctx.moveTo(posLeftChild.x - w, posNode.y);
        ctx.lineTo(posRightChild.x + w, posNode.y);
        ctx.stroke();
      },

      'drawVerticalEdge' : function(node, dim, canvas, viz, orn)
      {
        var parents = node.getParents();

        // only one parent should exist
        if (parents.length != 1)
        {
          return;
        }

        var posNode = node.pos.getc(true),
        posParent = parents[0].pos.getc(true),
        ctx = canvas.getCtx();

        ctx.moveTo(posNode.x, posNode.y);
        ctx.lineTo(posNode.x, posParent.y);
        ctx.stroke();
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

        var controllPoint = {};
        if (from.x != to.x)
        {
          controllPoint.x = (from.x + to.x) / 2;
          controllPoint.y = from.y - 2 * dim;
        } else {
          controllPoint.x = from.x + 2 * dim;
          controllPoint.y = (from.y + to.y) / 2;
        }


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
        ctx.quadraticCurveTo(controllPoint.x, controllPoint.y, to.x, to.y, to.x, to.y);
        ctx.stroke();

        // draw arrow
        var vect = new $jit.Complex(to.x - controllPoint.x, to.y - controllPoint.y);
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
    if (data.sentence_left != undefined && data.sentence_right != undefined)
    {
      label.innerHTML = data.sentence_left + ' - ' + data.sentence_right + '<br/>';
    }

    if(data.sentence)
    {
      label.innerHTML += data.sentence;
    }

    label.onclick = function(){
      st.onClick(node.id);
    };
  };

  //set animation transition type
  config['transition'] = $jit.Trans.Quart.easeInOut;

  st = new $jit.ST(config)
  return st;
}
