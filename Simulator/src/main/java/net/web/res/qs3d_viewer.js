'use strict';

/*
 *
 * Viewer3D
 *
 */

var Viewer3D;

if (typeof Viewer3D=='undefined') {
Viewer3D=function(parent, xCameraRange, yCameraRange, zCameraRange, startCameraPosition) {
  this.parent=parent;
  
  this.xCameraRange=xCameraRange;
  this.yCameraRange=yCameraRange;
  this.zCameraRange=zCameraRange;
  this.startCameraPosition=startCameraPosition;
  
  this.currentFPS=0;

  this.scene=new THREE.Scene();
  this.renderer=new THREE.WebGLRenderer();
  
  this.frameCount=0;
  this.frameCountStart=new Date().getTime();
  
  this.renderer.shadowMap.enabled=true;
  this.renderer.shadowMap.type=THREE.BasicShadowMap;
  
  parent.appendChild(this.renderer.domElement);
  
  this.camera=new THREE.PerspectiveCamera(46.8,this.parent.clientWidth/this.parent.clientHeight,1,20000);
  this.resetCamera();
  
  var that=this;
  window.addEventListener("resize",function(){that.resize();});
  this.resize();
  setTimeout(function(){that.resize();},0);
}

Viewer3D.prototype.resetCamera=function() {	
  this.camera.aspect=this.parent.clientWidth/this.parent.clientHeight;
  this.camera.position.set(this.startCameraPosition[0],this.startCameraPosition[1],this.startCameraPosition[2]);
  this.camera.rotation.set(0,0,0);
  this.camera.updateProjectionMatrix();
}

Viewer3D.prototype.resize=function() {
  this.renderer.setSize(this.parent.clientWidth,this.parent.clientHeight);
  this.camera.aspect=this.parent.clientWidth/this.parent.clientHeight;
  this.camera.updateProjectionMatrix();
}

Viewer3D.prototype.start=function() {
  this.resize();
  this.renderFrame();
}

Viewer3D.prototype.renderFrame=function() {
  var that=this;
  requestAnimationFrame(function(){that.renderFrame();});
  this.renderer.render(this.scene,this.camera);
  
  this.frameCount++;
  var time=new Date().getTime();
  if (time>this.frameCountStart+2000) {
    this.currentFPS=Math.floor(this.frameCount/2);
    this.frameCount=0;
    this.frameCountStart=time;
  }
}

Viewer3D.prototype.addControls=function() {
  this.mouseIsDown=false;
  
  var that=this;
  document.addEventListener("keydown",function(event){that.processKeyEvent(event);});
  document.addEventListener("mousedown",function(event){that.processMouseDown(event);});
  document.addEventListener("mouseup",function(event){that.processMouseUp(event);});
  document.addEventListener("mousemove",function(event){that.processMouseMove(event);});  
  var mousewheelevt=(/Firefox/i.test(navigator.userAgent))?"DOMMouseScroll":"mousewheel";
  document.addEventListener(mousewheelevt,function(event){that.processMouseWheel(event);});
}

Viewer3D.prototype.moveCamera=function(numPadDirection) {
  var rotation=this.camera.rotation.y;
  switch (numPadDirection) {
    case 2: 
      this.camera.position.x+=20*Math.sin(rotation);
      this.camera.position.z+=20*Math.cos(rotation);
	  break;
	case 8:
	  this.camera.position.x-=20*Math.sin(rotation);
	  this.camera.position.z-=20*Math.cos(rotation);
	  break;
	case 4:
	  this.camera.position.x-=20*Math.cos(rotation);
	  this.camera.position.z+=20*Math.sin(rotation);
	  break;
	case 6:
	  this.camera.position.x+=20*Math.cos(rotation);
	  this.camera.position.z-=20*Math.sin(rotation);
	  break;
  }
  this.camera.position.x=Math.max(this.xCameraRange[0],Math.min(this.xCameraRange[1],this.camera.position.x));
  this.camera.position.z=Math.max(this.zCameraRange[0],Math.min(this.zCameraRange[1],this.camera.position.z));
}

Viewer3D.prototype.elevateCamera=function(numPadDirection,value) {
  if (typeof value=='undefined') value=20;
  if (value<0) {value=-value; if (numPadDirection==8) numPadDirection=2; else numPadDirection=8;}
  switch (numPadDirection) {
    case 8:
	  this.camera.position.y=Math.min(this.yCameraRange[1],this.camera.position.y+value);
	  break;
	case 2:
	  this.camera.position.y=Math.max(this.yCameraRange[0],this.camera.position.y-value);
	  break;
  }
}

Viewer3D.prototype.processKeyEvent=function(event) {
  if (event.ctrlKey) {
    switch (event.keyCode) {
	  case 65: /* "A" */
	  case 52: /* "4" */
	  case 100: /* NumPad "4" */
      case 37: /* links */
	    this.camera.rotation.y+=10*Math.PI/180;
		event.preventDefault();
		break; 
	  case 68: /* "D" */
	  case 54: /* "6" */
	  case 102: /* NumPad "6" */
      case 39: /* rechts */
	    this.camera.rotation.y-=10*Math.PI/180;
		event.preventDefault();
		break;
	  case 87: /* "W" */
	  case 56: /* "8" */
	  case 104: /* NumPad "8" */
      case 38: /* hoch */
	    this.elevateCamera(8);
		event.preventDefault();
		break;
	  case 83: /* "S" */
	  case 50: /* "2" */
	  case 98: /* NumPad "2" */
      case 40: /* runter */
        this.elevateCamera(2);
		event.preventDefault();
		break;
    }
	return;
  }
  
  if (event.shiftKey) {
	switch (event.keyCode) {
	  case 65: /* "A" */
	  case 52: /* "4" */
	  case 100: /* NumPad "4" */
      case 37: /* links */
	    this.moveCamera(4);
		this.moveCamera(4);
		this.moveCamera(4);
		event.preventDefault();
		break; 
	  case 68: /* "D" */
	  case 54: /* "6" */
	  case 102: /* NumPad "6" */
      case 39: /* rechts */
	    this.moveCamera(6);
		this.moveCamera(6);
		this.moveCamera(6);
		event.preventDefault();
		break;
	  case 87: /* "W" */
	  case 56: /* "8" */
	  case 104: /* NumPad "8" */
      case 38: /* hoch */
	    this.moveCamera(8);
		this.moveCamera(8);
		this.moveCamera(8);
		event.preventDefault();
		break;
	  case 83: /* "S" */
	  case 50: /* "2" */
	  case 98: /* NumPad "2" */
      case 40: /* runter */
        this.moveCamera(2);
		this.moveCamera(2);
		this.moveCamera(2);
		event.preventDefault();
		break;
    }
	return;
  }

  switch (event.keyCode) {
    case 65: /* "A" */
	case 52: /* "4" */
	case 100: /* NumPad "4" */
    case 37: /* links */
	  this.moveCamera(4);
	  event.preventDefault();
	  break;
	case 68: /* "D" */
	case 54: /* "6" */
	case 102: /* NumPad "6" */
    case 39: /* rechts */
	  this.moveCamera(6);
	  event.preventDefault();
	  break;
	case 87: /* "W" */
	case 56: /* "8" */
	case 104: /* NumPad "8" */
    case 38:  /* hoch */
	  this.moveCamera(8);
	  event.preventDefault();
	  break;
	case 83: /* "S" */
	case 50: /* "2" */
	case 98: /* NumPad "2" */
    case 40: /* runter */
	  this.moveCamera(2);
	  event.preventDefault();
	  break;
	case 48: /* "0" */
	case 96: /* NumPad "0" */
	  this.resetCamera();
	  event.preventDefault();
	  break;
	case 33: /* Bild nach oben */
	  this.camera.rotation.x+=Math.PI/40;
	  event.preventDefault();
	  break;
	case 34: /* Bild nach unten */
	  this.camera.rotation.x-=Math.PI/40;
	  event.preventDefault();
	  break;
	case 35: /* Ende */
	  this.camera.rotation.x=0;
	  event.preventDefault();
	  break;
  }
}

Viewer3D.prototype.processMouseWheel=function(event) {
  var dir=0;
  if (typeof event.deltaY!='undefined') dir=event.deltaY; else {
    if (typeof event.detail!='undefined') dir=event.detail;
  }
  if (dir==0) return;
    
  if (event.ctrlKey) {
	if (dir<0) this.elevateCamera(8); else this.elevateCamera(2);
  } else {
	if (event.shiftKey) {
	  if (dir<0) {this.moveCamera(8); this.moveCamera(8); this.moveCamera(8);} else {this.moveCamera(2); this.moveCamera(2); this.moveCamera(2);}
	} else {
      if (dir<0) this.moveCamera(8); else this.moveCamera(2);
	}
  }
  event.preventDefault();
}

Viewer3D.prototype.processMouseDown=function(event) {
  if (event.button==0) {
    this.mouseIsDown=true;
	this.mouseX=event.screenX;
	this.mouseY=event.screenY;
  }  
}

Viewer3D.prototype.processMouseUp=function(event) {
  if (event.button==0) this.mouseIsDown=false;
}

Viewer3D.prototype.processMouseMove=function(event) {
  if (this.mouseIsDown) {
	var deltaX=event.screenX-this.mouseX;
	var deltaY=event.screenY-this.mouseY;
	this.camera.rotation.y+=(deltaX/10)*Math.PI/180;
	this.elevateCamera(2,-deltaY);
	this.mouseX=event.screenX;
	this.mouseY=event.screenY;
  }
}

}

