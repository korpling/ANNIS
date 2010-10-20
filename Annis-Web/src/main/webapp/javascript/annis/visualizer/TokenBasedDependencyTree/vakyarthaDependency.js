/*!
 * vakyartha dependency script 1.0
 * http://arborator.ilpga.fr/
 *
 * Copyright 2010, Kim Gerdes
 *
 * This program is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. http://www.gnu.org/licenses/
 *
 */


///////////////////////// parameters ////////////////
////////////////////////////////////////////////////


tab=8; 			// space between tokens
line=25 		// line height
dependencyspace=180; 	// y-space for dependency representationi
xoff=2; 		// placement of the start of a depdency link in relation to the corner of the word
linedeps=15; 		// distance between two arrows arriving at the same token (rare case)
pois=4; 		// size of arrow pointer
tokdepdist=20; 		// distance between tokens and depdendency relation
depminh = 40; 		// minimum height for dependency
worddistancefactor = 8; // distant words get higher curves. this factor fixes how much higher.

defaultattris={"font": '14px "Arial"', "text-anchor":'start'}

attris = {"t":		{"font": '18px "Arial"', "text-anchor":'start'},
	  "cat":	{"font": '14px "Times"', "text-anchor":'start',"fill": '#036'},
	  "lemma":	{"font": '14px "Times"', "text-anchor":'start',"fill": '#036'},
	  "depline":	{"stroke": 'green',"stroke-width":'1',"stroke-dasharray": ""},
	  "deptext":	{"font": '12px "Times"', "font-style":'italic', "fill": '#999'},
	  "form":	{"font-style":'italic'}
	  };



///////////////////////// node object and functions ////////////////
////////////////////////////////////////////////////////////////////


function Pnode(index,token)
	{
		this.index=index;
		this.govs={};
		if ("govs" in token) this.govs=token["govs"]
		this.width=0;
		this.texts=new Object();
		this.svgs=new Object();
		var currenty=dependencyspace;
		for (var i in shownfeatures)
			{
			var f = shownfeatures[i];
			this.texts[f]=token[f]
			var t = paper.text(currentx, currenty, token[f]);
			t.attr(defaultattris);
			if (f in token) t.attr(attris[f]);
			if ("attris" in token) t.attr(token["attris"][f]);
			var wi=t.getBBox().width;
			t.attr({"wi":wi,"index": i});
			this.svgs[f]=t;
			if (wi>this.width) this.width=wi;
			currenty=currenty+line;
			}
		svgwi=svgwi+this.width+tab;
		this.x=0;
		this.y=0;
		this.svgdep={};		// the svg dependency (the arrow and the function name)
		this.deplineattris={},t.deptextattris={};
		if ("attris" in token && "depline" in token["attris"]) this.deplineattris=token["attris"]["depline"];
		if ("attris" in token && "deptext" in token["attris"]) this.deptextattris=token["attris"]["deptext"];
	}

drawsvgDep = function(ind,govind,x1,y1,x2,y2,func,lineattris,textattris)
	{
		var set=paper.set();
		var x1x2=Math.abs(x1-x2)/2
		var yy = Math.max(y1-x1x2-worddistancefactor*Math.abs(ind-govind),-tokdepdist)
		yy = Math.min(yy,y1-depminh)
		var c = paper.path(attris["depline"]).moveTo(x1, y1).curveTo(x1, yy, x2, yy, x2, y2);
		c.attr({"x":x1,"y":y1});
		var poi = paper.path(attris["depline"]).moveTo(x2-pois, y2-pois).lineTo(x2, y2).lineTo(x2+pois,y2-pois);
		poi.attr({"x":x2-5,"y":y2-5})
		if (func in fcolors)
		      {
			var color = "#"+fcolors[func];
			c.attr({stroke: color});
			poi.attr({stroke: color});
		      }
		c.attr(lineattris);
		poi.attr( lineattris);
		var yy = Math.max(yy,10)
		t = paper.text(x2, y2+3, func);
		t.attr(attris["deptext"]);
		t.attr(textattris);
		t.attr("index",ind);
		t.attr("govind",govind);
		set.push(t); // text
		set.push(c); // curve
		set.push(poi); // pointer
		return set;
	}


drawDep = function(ind,govind,func,c)	// draw dependency of type func from govind to ind
	{
		ind=parseInt(ind)
		if (govind=="root" ) // head of sentence
		{
			govind=ind;
			node2=words[ind];
			var x2=node2.svgs[shownfeatures[0]].attr("x")+node2.svgs[shownfeatures[0]].attr("wi")/2;
			var y2=node2.svgs[shownfeatures[0]].attr("y")-tokdepdist;
			var x1=x2;
			var y1=0;
		}
		else // normal dependency
		{
			govind=parseInt(govind)
			var node1=words[govind];
			var node2=words[ind];
			if (node1==null) {delete node2.govs[govind];return;}
			if (ind<govind) var x1=node1.svgs[shownfeatures[0]].attr("x")+xoff;
			else var x1=node1.svgs[shownfeatures[0]].attr("x")+node1.svgs[shownfeatures[0]].attr("wi")-xoff;
			var x2=node2.svgs[shownfeatures[0]].attr("x")+node2.svgs[shownfeatures[0]].attr("wi")/2;
			var y1=node1.svgs[shownfeatures[0]].attr("y")-5-c*linedeps;
			var y2=node2.svgs[shownfeatures[0]].attr("y")-tokdepdist-c*linedeps;
		}
		node2.svgdep[govind]=drawsvgDep(ind,govind,x1,y1,x2,y2,func, node2.deplineattris, node2.deptextattris);
	}


drawalldeps = function()
	{
	  for (var i in words)
		  {
			  var n = words[i];
			  var c=0
			  for (var i in n.govs)
			  {
				  drawDep(n.index,i,n.govs[i],c);
				  c+=1;
			  }
		  }
	}

////////////////////////// initialisation //////////////////////
////////////////////////////////////////////////////////////////

words = new Object();

makewords = function()
	{
		svgwi=0;
		currentx=tab;
		for (var i in tokens)
			{
			var node = new Pnode( i, tokens[i]);
			words[i]=node;
			currentx=currentx+node.width+tab;
			}
	}


$(function () {
	paper = Raphael("holder", window.innerWidth-100,100);
	svgpos=$("svg").offset();
	makewords();
	$("#holder").attr("style","width:99%;");
	$("svg")[0].setAttribute("width",svgwi);
	$("svg")[0].setAttribute("height",dependencyspace+shownfeatures.length*line);
	drawalldeps();
});
