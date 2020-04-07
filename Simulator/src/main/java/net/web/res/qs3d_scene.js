'use strict';

var SceneBuilder;

if (typeof SceneBuilder=='undefined') {
SceneBuilder=function(scene,model,scaleFactor) {
  this.scene=scene;
  this.model=model;
  this.scaleFactor=scaleFactor;
  this.modePlay=false;
  this.animationRunning=false;
  
  this.stations={};
  this.fontObj=new THREE.Font(fontOpenSans);
  this.buildTextLater=[];
  this.icons=[];
}

SceneBuilder.prototype.add=function(geometry, color, colorFrame, center) {
  var scene=this.scene;
  
  var material;
  var group=new THREE.Object3D();
  
  material=new THREE.MeshStandardMaterial({color: color, transparent: true, opacity: 0.75});
  var mesh1=new THREE.Mesh(geometry,material);
  mesh1.castShadow=true;
  mesh1.receiveShadow=true;
  group.add(mesh1);
  
  if (colorFrame!=null) {
    material=new THREE.MeshBasicMaterial({color: colorFrame, wireframe: true});
    var mesh2=new THREE.Mesh(geometry,material);
	group.add(mesh2);
  }
  
  group.position.x=center[0];
  group.position.y=center[1];
  group.position.z=center[2];
  scene.add(group);
  return group;
}

SceneBuilder.prototype.buildText=function(x,y,z,size,text,color) {
  this.buildTextLater.push({x:x, y:y, z:z, size:size, text:text, color:color});
}

SceneBuilder.prototype.buildTextNow=function(x,y,z,size,text,color) {
  var geometry=new THREE.TextGeometry(text,{font: this.fontObj, size: size, height: 1});
  var material=new THREE.MeshStandardMaterial({color: color});
  var mesh=new THREE.Mesh(geometry,material);
  geometry.center();
  mesh.position.set(x,y,z);
  this.scene.add(mesh);
}

SceneBuilder.prototype.buildTextsNow=function() {
  for (var i=0;i<this.buildTextLater.length;i++) {
    var text=this.buildTextLater[i];
	this.buildTextNow(text.x,text.y,text.z,text.size,text.text,text.color);
  }
}

SceneBuilder.prototype.scale=function(size) {
  return {x: size.x*this.scaleFactor, y: size.y*this.scaleFactor, w: size.w*this.scaleFactor, h: size.h*this.scaleFactor};
}

SceneBuilder.prototype.buildModel=function() {
  var scene=this.scene;
  var model=this.model;
  
  var geometry;
  
  for (var i=0;i<model.elementTypes.length;i++) {
	var size=model.getSize(model.elementObjects[i]);
	var type=model.elementTypes[i];
	var id=model.elementIds[i];
	var name=model.getName(model.elementObjects[i]);
	var edge=model.getEdge(model.elementObjects[i]);
	
	if (size!=null) {

      if (type.startsWith(Language.xmlModelElementAnimationStartsWith)) continue;
	  if (type==Language.xmlModelElementEllipse) continue;
	  if (type==Language.xmlModelElementLine) continue;
	  
	  size=this.scale(size);
	  
	  /* Text */
	  if (type==Language.xmlModelElementText) {
		var lines=model.getAll(model.elementObjects[i],Language.xmlModelElementTextLine);
		var fontSize=10;  
		var fontData=model.getAny(model.elementObjects[i],Language.xmlModelElementFontSize);
		if (fontData!=null && typeof(fontData.xmlcontent)!='undefined') fontSize=parseInt(fontData.xmlcontent)*3;
		var count=0;
		for (var j=0;j<lines.length;j++) if (typeof(lines[j].xmlcontent)!='undefined') count++;
		var nr=0;
		var x=size.x+size.w/2;
		var z=size.y+size.h/2;
		for (var j=0;j<lines.length;j++) if (typeof(lines[j].xmlcontent)!='undefined') {
		  nr++;
		  name=lines[j].xmlcontent;
          this.buildText(x,fontSize*(1+count-nr),z,fontSize,name,0x0000ff);		  
		}
		continue;
	  }
		  
	  /* Rechteck */
	  if (type==Language.xmlModelElementRectangle) {
	    geometry=new THREE.BoxGeometry(size.w,10,size.h);
	    var x=size.x+size.w/2;
	    var y=size.y+size.h/2;
        this.add(geometry,0x808080,null,[x,5,y]);
		continue;
	  }
	  
	  /* Station */	  
      geometry=new THREE.BoxGeometry(size.w,100,size.h);
	  var x=size.x+size.w/2;
	  var y=size.y+size.h/2;
      this.add(geometry,0x00dddd,null,[x,50,y]);
	  this.stations[id]={x:x,y:y};
	  var display=type;
	  if (type!=Language.xmlModelElementVertex) {
	    if (display.startsWith(Language.xmlModelElementStartsWith)) display=display.substr(Language.xmlModelElementStartsWith.length);
		if (name==null || name=="") {
	      this.buildText(x,140,y,40,display,0xffffff);
		} else {
		  this.buildText(x,190,y,20,display,0xffffff);
		  this.buildText(x,140,y,40,name,0xffffff);
		}
	  }
	  continue;
	}
	
	if (edge!=null) {
		/* Verbindungskante */
		var index1=model.elementIds.indexOf(edge[0]);
		var index2=model.elementIds.indexOf(edge[1]);
		if (index1>=0 && index2>=0) {
		  var size1=model.getSize(model.elementObjects[index1]);
		  var size2=model.getSize(model.elementObjects[index2]);
		  if (size1!=null && size2!=null) {
			size1=this.scale(size1);
			size2=this.scale(size2);
			var x1=size1.x+size1.w/2;
			var y1=size1.y+size1.h/2;
			var x2=size2.x+size2.w/2;
			var y2=size2.y+size2.h/2;
			var delta=Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));			
			geometry=new THREE.CylinderGeometry(1*this.scaleFactor,8*this.scaleFactor,delta,20);
			var group=this.add(geometry,0x0000ff,0x770077,[(x1+x2)/2,50,(y1+y2)/2])
			group.rotation.x=Math.PI/2;
			group.rotation.z=-Math.atan2(x2-x1,y2-y1);
		  }
		}
	}
  }  
}

