/* Globale Initialisierungen für sankey.js */

var parallelrendering=false;
var fixedlayout=[];

/* Neue Klasse */

function SankeyProcessor(chartElementID,data) {
  /* Parameter speichern */
  this.chartElement=document.getElementById(chartElementID);
  this.chartElement3D=d3.select("#chart");
  this.data=data;
  
  /* Element vorkonfigurieren */
  this.chartElement.style.position="relative";
  this.chartElement.style.width="98%";
  this.chartElement.style.height="98%";
  
  /* Größe berechnen */
  this.width = this.chartElement.offsetWidth - this.margin.left - this.margin.right,
  this.height = this.chartElement.offsetHeight - this.margin.bottom - this.margin.top;

  /* Objekte erstellen */  
  this.svgOuter = this.chartElement3D.append("svg");
  this.svgOuter.append("rect").attr("x",0).attr("y",0).attr("width","100%").attr("height","100%").attr("fill","white");
  this.svg=this.svgOuter.attr("width", this.width + this.margin.left + this.margin.right).attr("height", this.height + this.margin.top + this.margin.bottom).append("g").attr("transform", "translate(" + this.margin.left + "," + this.margin.top + ")");
  this.sankey = d3.sankey().nodeWidth(30).nodePadding(10).size([this.width, this.height]);
  this.sankey.nodes(this.data.nodes).links(this.data.links);
  this.path = this.sankey.reversibleLink();
  
  /* Einstellungen zur Beschriftung usw. */
  german = d3.formatLocale({decimal: ",", thousands: ".", grouping: [3], currency: ["","&euro;"]});
  this.color = d3.scaleOrdinal(d3.schemeCategory20);
  this.linkFormat = function(a) {return german.format(",.0f")(a);},
  this.nodeFormat = function(a) {return german.format(",.0f")(a);};
  
  /* Mehr Einstellungen */     
  this.minNodeWidth = 50;
  this.lowOpacity = 0.3;
  this.highOpacity = 0.7;
  this.labelNumbers=true;
  this.labelText=true;
  this.showLinkCount=false;
  this.yMove=true;
  this.xMove=true;
    
  /* Start */
  var that=this;
  window.onresize=function(event) {that.resize();}
  this.resize();
}

SankeyProcessor.prototype.margin = {top: 70, right: 10, bottom: 30, left: 40}

SankeyProcessor.prototype.resize = function() {
  this.width = this.chartElement.offsetWidth - this.margin.left - this.margin.right,
  this.height = this.chartElement.offsetHeight - this.margin.bottom - this.margin.top;
  this.svgOuter.attr("width",this.width + this.margin.left + this.margin.right);
  this.svgOuter.attr("height", this.height + this.margin.top + this.margin.bottom);
  this.sankey.size([this.width, this.height]);    
  var sizecorrection = Math.max(0, 220 - parseInt(window.innerWidth * 0.2));
  this.chartElement3D.style("width",this.chartElement.offsetWidth-sizecorrection);
  this.draw();
}