/*
 *
 * FrameBuilder
 *
 */

var FrameBuilder;

if (typeof FrameBuilder=='undefined') {
FrameBuilder=function(scene, floorTexture, floorSizeX, floorSizeY, lightHeight) {
  this.scene=scene;
  this.floorTexture=floorTexture;
  this.floorSizeX=floorSizeX;
  this.floorSizeY=floorSizeY;
  this.lightHeight=lightHeight;
}

FrameBuilder.prototype.buildBackground=function() {
  var scene=this.scene;
  
  scene.background=new THREE.Color(0xaed6f1);  
}

FrameBuilder.prototype.buildSceneFrame=function() {
  var scene=this.scene;
  
  var texture, geometry, material, mesh;
  
  /* Textur für Boden */
  texture=new THREE.TextureLoader().load(this.floorTexture);
  texture.wrapS=THREE.MirroredRepeatWrapping;
  texture.wrapT=THREE.MirroredRepeatWrapping;
  texture.repeat.set(10,10);
  
  /* Boden */
  geometry=new THREE.PlaneBufferGeometry(this.floorSizeX[1]-this.floorSizeX[0],this.floorSizeY[1]-this.floorSizeY[0]);
  material=new THREE.MeshStandardMaterial({map: texture});
  mesh=new THREE.Mesh(geometry,material);
  mesh.rotation.x=-Math.PI/2;
  mesh.position.x=(this.floorSizeX[1]+this.floorSizeX[0])/2;
  mesh.position.y=0;
  mesh.position.z=(this.floorSizeY[1]+this.floorSizeY[0])/2;
  mesh.castShadow=true;
  mesh.receiveShadow=true;
  this.scene.add(mesh);
}

FrameBuilder.prototype.buildLight=function() {
  var scene=this.scene;
  
  /* Allgemeine Helligkeit */
  scene.add(new THREE.AmbientLight(0xffffff,0.75));
  
  /* Licht von oben nach unten (mit Schattenwurf) */
  var dirLight=new THREE.DirectionalLight(0xffffff,0.75);
  dirLight.position.set(0,this.lightHeight,0);				
  dirLight.castShadow=true;
  dirLight.shadow.camera.left=this.floorSizeX[0];
  dirLight.shadow.camera.right=this.floorSizeX[0];
  dirLight.shadow.camera.top=this.floorSizeY[1];
  dirLight.shadow.camera.bottom=this.floorSizeY[0];
  dirLight.shadow.camera.far=this.lightHeight;
  scene.add(dirLight);
}

FrameBuilder.prototype.build=function() {
  this.buildBackground();
  this.buildSceneFrame();
  this.buildLight();
}

}