/**
 * Copyright 2020 Alexander Herzog
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
package ui.modeleditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;

/**
 * Daten zu einer einzelnen Ressource
 * @author Alexander Herzog
 * @see ModelResources
 */
public final class ModelResource implements Cloneable {
	/**
	 * Name des XML-Elements, das die Ressourcen-Elemente enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"Ressource"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * @see ModelResource#getMode()
	 * @see ModelResource#getCount()
	 * @see ModelResource#setCount(int)
	 */
	public enum Mode {
		/** Ressource ist über eine fixe Anzahl an Bedienern definiert */
		MODE_NUMBER,

		/** Ressource ist über einen Schichtplan definiert */
		MODE_SCHEDULE
	}

	/**
	 * Name der Ressource
	 * @see #getName()
	 * @see #setName(String)
	 */
	private String name;

	/**
	 * Icon der Ressource
	 * @see #getIcon()
	 * @see #setIcon(String)
	 */
	private String icon;

	/**
	 * Modus der Ressource
	 * @see Mode
	 * @see #getMode()
	 */
	private Mode modeCount;

	/**
	 * Anzahl an Bedienern in der Ressource (Wert &ge;0 oder -1, wenn unendlich viele Bediener vorhanden sind)
	 * @see #getCount()
	 * @see #setCount(int)
	 */
	private int count;

	/**
	 * Name des Schichtplans
	 * @see #getSchedule()
	 * @see #setSchedule(String)
	 */
	private String schedule;

	/**
	 * Kosten pro Betriebsstunde und Bediener
	 * @see #getCostsPerActiveHour()
	 * @see #setCostsPerActiveHour(double)
	 */
	private double costsPerActiveHour;

	/**
	 * Kosten pro aktiver Arbeitsstunde und Bediener
	 * @see #getCostsPerProcessHour()
	 * @see #setCostsPerProcessHour(double)
	 */
	private double costsPerProcessHour;

	/**
	 * Kosten pro Leerlaufstunde und Bediener
	 * @see #getCostsPerIdleHour()
	 * @see #setCostsPerIdleHour(double)
	 */
	private double costsPerIdleHour;

	/**
	 * Liste der Ausfall-Ereignisse
	 * @see #getFailures()
	 */
	private final List<ModelResourceFailure> failures;

	/**
	 * Rüstzeiten-Verteilung beim Wechsel eines Bedieners von einer Station zu einer anderen
	 * @see #getMoveTimes()
	 * @see #setMoveTimes(Object)
	 */
	private AbstractRealDistribution moveTimesDistribution;

	/**
	 * Rüstzeiten-Ausdruck beim Wechsel eines Bedieners von einer Station zu einer anderen
	 * @see #getMoveTimes()
	 * @see #setMoveTimes(Object)
	 */
	private String moveTimesExpression;

	/**
	 * Verwendete Zeitbasis für Rüstzeiten
	 * @see #getMoveTimeBase()
	 * @see #setMoveTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase moveTimeBase;

	/**
	 * Konstruktor der Klasse <code>ModelResource</code>
	 */
	public ModelResource() {
		failures=new ArrayList<>();
		moveTimeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		clear();
	}

	/**
	 * Konstruktor der Klasse <code>ModelResource</code>
	 * @param name	Name der Ressource
	 */
	public ModelResource(final String name) {
		this();
		this.name=name;
	}

	/**
	 * Konstruktor der Klasse <code>ModelResource</code>
	 * @param name	Name der Ressource
	 * @param count	Anzahl an Bedienern in der Ressource (Wert &ge;0 oder -1, wenn unendlich viele Bediener vorhanden sind)
	 */
	public ModelResource(final String name, final int count) {
		this();
		this.name=name;
		this.count=count;
	}

	/**
	 * Stellt alle Eigenschaften der Ressource auf die Ausgangswerte zurück.
	 */
	public void clear() {
		name="";
		icon="";

		modeCount=Mode.MODE_NUMBER;
		count=1;
		schedule="";

		costsPerActiveHour=0;
		costsPerProcessHour=0;
		costsPerIdleHour=0;

		failures.clear();

		moveTimesDistribution=null;
		moveTimesExpression=null;
		moveTimeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
	}

