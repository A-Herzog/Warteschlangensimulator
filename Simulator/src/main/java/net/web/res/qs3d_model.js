'use strict';

var ModelReader;

if (typeof ModelReader=='undefined') {
ModelReader=function(modelJSON) {
  this.nodes=modelJSON[Language.xmlModelRoot].xmlchildren;
  
  this.elements=null;
  for (var i=0;i<this.nodes.length;i++) {
    if (typeof(this.nodes[i][Language.xmlModelElements])!='undefined') {this.elements=this.nodes[i][Language.xmlModelElements].xmlchildren; break;}
  }

  this.elementTypes=[];
  this.elementIds=[];
  this.elementObjects=[];
  if (this.elements!=null) {
	for (var i=0;i<this.elements.length;i++) {
	  var type=this.getType(this.elements[i]);
	  if (type!=null) {
	    var obj=this.elements[i][type];
	    this.elementTypes.push(type);
		this.elementIds.push(parseInt(obj[Language.xmlId]));
		this.elementObjects.push(obj.xmlchildren);
	  }
	}	  
  }
}

ModelReader.prototype.getType=function(xmlElement) {	
  if (typeof(xmlElement)=='undefined') return null;
  for (var p in xmlElement) return p;
  return null;
}

ModelReader.prototype.getAny=function(xmlElementArray,name) {
  if (typeof(xmlElementArray)=='undefined') return null;
  for (var i=0;i<xmlElementArray.length;i++) {
	if (typeof(xmlElementArray[i][name])!='undefined') {
	  return xmlElementArray[i][name];
	}
  }
  return null;
}

ModelReader.prototype.getAll=function(xmlElementArray,name) {
  var result=[];
  if (typeof(xmlElementArray)!='undefined') for (var i=0;i<xmlElementArray.length;i++) {
	if (typeof(xmlElementArray[i][name])!='undefined') {
	  result.push(xmlElementArray[i][name]);
	}
  }
  return result;
}

ModelReader.prototype.getSize=function(xmlElementArray) {
	return this.getAny(xmlElementArray,Language.xmlModelElementSize);
}

ModelReader.prototype.getName=function(xmlElementArray) {
  var name=this.getAny(xmlElementArray,Language.xmlModelElementName);
  if (name==null) return null;
  if (typeof(name.xmlcontent)=='undefined') return null;
  return name.xmlcontent;
}

ModelReader.prototype.getEdge=function(xmlElementArray) {
  var edge=this.getAny(xmlElementArray,Language.xmlModelElementConnection);
  if (edge==null) return null;
  if (typeof(edge[Language.xmlModelElementConnectionElement1])=='undefined') return null;
  if (typeof(edge[Language.xmlModelElementConnectionElement2])=='undefined') return null;
  return [parseInt(edge[Language.xmlModelElementConnectionElement1]),parseInt(edge[Language.xmlModelElementConnectionElement2])];
}

ModelReader.prototype.getModelSize=function() {	
  var maxX=0;
  var maxY=0;
  
  for (var i=0;i<this.elementTypes.length;i++) {
	var size=this.getSize(this.elementObjects[i]);
	if (size!=null) {
		var x=parseInt(size.x)+parseInt(size.w);
		var y=parseInt(size.y)+parseInt(size.h);
		if (x>maxX) maxX=x;
		if (y>maxY) maxY=y;
	}
  }
  return [maxX,maxY];
}

}