SceneBuilder.prototype.build=function() {
  this.buildModel();
 
  var that=this;
  
  setTimeout(function() {
	  that.buildTextsNow();
	  that.requestStatus();
  },10);
}

SceneBuilder.prototype.requestStep=function() {
  var that=this;
	
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {if (this.readyState==4 && this.status==200) that.processStepResponse(this);}
  xhttp.ontimeout=function() {alert(stepTimeout);}
  xhttp.open("GET","/animation?command=step",true);
  xhttp.send();
}

SceneBuilder.prototype.processStepResponse=function(response) {
  if (response.responseText!=null && response.responseText!="") {
	  alert(response.responseText);
	  return;
  }	
  this.requestStatus();
}

SceneBuilder.prototype.requestStatus=function() {
  var that=this;
	
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {if (this.readyState==4 && this.status==200) that.processStatusResponse(this);}
  xhttp.ontimeout=function() {alert(stepTimeout);}
  xhttp.open("GET","/animation?command=status",true);
  xhttp.send();
}

SceneBuilder.prototype.removeOldIcons=function() {
  for(var i=0;i<this.icons.length;i++) {
	var icon=this.icons[i];
	this.scene.remove(icon);
	icon.geometry.dispose();
	icon.material.map.dispose();
	icon.material.dispose();	
  }
	
  this.icons=[];
}