	/**
	 * Vergleicht zwei <code>ModelResource</code>-Objekte
	 * @param otherModelResource	Anderes <code>ModelResource</code>-Objekt, welches mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsModelResource(final ModelResource otherModelResource) {
		if (otherModelResource==null) return false;

		if (!name.equals(otherModelResource.name)) return false;

		if (!icon.equals(otherModelResource.icon)) return false;

		if (modeCount!=otherModelResource.modeCount) return false;
		switch (modeCount) {
		case MODE_NUMBER:
			if (count!=otherModelResource.count) return false;
			break;
		case MODE_SCHEDULE:
			if (!schedule.equals(otherModelResource.schedule)) return false;
			break;
		}

		if (costsPerActiveHour!=otherModelResource.costsPerActiveHour) return false;
		if (costsPerProcessHour!=otherModelResource.costsPerProcessHour) return false;
		if (costsPerIdleHour!=otherModelResource.costsPerIdleHour) return false;

		if (failures.size()!=otherModelResource.failures.size()) return false;
		for (int i=0;i<failures.size();i++) if (!failures.get(i).equalsModelResourceFailure(otherModelResource.failures.get(i))) return false;

		if (!DistributionTools.compare(moveTimesDistribution,otherModelResource.moveTimesDistribution)) return false;
		if (!Objects.equals(moveTimesExpression,otherModelResource.moveTimesExpression)) return false;
		if (moveTimeBase!=otherModelResource.moveTimeBase) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Objektes
	 * @return Kopiertes <code>ModelResource</code>-Objekt
	 */
	@Override
	public ModelResource clone() {
		ModelResource clone=new ModelResource(name);

		clone.icon=icon;

		clone.modeCount=modeCount;
		clone.count=count;
		clone.schedule=schedule;

		clone.costsPerActiveHour=costsPerActiveHour;
		clone.costsPerProcessHour=costsPerProcessHour;
		clone.costsPerIdleHour=costsPerIdleHour;

		for (ModelResourceFailure failure: failures) clone.failures.add(failure.clone());

		clone.moveTimesDistribution=DistributionTools.cloneDistribution(moveTimesDistribution);
		clone.moveTimesExpression=moveTimesExpression;
		clone.moveTimeBase=moveTimeBase;

		return clone;
	}

	/**
	 * Liefert den Namen der Ressource
	 * @return	Name der Ressource
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen der Ressource ein
	 * @param name	Name der Ressource
	 */
	public void setName(final String name) {
		if (name==null) this.name=""; else this.name=name;
	}

	/**
	 * Liefert das Icon der Ressource
	 * @return	Icon der Ressource
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Stellt das Icon für die Ressource ein
	 * @param icon	Icon der Ressource
	 */
	public void setIcon(final String icon) {
		if (icon==null) this.icon=""; else this.icon=icon;
	}

	/**
	 * Liefert den Modus (d.h. ob die Ressource über eine fixe Anzahl oder über einen Schichtplan definiert sein soll
	 * @return	Modus der Ressource
	 * @see Mode
	 */
	public Mode getMode() {
		return modeCount;
	}

	/**
	 * Liefert die Anzahl an Bedienern in der Ressource
	 * @return Anzahl an Bedienern in der Ressource (Wert &ge;0 oder -1, wenn unendlich viele Bediener vorhanden sind)
	 * @see #setCount(int)
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Stellt die Anzahl an Bedienern in der Ressource ein und setzt dabei den
	 * Modus auf <code>MODE_NUMBER</code>
	 * @param count	Anzahl an Bedienern in der Ressource (Wert &ge;0 oder -1, wenn unendlich viele Bediener vorhanden sind)
	 * @see #getCount()
	 * @see #getMode()
	 */
	public void setCount(final int count) {
		modeCount=Mode.MODE_NUMBER;
		this.count=count;
	}

	/**
	 * Liefert den Namen des Schichtplans, aus dem sich die Anzahl an Bedienern ableiten soll
	 * @return	Name des Schichtplans
	 * @see #setSchedule(String)
	 */
	public String getSchedule() {
		return schedule;
	}

	/**
	 * Stellt den Namen des Schichtplans, aus dem sich die Anzahl an Bedienern ableiten soll, ein und
	 * setzt dabei den Modus <code>MODE_SCHEDULE</code>
	 * @param schedule	Name des Schichtplans
	 * @see #getSchedule()
	 * @see #getMode()
	 */
	public void setSchedule(final String schedule) {
		modeCount=Mode.MODE_SCHEDULE;
		if (schedule==null) this.schedule=""; else this.schedule=schedule;
	}

	/**
	 * Liefert die Kosten pro Betriebsstunde der Ressource (pro Bediener)
	 * @return	Kosten pro Betriebsstunde und Bediener
	 */
	public double getCostsPerActiveHour() {
		return costsPerActiveHour;
	}

	/**
	 * Stell die Kosten pro Betriebsstunde der Ressource (pro Bediener) ein
	 * @param costsPerActiveHour	Kosten pro Betriebsstunde und Bediener
	 */
	public void setCostsPerActiveHour(final double costsPerActiveHour) {
		this.costsPerActiveHour=costsPerActiveHour;
	}

