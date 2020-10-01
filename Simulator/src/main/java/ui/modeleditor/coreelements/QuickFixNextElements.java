package ui.modeleditor.coreelements;

import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementAssignString;
import ui.modeleditor.elements.ModelElementBalking;
import ui.modeleditor.elements.ModelElementBarrier;
import ui.modeleditor.elements.ModelElementBarrierPull;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementDuplicate;
import ui.modeleditor.elements.ModelElementHold;
import ui.modeleditor.elements.ModelElementHoldJS;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSet;
import ui.modeleditor.elements.ModelElementSetJS;

/**
 * Vorschläge für jeweils folgende Elemente für die Schnellkorrektur
 * @author Alexander Herzog
 * @see ModelElementPosition#addEdgeOutFixes(java.util.List)
 * @see ModelElementPosition#findEdgesTo(Class[], java.util.List)
 */
public class QuickFixNextElements {
	private QuickFixNextElements() {
	}

	/**
	 * Sinnvolle Folgestationen für Quellen
	 */
	@SuppressWarnings("rawtypes")
	public static final Class[] source=new Class[]{
			ModelElementDecide.class,
			ModelElementDuplicate.class,
			ModelElementProcess.class,
			ModelElementDelay.class,
			ModelElementHold.class,
			ModelElementHoldJS.class,
			ModelElementBarrier.class,
			ModelElementBarrierPull.class,
			ModelElementBalking.class,
			ModelElementSet.class,
			ModelElementSetJS.class,
	};

	/**
	 * Sinnvolle Folgestationen für Bedienstationen (und Verzögerungsstationen)
	 */
	@SuppressWarnings("rawtypes")
	public static final Class[] process=new Class[]{
			ModelElementDispose.class
	};

	/**
	 * Sinnvolle Folgestationen für Zurückschrecken-Stationen
	 */
	@SuppressWarnings("rawtypes")
	public static final Class[] balking=new Class[]{
			ModelElementProcess.class
	};

	/**
	 * Sinnvolle Folgestationen für Bedingung-Stationen und ähnliche
	 */
	@SuppressWarnings("rawtypes")
	public static final Class[] hold=new Class[]{
			ModelElementProcess.class,
			ModelElementDelay.class
	};

	/**
	 * Sinnvolle Folgestationen für Verzweigungen und Duplizieren-Stationen
	 */
	@SuppressWarnings("rawtypes")
	public static final Class[] duplicate=new Class[] {
			ModelElementAssign.class,
			ModelElementAssignString.class,
			ModelElementProcess.class,
			ModelElementDelay.class
	};
}
