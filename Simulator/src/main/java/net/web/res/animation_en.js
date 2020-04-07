'use strict';

var stepTimeout="The animation step could not be executed (time out).";
var objectTypeClient="Client";
var objectTypeOperator="Operator";
var objectTypeTransporter="Transporter";
var objectStatusType="Type";
var objectStatusId="id";
var objectStatusStation="Station";
var objectStatusRaw="Unprocessed status data. To keep the presentation clear, the icon property has been removed.";
var rawStatusNoData="There are no status messages yet.";
var expressionPrompt="Enter expression to be evaluated:";

var staticIcons=[];
var movingIcons=[];

var cacheIconObjs=[];
var cacheIconSrcs=[];

var lastResult=null;

var modePlay=false;
var animationRunning=false;

function drawIcon(icon) {
  for (var i=0;i<cacheIconSrcs.length;i++) if (cacheIconSrcs[i]==icon.icon) {
    context.drawImage(cacheIconObjs[i],icon.x,icon.y,icon.w,icon.h);
	return;
  }
	
  var img=new Image();	
  img.onload=function(){
    context.drawImage(img,icon.x,icon.y,icon.w,icon.h);
    cacheIconObjs.push(img);
    cacheIconSrcs.push(icon.icon);
  }
  img.src=icon.icon;
}

function redrawAll() {
  /* Zeichenfläche neu aufbauen */
  context.clearRect(0,0,surface.width,surface.height);
  drawElements();
  
  /* Statische Icons */
  for (var i=0;i<staticIcons.length;i++) drawIcon(staticIcons[i]);
  
  /* Dynamische Icons */
  var needNextStep=false;
  for (var i=0;i<movingIcons.length;i++) {
	var icon=movingIcons[i];
	drawIcon(icon[0]);
	if (icon.length>1) {
		icon.shift();
		needNextStep=true;
	}
  }
  
  if (needNextStep) {
	animationRunning=true;
	setTimeout(function(){redrawAll();},50);
  } else {
	  animationRunning=false;
	  if (modePlay) setTimeout(function(){processStep();},50);
  }
}

function processStep() {
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {if (this.readyState==4 && this.status==200) processStepResponse(this);}
  xhttp.ontimeout=function() {alert(stepTimeout);}
  xhttp.open("GET","/animation?command=step",true);
  xhttp.send();
}

function processStepResponse(response) {
  if (response.responseText!=null && response.responseText!="") {alert(response.responseText); return;}
	
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {if (this.readyState==4 && this.status==200) processStatusResponse(this);}
  xhttp.ontimeout=function() {alert(stepTimeout);}
  xhttp.open("GET","/animation?command=status",true);
  xhttp.send();
}

function getMainInfo(obj,isMove) {
  var s="";
  if (obj.type=="client") s=objectTypeClient;
  if (obj.type=="operator") s=objectTypeOperator;
  if (obj.type=="transporter") s=objectTypeTransporter;
	  
  var result="";
  result+="<b>"+s+"<\/b>";
  result+=" <img style=\"vertical-align:middle\" src=\""+obj.icon+"\"> ";
  result+="(";
  if (typeof(obj.typeName)!='undefined') result+=objectStatusType+": "+obj.typeName+", ";  
  result+=objectStatusId+": "+obj.id;
  if (obj.stationID1!=obj.stationID2 && isMove) result+=", "+objectStatusStation+": "+obj.stationID1+" &rarr; "+obj.stationID2; else result+=", "+objectStatusStation+": "+obj.stationID2;
  result+=")";
  return result;
}

function getInfo(obj,full) {
  var result="";
  result+="<li>";
  if (full) result+=getMainInfo(obj,false)+"<br>";
  var objClone=JSON.parse(JSON.stringify(obj));
  delete objClone.icon;
  result+="<span style=\"font-size: smaller; color: gray;\" title=\""+objectStatusRaw+"\">"+JSON.stringify(objClone)+"</span>";
  result+="</li>";
  
  return result; 
}

function processStatusResponse(response) {
  var json=JSON.parse(response.responseText);
  lastResult=response.responseText;

  /* Statusmeldungen */
  var status=document.getElementById("status");
  status.innerHTML=json.logs;
  
  var nr;
  var info;
  var objInfo;
      
  /* Statische Bilder */  
  staticIcons=[];
  var staticList=json.staticImages;
  nr=1;
  info="";
  while (typeof(staticList[nr])!='undefined') {
	staticIcons.push(staticList[nr]);
    info+=getInfo(staticList[nr],true);
	nr++;
  }
  var statusStatic=document.getElementById("status_static");
  if (info!="") info="<ul>"+info+"</ul>";
  statusStatic.innerHTML=info;
  
  /* Dynamische Bilder */
  movingIcons=[];
  var movingList=json.movingImages;
  nr=1;
  info="";
  while (typeof(movingList[nr])!='undefined') {
	  info+="<li>"+getMainInfo(movingList[nr][0],true)+"<ol>";
	  var count=movingList[nr]["count"];
	  var arr=[];
	  for (var i=0;i<count;i++) {
		  arr.push(movingList[nr][i]);
		  info+=getInfo(movingList[nr][i],false);
	  }
	  movingIcons.push(arr);
	  info+="</ol></li>";	 	
	nr++;
  } 
  var statusMoving=document.getElementById("status_moving");
  if (info!="") info="<ul>"+info+"</ul>";
  statusMoving.innerHTML=info;
  
  /* Zeichnen */
  redrawAll();
}

function quit() {
  var xhttp=new XMLHttpRequest();
  xhttp.open("GET","/animation?command=quit",true);
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {if (this.readyState==4) document.location="/";}
  xhttp.ontimeout=function() {document.location="/";}
  xhttp.send();
}

function showJSONData() {
  if (lastResult==null) {
    alert(rawStatusNoData);
    return;
  }
  window.open("/animation?command=status","_blank");
}

function showStationJSONData() {
	window.open("/animation?command=stations","_blank");
}

function calc() {
  var expression=prompt(expressionPrompt,"1+2");
  if (expression==null) return;
  var xhttp=new XMLHttpRequest();
  xhttp.open("GET","/animation?command=calc&expression="+encodeURIComponent(expression),true);
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function(response) {if (this.readyState==4) alert(expression+"="+this.responseText);}
  xhttp.send();
}

function playPause(button) {
  modePlay=!(modePlay || animationRunning);
		  
  if (modePlay) {
    button.innerHTML="Pause";
    processStep();
  } else {
    button.innerHTML="Play"; 
  }
}