'use strict';

var taskName="Aufgabe";
var statusName="Status";
var noConnection="Es besteht keine Verbindung zum Simulator.";
var selectModelFile="Bitte wählen Sie eine Modelldatei aus.";
var selectTableFile="Bitte wählen Sie eine Tabellendatei aus.";
var uploadError="Upload Fehlgeschlagen (Zeitüberschreitung).";
var resultDownload="Ergebnis herunterladen";
var resultView="Ergebnis anzeigen";
var resultDelete="Ergebnis löschen";
var taskDelete="Aufgabe abbrechen";
var noTasks="keine Aufträge";

function getTaskStatus(task,finished,directViewable) {
	var result="";
	
	result+="<li>\n";
	result+="<b>"+taskName+" - "+task.time+" - "+task.client+"</b><br>";
	result+=statusName+": "+task.statusText;
	if (task.messages.length>0) {
		result+="<div class=\"messages\">";
		for (var i=0;i<task.messages.length;i++) result+=task.messages[i]+"<br>";
		result+="</div>";
	}
	if (finished) {
		result+="<a href=\"/download/"+task.id+"\" class=\"button\"><b>"+resultDownload+"</b></a>";
		if (directViewable) {
			result+=" ";
			result+="<a href=\"/view/"+task.id+"\" class=\"button\" target=\"_blank\"><b>"+resultView+"</b></a>";
		}
		result+=" ";
		result+="<a href=\"javascript:deleteTask("+task.id+");\" class=\"button\"><b>"+resultDelete+"</b></a>";
	} else {
		result+="<a href=\"javascript:deleteTask("+task.id+");\" class=\"button\"><b>"+taskDelete+"</b></a>";
	}
	result+="</li>\n";
	
	return result;
}

function getStatus(json) {
  var waiting="";
  var running="";
  var done="";
  var system="";
  var language="";
  
  for (var i=0;i<json.length;i++) {
	  var task=json[i];
	  if (typeof(task.system)!='undefined') {system=task.system; continue;}	  
	  if (typeof(task.language)!='undefined') {language=task.language; continue;}
	  if (task.status==0) {waiting+=getTaskStatus(task,false); continue;} 
	  if (task.status==1) {running+=getTaskStatus(task,false); continue;}
	  done+=getTaskStatus(task,true,task.viewable==1);
  }

  if (waiting=="") waiting="<li>"+noTasks+"</li>";
  if (running=="") running="<li>"+noTasks+"</li>";
  if (done=="") done="<li>"+noTasks+"</li>";
  
  return ["<ul class=\"tab\">"+waiting+"</ul>","<ul class=\"tab\">"+running+"</ul>","<ul class=\"tab\">"+done+"</ul>",system,language];	
}

function setLanguage(language) {
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {
	if (this.readyState==4 && this.status==200) location.reload();		
  };
  xhttp.open("GET","/language/"+language,true);
  xhttp.send();
}

function buildLanguageChooserSingle(language, name) {
  if (language==name) {
    return "<b>"+name+"</b>";
  } else {
    return "<a href='javascript:setLanguage(\""+name+"\");'>"+name+"</a>";
  }
}

function buildLanguageChooser(language) {
  var result="Sprache: ";
  result+=buildLanguageChooserSingle(language,"de");
  result+="&nbsp;";
  result+=buildLanguageChooserSingle(language,"en");
  
  return result;
}

var lastResponse="";

function requestStatusReadyStateChange() {
  if (this.readyState!=4) return;
  if (this.status==200) {
    if (lastResponse!=this.responseText) {
      lastResponse=this.responseText;
      var status=getStatus(JSON.parse(this.responseText));
      document.getElementById("status_waiting").innerHTML=status[0];
      document.getElementById("status_running").innerHTML=status[1];
      document.getElementById("status_done").innerHTML=status[2];
      document.getElementById("status_system").innerHTML=status[3];
      document.getElementById("status_language").innerHTML=buildLanguageChooser(status[4]);
      document.getElementById("UploadForm").style.display="inline";
    }		
  } else {
    lastResponse="";
    document.getElementById("status_waiting").innerHTML="<span style=\"color: red\">"+noConnection+"<\/span>";
    document.getElementById("status_running").innerHTML="<span style=\"color: red\">"+noConnection+"<\/span>";
    document.getElementById("status_done").innerHTML="<span style=\"color: red\">"+noConnection+"<\/span>";
    document.getElementById("status_system").innerHTML="";
    document.getElementById("status_language").innerHTML="";
    document.getElementById("UploadForm").style.display="none";
  }
  setTimeout(function(){requestStatus();},500);
}

function requestStatus() {  
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=requestStatusReadyStateChange;
  xhttp.open("GET","/status",true);
  xhttp.send();
}

requestStatus();

function showUploadInfo(message) {
	var div=document.getElementById("UploadInfo");
	div.innerHTML=message;
	div.style.display="block";
	setTimeout(function(){document.getElementById("UploadInfo").style.display="none";},2000);
}

function uploadModel() {
  if (typeof(document.getElementById("UploadFile").files[0])=='undefined') {
	  alert(selectModelFile);
	  return;
  }

  var form=document.getElementById('UploadForm');
  var formData=new FormData(form);
	  
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {
	if (this.readyState==4 && this.status==200) showUploadInfo(this.responseText);		
  };
  xhttp.ontimeout=function() {
    alert(uploadError);
  }
  xhttp.open("POST","/upload",true);
  xhttp.send(formData);
  
  form.reset();
}

function uploadTable() {
  if (typeof(document.getElementById("UploadFile").files[0])=='undefined') {
	  alert(selectTableFile);
	  return;
  }

  var form=document.getElementById('UploadForm');
  var formData=new FormData(form);
	  
  var xhttp=new XMLHttpRequest();
  xhttp.timeout=2000;
  xhttp.onreadystatechange=function() {
	if (this.readyState==4 && this.status==200) showUploadInfo(this.responseText);		
  };
  xhttp.ontimeout=function() {
    alert(uploadError);
  }
  xhttp.open("POST","/upload",true);
  xhttp.send(formData);
  
  form.reset();
}

function deleteTask(id) {
	var xhttp=new XMLHttpRequest();
	xhttp.open("GET","/delete/"+id,true);
	xhttp.send();
}