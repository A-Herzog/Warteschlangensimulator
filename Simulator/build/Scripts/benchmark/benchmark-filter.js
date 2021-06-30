/* Ausgabe von: Threads, Laufzeit(in Sek.), #Ereignisse/Sek., #Ereignisse/Thread/Sek., minThreadLaufzeit, meanThreadLaufzeit, maxThreadLaufzeit, ThreadLaufzeitDifferenz  */
Statistics.translate("de");
var events=Statistics.xml("Simulation->StatistikEreignisse");
var threads=Statistics.xml("Simulation->StatistikThreads");
var time=Statistics.xml("Simulation->StatistikLaufzeit");
var threadRunTimes=Statistics.xmlArray("Simulation->StatistikThreadLaufzeiten");
var count=threadRunTimes.length;
var min=threadRunTimes[0];
var max=threadRunTimes[0];
var sum=threadRunTimes[0];
for (var i=1;i<count;i++) {
    var value=threadRunTimes[i];
	min=Math.min(min,value);
	max=Math.max(max,value);
    sum+=value;
}
Output.print(threads);
Output.tab();
Output.print(time/1000.0);
Output.tab();
Output.print(events*1000/time);
Output.tab();
Output.print(events/threads*1000/time);
Output.tab();
Output.print(min);
Output.tab();
Output.print(sum/count);
Output.tab();
Output.print(max);
Output.tab();
Output.setFormat("Percent");
Output.println(max/min-1);