SankeyProcessor.prototype.draw = function() {
  /* Quelle: https://sankey.csaladen.es/ */
  this.svg.selectAll("g").remove();
    
  this.sankey.layout(500);  
  
  var that=this;
    
  var g = this.svg.append("g").selectAll(".link").data(this.data.links).enter().append("g").attr("class", "link").sort(function(j, i) {return i.dy - j.dy;});
  var h = g.append("path").attr("d", this.path(0));
  var f = g.append("path").attr("d", this.path(1));
  var e = g.append("path").attr("d", this.path(2));
  g.attr("fill", function(i) {
    if (i.fill) return i.fill; else if (i.source.fill) return i.source.fill; else return i.source.color = that.color(i.source.name.replace(/ .*/, ""));
  }).attr("opacity", this.lowOpacity).on("mouseover", function(d) {
    d3.select(this).style('opacity', that.highOpacity);
  }).on("mouseout", function(d) {
    d3.select(this).style('opacity', that.lowOpacity);
  }).append("title").text(function(i) {
    return i.source.name + " ? " + i.target.name + "\n" + that.linkFormat(i.value)
  });
  
  var c = this.svg.append("g").selectAll(".node").data(this.data.nodes).enter().append("g").attr("class", "node").attr("transform", function(i) {
    return "translate(" + i.x + "," + i.y + ")"
  }).call(d3.drag().on("start", function() {
    this.parentNode.appendChild(this)
  }).on("drag", b));
  c.append("rect").attr("height", function(i) {
    return i.dy
  }).attr("width", this.sankey.nodeWidth()).style("fill", function(i) {
    if (i.fill) return i.color = i.fill;
    else return i.color = that.color(i.name.replace(/ .*/, ""));
  }).style("stroke", function(i) {
    return d3.rgb(i.color).darker(2);
  }).on("mouseover", function(d) {
    that.svg.selectAll(".link").filter(function(l) {return l.source == d || l.target == d;}).transition().style('opacity', that.highOpacity);
  }).on("mouseout", function(d) {
    that.svg.selectAll(".link").filter(function(l) {return l.source == d || l.target == d;}).transition().style('opacity', that.lowOpacity);
  }).on("dblclick", function(d) {
    that.svg.selectAll(".link").filter(function(l) {return l.target == d;}).attr("display", function() {
      if (d3.select(this).attr("display") == "none") return "inline"; else return "none";
    });
  }).append("title").text(function(i) {
    return i.name + "\n" + that.nodeFormat(i.value);			
  });
  c.append("text").attr("x", -6).attr("y", function(i) {return i.dy / 2;}).attr("dy", ".35em").attr("text-anchor", "end").attr("font-size","16px").text(function(i) {
    if (that.labelText){return i.name;} else {return "";}
  }).filter(function(i) {
    return i.x < that.width / 2;
  }).attr("x", 6 + this.sankey.nodeWidth()).attr("text-anchor", "start")
  
  if (this.showLinkCount) c.append("text").attr("x", -6).attr("y", function(i) {
    return i.dy / 2 + 20;
  }).attr("dy", ".35em").attr("text-anchor", "end").attr("font-size","16px").text(function(i) {
    return "? "+(i.targetLinks.length)+" | "+(i.sourceLinks.length)+" ?";
  }).filter(function(i) {
    return i.x < that.width / 2;
  }).attr("x", 6 + this.sankey.nodeWidth()).attr("text-anchor", "start")

  c.append("text").attr("x", function(i) {return -i.dy / 2;}).attr("y", function(i) {return i.dx / 2 + 9;}).attr("transform", "rotate(270)").attr("text-anchor", "middle").attr("font-size","23px").text(function(i) {
    if ((i.dy>that.minNodeWidth)&&(that.labelNumbers)){return that.nodeFormat(i.value);}
  }).attr("fill",function(d){
    return d3.rgb(d["color"]).brighter(2)
  }).attr("stroke",function(d){
    return d3.rgb(d["color"]).darker(2)
  }).attr("stroke-width","1px");
		
  function b(i) { /* dragmove */
    if (that.yMove) {
      if (that.xMove) {
        d3.select(this).attr("transform", "translate(" + (i.x = Math.max(0, Math.min(that.width - i.dx, d3.event.x))) + "," + (i.y = Math.max(0, Math.min(that.height - i.dy, d3.event.y))) + ")")
      } else {
        d3.select(this).attr("transform", "translate(" + i.x + "," + (i.y = Math.max(0, Math.min(that.height - i.dy, d3.event.y))) + ")")
      }
    } else {
      if (that.xMove) {
        d3.select(this).attr("transform", "translate(" + (i.x = Math.max(0, Math.min(that.width - i.dx, d3.event.x))) + "," + i.y + ")")
      }
    }
    that.sankey.relayout();
    f.attr("d", that.path(1));
    h.attr("d", that.path(0));
    e.attr("d", that.path(2))
  };
};

/* Funktion zur Erstellung eines Sankey-Diagramms */
function buildSankey(chartElementID,nodes,links) {
  var nodeObjs=[];
  for (var i=0;i<nodes.length;i++) nodeObjs.push({"name": nodes[i]});
  var data={"nodes": nodeObjs, "links": links }; 
  new SankeyProcessor("chart",data);
}