	/**
	 * Liefert die zusätzlichen Kosten pro aktiver Arbeitsstunde der Ressource (pro Bediener)
	 * @return	Kosten pro aktiver Arbeitsstunde und Bediener
	 */
	public double getCostsPerProcessHour() {
		return costsPerProcessHour;
	}

	/**
	 * Stellt die zusätzlichen Kosten pro aktiver Arbeitsstunde der Ressource (pro Bediener) ein
	 * @param costsPerProcessHour	Kosten pro aktiver Arbeitsstunde und Bediener
	 */
	public void setCostsPerProcessHour(final double costsPerProcessHour) {
		this.costsPerProcessHour=costsPerProcessHour;
	}

	/**
	 * Liefert die zusätzlichen Kosten pro Leerlaufstunde der Ressource (pro Bediener)
	 * @return	Kosten pro Leerlaufstunde und Bediener
	 */
	public double getCostsPerIdleHour() {
		return costsPerIdleHour;
	}

	/**
	 * Stellt die zusätzlichen Kosten pro Leerlaufstunde der Ressource (pro Bediener) ein
	 * @param costsPerIdleHour	Kosten pro Leerlaufstunde und Bediener
	 */
	public void setCostsPerIdleHour(final double costsPerIdleHour) {
		this.costsPerIdleHour=costsPerIdleHour;
	}

	/**
	 * Liefert die Liste der Ausfall-Ereignisse
	 * @return	Liste der Ausfall-Ereignisse
	 */
	public List<ModelResourceFailure> getFailures() {
		return failures;
	}

	/**
	 * Liefert die Rüstzeit beim Wechsel eines Bedieners von einer Station zu einer anderen
	 * @return	Verteilung (<code>AbstractRealDistribution</code>, Zeichenkette oder <code>null</code>
	 */
	public Object getMoveTimes() {
		if (moveTimesDistribution!=null) return moveTimesDistribution;
		if (moveTimesExpression!=null) return moveTimesExpression;
		return null;
	}

