/*!
 * vakyartha dependency script 1.1
 * http://arborator.ilpga.fr/
 *
 * Copyright 2010, Kim Gerdes
 *
 * This program is free software:
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

///////////////////////// parameters ////////////////
////////////////////////////////////////////////////
tab=8;  // space between tokens
line=25;  // line height
dependencyspace=180;  // y-space for dependency representation
xoff=8; // placement of the start of a depdency link in relation to the center of the word
linedeps=15;  // y distance between two arrows arriving at the same token (rare case)
pois=4; // size of arrow pointer
tokdepdist=15;  // distance between tokens and depdendency relation
funccurvedist=8;  // distance between the function name and the curves highest point
depminh = 20; // minimum height for dependency
worddistancefactor = 8; // distant words get higher curves. this factor fixes how much higher.
defaultattris={
  "font": '14px "Arial"',
  "text-anchor":'start'
};
attris = {
  "t":  {
    "font": '18px "Arial"',
    "text-anchor":'start'
  },
  "cat": {
    "font": '14px "Times"',
    "text-anchor":'start',
    "fill": '#036'
  },
  "lemma": {
    "font": '14px "Times"',
    "text-anchor":'start',
    "fill": '#036'
  },
  "depline": {
    "stroke": '#999',
    "stroke-width":'1'
  },
  "deptext": {
    "font": '12px "Times"',
    "font-style":'italic',
    "fill": '#999'
  },
  "form": {
    "font-style":'italic'
  }
};

///////////////////////// node object and functions ////////////////
////////////////////////////////////////////////////////////////////
function Pnode(c,b){
  this.index=c;
  this.govs={};

  if("govs"in b) {this.govs=b["govs"];}
  this.width=0;
  this.texts=new Object();
  this.svgs=new Object();
  var d=dependencyspace;
  for (var h in shownfeatures){
    var e=shownfeatures[h];
    this.texts[e]= b[e];
    var f=paper.text(currentx,d,b[e]);
    f.attr(defaultattris);
    f.attr("title", b.tooltip);
    if(e in b)f.attr(attris[e]);
    if("attris"in b)f.attr(b["attris"][e]);
    var g=f.getBBox().width;
    f.width=g;
    f.index=c;
    this.svgs[e]=f;
    if(g>this.width)this.width=g;
    d=d+line;
  }

  svgwi=svgwi+this.width+tab;
  this.x=0;
  this.y=0;
  this.svgdep={};

  this.deplineattris={};
  this.deptextattris={};

  if("attris"in b&&"depline"in b["attris"])this.deplineattris=b["attris"]["depline"];
  if("attris"in b&&"deptext"in b["attris"])this.deptextattris=b["attris"]["deptext"];
}

drawsvgDep=function(c,b,d,h,e,f,g,i,p){
  var l=paper.set();
  var q=Math.abs(d-e)/2;
  var m=Math.max(h-q-worddistancefactor*Math.abs(c-b),-tokdepdist);
  m=Math.min(m,h-depminh);
  var r="M"+d+","+h+"C"+d+","+m+" "+e+","+m+" "+e+","+f;
  var j=paper.path(r).attr(attris["depline"]).attr({
    "x":d,
    "y":h
  });
  var n=paper.pointer(e,f,pois,0).attr(attris["depline"]);
  a=j.getPointAtLength(j.getTotalLength()/2);
  t=paper.text(a.x,a.y-funccurvedist,g);
  t.attr(attris["deptext"]);
  t.attr(p);
  t.index=c;
  t.govind=b;
  if(c==b){
    var k=a.x+t.getBBox().width/2+funccurvedist/2;
    if(k+t.getBBox().width/2>svgwi)k=a.x-t.getBBox().width/2-funccurvedist/2;
    if(k-t.getBBox().width/2<0)k=a.x;
    t.attr("x",k);
  }
  if(g in fcolors){
    var o="#"+fcolors[g];
    j.attr({
      stroke:o
    });
    n.attr({
      stroke:o
    });
    t.attr({
      fill:o
    });
  }

  j.attr(i);
  n.attr(i);
  l.push(t);
  l.push(j);
  l.push(n);
  return l;
};

drawDep=function(c,b,d,h)
{
  c=parseInt(c);
  var e;
  var f;
  var g;
  var i;
  if(b=="root")
  {
    b=c;
    node2=words[c];
    e=node2.svgs[shownfeatures[0]].attr("x")+node2.svgs[shownfeatures[0]].width/2;
    f=node2.svgs[shownfeatures[0]].attr("y")-tokdepdist;
    g=e;
    i=0;
  }
  else
  {
    b=parseInt(b);
    var node1=words[b];
    var node2=words[c];
    if(node1==null)
    {
      delete node2.govs[b];
      return;
    }
    if(c<b)
    {
      g=node1.svgs[shownfeatures[0]].attr("x")+node1.svgs[shownfeatures[0]].width/2-xoff;
    }
    else
    {
      g=node1.svgs[shownfeatures[0]].attr("x")+node1.svgs[shownfeatures[0]].width/2+xoff;
    }
    e=node2.svgs[shownfeatures[0]].attr("x")+node2.svgs[shownfeatures[0]].width/2;
    i=node1.svgs[shownfeatures[0]].attr("y")+pois-tokdepdist;
    f=node2.svgs[shownfeatures[0]].attr("y")-tokdepdist-h*linedeps;
  }

  node2.svgdep[b]=drawsvgDep(c,b,g,i,e,f,d,node2.deplineattris,node2.deptextattris);
};

drawalldeps=function(){
  for(var w in words){
    var b=words[w];
    var d=0;
    for(var key in b.govs){
      drawDep(b.index,key,b.govs[key],d);
      d+=1;
    }
  }
};

words=new Object();
makewords=function(){
  svgwi=0;
  currentx=tab;
  for(var c in tokens){
    var b=new Pnode(c,tokens[c]);
    words[c]=b;
    currentx=currentx+b.width+tab;
  }
};


Raphael.fn.pointer=function(c,b,d,h){
  var e=c+","+(b+d);
  var f="0,0"+(-d/2)+","+(-d*1.5)+" "+(-d/2)+","+(-d*1.5);
  var g=(d/2)+","+(d/2)+" "+(d/2)+","+(d/2)+" "+(d)+",0";
  var i=this.path("M"+e+"c"+f+"c"+g+"z");
  i.rotate(h);
  return i;
};


/**
 * Calls the visualization, written by Kim Gerdes. Do not ask me, what is
 * happening here
 */
function drawDependenceTree()
{
  paper=Raphael("holder",window.innerWidth-100,100);
  svgpos=$("svg").offset();
  makewords();
  $("#holder").attr("style","background:white; position:relative;margin:0px; padding:0px");
  $("svg")[0].setAttribute("width",svgwi);
  $("svg")[0].setAttribute("height",dependencyspace+shownfeatures.length*line);
  drawalldeps();
}

$(function(){
  drawDependenceTree();
});