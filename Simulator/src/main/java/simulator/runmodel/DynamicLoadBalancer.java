package simulator.runmodel;

import org.apache.commons.math3.util.FastMath;

/**
 * Über alle Threads synchronisierte Zählung der Ankünfte, um so die Kunden
 * pro Thread dynamisch anpassen zu können.
 * @author Alexander Herzog
 * @see RunData#nextClientIsLast(SimulationData)
 */
public class DynamicLoadBalancer {
	private final static long MIN_LOAD_PACKAGE_SIZE=1_000;

	private long arrivalsToBeSimulated;
	private final long arrivalPackage;

	/**
	 * Konstruktor der Klasse
	 * @param arrivalsToBeSimulated	Gesamtzahl der zu simulierenden Ankünfte
	 */
	public DynamicLoadBalancer(final long arrivalsToBeSimulated) {
		this.arrivalsToBeSimulated=arrivalsToBeSimulated;
		this.arrivalPackage=Math.max(MIN_LOAD_PACKAGE_SIZE,arrivalsToBeSimulated/1000);
	}

	/**
	 * Liefert (synchronisiert) das nächste Paket von zulässigen Ankünfte
	 * @return	Ankünfte für den Thread (kann 0 sein, wenn der Thread keine weiteren Ankünfte generieren soll)
	 */
	public long getArrivals() {
		synchronized(this) {
			if (arrivalsToBeSimulated==0) return 0;
			long arrivalThisPackage=FastMath.min(arrivalPackage,arrivalsToBeSimulated);
			arrivalsToBeSimulated-=arrivalThisPackage;
			return arrivalThisPackage;
		}
	}
}