	/**
	 * Stellt die Rüstzeit beim Wechsel eines Bedieners von einer Station zu einer anderen ein
	 * @param moveTimes	Verteilung (<code>AbstractRealDistribution</code>, Zeichenkette oder <code>null</code>
	 */
	public void setMoveTimes(final Object moveTimes) {
		moveTimesDistribution=null;
		moveTimesExpression=null;
		if (moveTimes instanceof AbstractRealDistribution) moveTimesDistribution=(AbstractRealDistribution)moveTimes;
		if (moveTimes instanceof String) {
			String s=(String)moveTimes;
			s=s.trim();
			if (!s.isEmpty()) moveTimesExpression=s;
		}
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getMoveTimeBase() {
		return moveTimeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param moveTimeBase	Neue zu verwendende Zeitbasis
	 */
	public void setMoveTimeBase(ModelSurface.TimeBase moveTimeBase) {
		if (moveTimeBase!=null)	this.moveTimeBase=moveTimeBase;
	}

	/**
	 * Speichert das Ressourcen-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		node.setAttribute(Language.trPrimary("Surface.XML.Resource.Name"),name);

		if (icon!=null && !icon.isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.Icon"),icon);
		}

		/* Anzahl */

		switch (modeCount) {
		case MODE_NUMBER:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.Type"),Language.trPrimary("Surface.XML.Resource.Type.Number"));
			if (count<0) node.setAttribute(Language.trPrimary("Surface.XML.Resource.Value"),Language.trPrimary("Surface.XML.Resource.Type.Number.Infinite")); else node.setAttribute(Language.trPrimary("Surface.XML.Resource.Value"),""+count);
			break;
		case MODE_SCHEDULE:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.Type"),Language.trPrimary("Surface.XML.Resource.Type.Schedule"));
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.Value"),schedule);
			break;
		}

		/* Kosten */

		if (costsPerActiveHour!=0.0) node.setAttribute(Language.trPrimary("Surface.XML.Resource.CostsPerHour"),NumberTools.formatSystemNumber(costsPerActiveHour));
		if (costsPerProcessHour!=0.0) node.setAttribute(Language.trPrimary("Surface.XML.Resource.CostsPerProcessHour"),NumberTools.formatSystemNumber(costsPerProcessHour));
		if (costsPerIdleHour!=0.0) node.setAttribute(Language.trPrimary("Surface.XML.Resource.CostsPerIdleHour"),NumberTools.formatSystemNumber(costsPerIdleHour));

		/* Ausfälle */

		for (ModelResourceFailure failure: failures) failure.addDataToXML(doc,node);

		/* Rüstzeiten */

		if (moveTimesDistribution!=null) {
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.SetupTime.Distribution"),DistributionTools.distributionToString(moveTimesDistribution));
			if (moveTimeBase!=ModelSurface.TimeBase.TIMEBASE_SECONDS) {
				node.setAttribute(Language.trPrimary("Surface.XML.Resource.SetupTime.TimeBase"),ModelSurface.getTimeBaseString(moveTimeBase));
			}
		}
		if (moveTimesExpression!=null && !moveTimesExpression.trim().isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.SetupTime.Expression"),moveTimesExpression.trim());
			if (moveTimeBase!=ModelSurface.TimeBase.TIMEBASE_SECONDS) {
				node.setAttribute(Language.trPrimary("Surface.XML.Resource.SetupTime.TimeBase"),ModelSurface.getTimeBaseString(moveTimeBase));
			}
		}
	}

	/**
	 * Versucht die Daten des Ressourcen-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		String s;

		name=Language.trAllAttribute("Surface.XML.Resource.Name",node);
		icon=Language.trAllAttribute("Surface.XML.Resource.Icon",node);

		/* Anzahl */

		final String type=Language.trAllAttribute("Surface.XML.Resource.Type",node);

		if (type.isEmpty()) {
			modeCount=Mode.MODE_NUMBER;
			s=Language.trAllAttribute("Surface.XML.Resource.Value",node);
			if (s.isEmpty()) s=node.getTextContent(); /* Fallback zu altem Schema */
			count=0;
			for (String test2: Language.trAll("Surface.XML.Resource.Type.Number.Infinite")) if (s.equalsIgnoreCase(test2)) {
				count=-1; break;
			}
			if (count==0) {
				Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.Resource.ErrorNumber"),s,name);
				count=I;
			}
		}
		for (String test : Language.trAll("Surface.XML.Resource.Type.Number")) if (type.equalsIgnoreCase(test)) {
			modeCount=Mode.MODE_NUMBER;
			s=Language.trAllAttribute("Surface.XML.Resource.Value",node);
			if (s.isEmpty()) s=node.getTextContent(); /* Fallback zu altem Schema */
			count=0;
			for (String test2: Language.trAll("Surface.XML.Resource.Type.Number.Infinite")) if (s.equalsIgnoreCase(test2)) {
				count=-1; break;
			}
			if (count==0) {
				Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.Resource.ErrorNumber"),s,name);
				count=I;
			}
			break;
		}
		for (String test: Language.trAll("Surface.XML.Resource.Type.Schedule")) if (type.equalsIgnoreCase(test)) {
			modeCount=Mode.MODE_SCHEDULE;
			schedule=Language.trAllAttribute("Surface.XML.Resource.Value",node);
			if (schedule.isEmpty()) schedule=node.getTextContent(); /* Fallback zu altem Schema */
			break;
		}

		/* Kosten */

		s=Language.trAllAttribute("Surface.XML.Resource.CostsPerHour",node);
		if (!s.trim().isEmpty()) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Resource.ErrorCostsPerHour"),s,name);
			costsPerActiveHour=D;
		}
		s=Language.trAllAttribute("Surface.XML.Resource.CostsPerProcessHour",node);
		if (!s.trim().isEmpty()) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Resource.ErrorCostsPerProcessHour"),s,name);
			costsPerProcessHour=D;
		}
		s=Language.trAllAttribute("Surface.XML.Resource.CostsPerIdleHour",node);
		if (!s.trim().isEmpty()) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Resource.ErrorCostsPerIdleHour"),s,name);
			costsPerIdleHour=D;
		}

		/* Ausfälle */

		final ModelResourceFailure failureDirect=new ModelResourceFailure();
		if (failureDirect.loadFromXML(node,name)==null) failures.add(failureDirect); /* Altes Format erkennen */

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			for (String test: ModelResourceFailure.XML_NODE_NAME) if (e.getNodeName().equalsIgnoreCase(test)) {
				ModelResourceFailure failure=new ModelResourceFailure();
				final String error=failure.loadFromXML(e,name);
				if (error!=null) return error;
				failures.add(failure);
				break;
			}
		}

		/* Rüstzeiten */

		s=Language.trAllAttribute("Surface.XML.Resource.SetupTime.Distribution",node);
		if (!s.trim().isEmpty()) {
			moveTimesDistribution=DistributionTools.distributionFromString(s,3600);
			if (moveTimesDistribution==null) return String.format(Language.tr("Surface.Resource.ErrorMoveTimeDistribution"),s,name);
			final String timeBaseName=Language.trAllAttribute("Surface.XML.Resource.SetupTime.TimeBase",node);
			moveTimeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
		} else {
			s=Language.trAllAttribute("Surface.XML.Resource.SetupTime.Expression",node);
			if (!s.trim().isEmpty()) moveTimesExpression=s;
			final String timeBaseName=Language.trAllAttribute("Surface.XML.Resource.SetupTime.TimeBase",node);
			moveTimeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
		}

		return null;
	}
}
