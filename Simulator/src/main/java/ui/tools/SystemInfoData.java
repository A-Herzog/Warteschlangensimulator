/**
 * Copyright 2023 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.tools;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Liefert Informationen zur CPU-Auslastung und zur Arbeitsspeicherbelegung.
 * @author Alexander Herzog
 */
public class SystemInfoData {
	/**
	 * System zu Ermittlung der Arbeitsspeicher-Daten
	 */
	private final MemoryMXBean memory=ManagementFactory.getMemoryMXBean();

	/**
	 * System zu Ermittlung der Thread-Daten
	 */
	private final ThreadMXBean threads=ManagementFactory.getThreadMXBean();

	/**
	 * Anzahl der logischen CPU-Kerne in diesem System
	 */
	private final int cpuCount=Runtime.getRuntime().availableProcessors();

	/**
	 * CPU-Zeit der Threads beim letzten Aufruf
	 */
	private Map<Long,Long> lastLoad;

	/**
	 * Zeitpunkt des letzten Aufrufs von {@link #getLoad()}
	 * @see #getLoad()
	 */
	private long lastTimeStamp;

	/**
	 * Konstruktor der Klasse
	 */
	public SystemInfoData() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Liefert die aktuelle Auslastung der CPU durch Threads dieses Programms.
	 * @return	Auslastung als Wert zwischen 0 und 1
	 */
	public double getLoad() {
		final long timeStamp=System.currentTimeMillis();
		long sum=0;
		final Map<Long,Long> load=new HashMap<>();
		for (long id: threads.getAllThreadIds()) {
			final long l=threads.getThreadCpuTime(id);
			load.put(id,l);
			final long threadLoad=(lastLoad==null)?l:(l-lastLoad.getOrDefault(id,0L));
			sum+=threadLoad;
		}
		lastLoad=load;
		final long delta=(timeStamp-lastTimeStamp)*1_000_000;
		lastTimeStamp=timeStamp;
		final double workLoad=((double)sum)/delta;
		return workLoad/cpuCount;
	}

	/**
	 * Lieferte die aktuelle Arbeitsspeicherbelegung in Bytes.
	 * @return	Arbeitsspeicherbelegung in Bytes
	 */
	public long getRAMUsage() {
		final MemoryUsage memoryHeap=memory.getHeapMemoryUsage();
		final MemoryUsage memoryNonHeap=memory.getNonHeapMemoryUsage();
		return memoryHeap.getUsed()+memoryNonHeap.getUsed();
	}
}