SceneBuilder.prototype.addStaticIcon=function(icon) {
	var scene=this.scene;
	
	var x=icon.x*this.scaleFactor;
	var y=icon.y*this.scaleFactor;
	var w=icon.w*this.scaleFactor;
	var h=icon.h*this.scaleFactor;
	  
	var geometry=new THREE.BoxGeometry(w,50,h);
	var texture=new THREE.TextureLoader().load(icon.icon);
	texture.minFilter=THREE.LinearFilter;
	var material=new THREE.MeshStandardMaterial({map: texture});
    var mesh=new THREE.Mesh(geometry,material);
	mesh.castShadow=true;
	mesh.receiveShadow=true;
	
	mesh.position.x=x+w/2;
	mesh.position.y=125;
	mesh.position.z=y+h/2;
	
	scene.add(mesh);
	this.icons.push(mesh);
}

SceneBuilder.prototype.getPoint=function(point1, point2, level) {
	var x1=(parseInt(point1.x)+parseInt(point1.w)/2)*this.scaleFactor;
	var y1=(parseInt(point1.y)+parseInt(point1.h)/2)*this.scaleFactor;
	var x2=(parseInt(point2.x)+parseInt(point2.w)/2)*this.scaleFactor;
	var y2=(parseInt(point2.y)+parseInt(point2.h)/2)*this.scaleFactor;
	return {x:x1*(1-level)+x2*level, y:y1*(1-level)+y2*level};
}

SceneBuilder.prototype.addMovingIcon=function(icon) {
  if (typeof(icon)=='undefined' || typeof(icon.count)=='undefined' || parseInt(icon.count)<2) return;
  
  var steps=5;
  var path=[];
  path.push(this.getPoint(icon[0],icon[1],0.0));
  for (var i=0;i<parseInt(icon.count)-1;i++) {
	  for (var j=1;j<=steps;j++) path.push(this.getPoint(icon[i],icon[i+1],j/steps));
  }
  	
  var geometry=new THREE.BoxGeometry(icon[0].w*this.scaleFactor,50,icon[0].h*this.scaleFactor);
  var texture=new THREE.TextureLoader().load(icon[0].icon);
  texture.minFilter=THREE.LinearFilter;
  var material=new THREE.MeshStandardMaterial({map: texture});
  var mesh=new THREE.Mesh(geometry,material);
  mesh.castShadow=true;
  mesh.receiveShadow=true;
  
  mesh.position.y=125;
  
  var moveData={path: path, mesh: mesh};
  this.scene.add(mesh);

  this.moveIcon(moveData);  
}

SceneBuilder.prototype.moveIcon=function(icon) {
  this.animationRunning=true;
  var that=this;
	
  if (icon.path.length==0) {
    /* Icon zu statischem Icon überführen */
	this.icons.push(icon.mesh);
	this.animationRunning=false;
	if (this.modePlay) setTimeout(function(){that.requestStep();});
	return;
  }
  
  var pos=icon.path.shift();
  icon.mesh.position.x=pos.x;
  icon.mesh.position.z=pos.y;
  
  setTimeout(function(){that.moveIcon(icon);},25);  
}

SceneBuilder.prototype.processStatusResponse=function(response) {
  /* Alte Icons entfernen */
  this.removeOldIcons();
	
  var json=JSON.parse(response.responseText);
  var nr;
  
  /* Statische Bilder */
  nr=1;
  while (typeof(json.staticImages[nr])!='undefined') {
	this.addStaticIcon(json.staticImages[nr]);
	nr++;
  }
  	  
  /* Dynamische Bilder */
  nr=1;
  while (typeof(json.movingImages[nr])!='undefined') {
	  this.addMovingIcon(json.movingImages[nr]);
	  nr++;
  }
  
	if (this.modePlay && !this.animationRunning) {
		var that=this;
		setTimeout(function(){that.requestStep();});
	}
}

SceneBuilder.prototype.quit=function() {
  var xhttp=new XMLHttpRequest();
  xhttp.open("GET","/animation?command=quit",true);
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {if (this.readyState==4) document.location="/";}
  xhttp.ontimeout=function() {document.location="/";}
  xhttp.send();
}

SceneBuilder.prototype.playPause=function(button) {
  this.modePlay=!(this.modePlay || this.animationRunning);
  
  if (this.modePlay) {
    button.innerHTML=Language.pause;
    this.requestStep();
  } else {
    button.innerHTML=Language.play; 
  }
}

}