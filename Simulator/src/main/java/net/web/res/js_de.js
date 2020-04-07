'use strict';

var statusServerAddress="Serveradresse";
var statusProgramVersion="Programmversion";
var statusCurrentModel="Aktuelles Modell";
var statusChanged="Seit letztem Speichern verändert";
var statusChangedYes="ja";
var statusChangedNo="nein";
var statusMode="Aktueller Modus";
var statusRaw="Unverarbeitete Statusdaten";
var noConnection="Es besteht keine Verbindung zum Simulator.";
var selectModelFile="Bitte wählen Sie eine Modelldatei aus.";
var uploadError="Upload Fehlgeschlagen (Zeitüberschreitung).";

function getStatusText(url, json) {
  var s=url.substring(url.indexOf("//")+2);
  s=s.substring(0,s.indexOf("/"));
	
  var result="";
  result+="<span class=\"buttonframe\" style=\"margin-left: 0; color: black;\">\n";
  result+=statusServerAddress+": <b>"+s+"<\/b><br>\n";
  result+=statusProgramVersion+": <b>"+json.version+"<\/b><br>\n";
  result+=statusCurrentModel+": <b>"+json.model+"<\/b><br>\n";
  result+=statusChanged+": <b>"+((json.changed!="0")?statusChangedYes:statusChangedNo)+"<\/b><br>\n";
  result+=statusMode+": <b>"+json.mode+"<\/b><br>\n";
  result+="<\/span>\n";
  result+="<br><span style=\"font-size: smaller; color: gray;\" title=\""+statusRaw+"\">"+JSON.stringify(json)+"<\/span>\n";
  return result;	
}

function requestStatus() {
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=1000;
  xhttp.onreadystatechange=function() {
	if (this.readyState==4 && this.status==200) {		
		document.getElementById("Status").innerHTML=getStatusText(this.responseURL,JSON.parse(this.responseText));
		document.getElementById("Commands").style.display="inline";
		setTimeout(function(){requestStatus();},2000);
	}	
  };
  xhttp.ontimeout=function() {
	document.getElementById("Status").innerHTML="<span style=\"color: red\">"+noConnection+"<\/span>";
	document.getElementById("Commands").style.display="none";
	setTimeout(function(){requestStatus();},500);
  }
  xhttp.open("GET","/status",true);
  xhttp.send();
}

requestStatus();

function uploadModel() {
  if (typeof(document.getElementById("UploadFile").files[0])=='undefined') {
	  alert(selectModelFile);
	  return;
  }

  var formData=new FormData(document.getElementById('UploadForm'));
	  
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {
	if (this.readyState==4 && this.status==200) alert(this.responseText);		
  };
  xhttp.ontimeout=function() {
    alert(uploadError);
  }
  xhttp.open("POST","/upload",true);
  xhttp.send(formData);	
}