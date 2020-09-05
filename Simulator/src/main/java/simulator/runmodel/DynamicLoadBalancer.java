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
	private final long last25Percent;
	private final long arrivalPackageLarge;
	private final long arrivalPackageSmall;

	/**
	 * Konstruktor der Klasse
	 * @param arrivalsToBeSimulated	Gesamtzahl der zu simulierenden Ankünfte
	 * @param threadCount	Anzahl an Threads
	 */
	public DynamicLoadBalancer(final long arrivalsToBeSimulated, final int threadCount) {
		this.arrivalsToBeSimulated=arrivalsToBeSimulated;
		this.last25Percent=arrivalsToBeSimulated/4;
		this.arrivalPackageLarge=Math.max(MIN_LOAD_PACKAGE_SIZE,arrivalsToBeSimulated/threadCount/500);
		this.arrivalPackageSmall=Math.max(MIN_LOAD_PACKAGE_SIZE,arrivalsToBeSimulated/threadCount/2000);
	}

	/**
	 * Liefert (synchronisiert) das nächste Paket von zulässigen Ankünfte
	 * @return	Ankünfte für den Thread (kann 0 sein, wenn der Thread keine weiteren Ankünfte generieren soll)
	 */
	public long getArrivals() {
		synchronized(this) {
			if (arrivalsToBeSimulated==0) return 0;
			final long arrivalThisPackage;
			if (arrivalsToBeSimulated>last25Percent) {
				arrivalThisPackage=FastMath.min(arrivalPackageLarge,arrivalsToBeSimulated);
			} else {
				arrivalThisPackage=FastMath.min(arrivalPackageSmall,arrivalsToBeSimulated);
			}
			arrivalsToBeSimulated-=arrivalThisPackage;
			return arrivalThisPackage;
		}
	}
}
