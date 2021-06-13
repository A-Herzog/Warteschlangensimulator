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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.FullTextSearch;

/**
 * Enthält die Daten zu einem Ausfall eines Transporters in einer Transportergruppe
 * @author Alexander Herzog
 * @see ModelTransporter
 */
public final class ModelTransporterFailure implements Cloneable {
	/**
	 * Name des XML-Elements, das ein Transporter-Ausfall-Element enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"TransporterAusfall"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Nach welcher Regel sollen Ausfälle erfolgen?
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see ModelTransporterFailure#getFailureNumber()
	 * @see ModelTransporterFailure#setFailureByNumber(int)
	 */
	public enum FailureMode {
		/** Ausfall des Transporters nach eine bestimmten Anzahl an bedienten Kunden */
		FAILURE_BY_NUMBER,

		/** Ausfall des Transporters nach einer bestimmten Fahrtstrecke */
		FAILURE_BY_DISTANCE,

		/** Ausfall des Transporters nach einer bestimmten Bedienzeit */
		FAILURE_BY_WORKING_TIME,

		/** Ausfall des Transporters gemäß einer Abstände-Verteilung */
		FAILURE_BY_DISTRIBUTION,

		/** Ausfall des Transporters nach eines Abstände-Ausdrucks */
		FAILURE_BY_EXPRESSION
	}

	/**
	 * Nach welcher Regel sollen Ausfälle erfolgen?
	 * @see FailureMode
	 */
	private FailureMode modeFailure;

	/** Anzahl an Bedienungen, nach denen der Transporter in Pause/Ausfallzeit geht */
	private int servedNumber;
	/** Zeitdauer oder Strecke, nach der der Transporter in Pause/Ausfallzeit geht */
	private double servedTimeOrDistance;
	/** Verteilung gemäß der der Transporter ausfällt */
	private AbstractRealDistribution interDownTimeDistribution;
	/** Ausdruck, der die Abstände zwischen zwei Ausfällen bestimmt */
	private String interDownTimeExpression;

	/** Verteilung der Pausen/Ausfallzeiten-Dauern des Transporters */
	private AbstractRealDistribution downTimeDistribution;
	/** Ausdruck gemäß dessen die Pausen/Ausfallzeiten-Dauern des Transporters bestimmt werden sollen */
	private String downTimeExpression;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelTransporterFailure() {
		clear();
	}

	/**
	 * Löscht alle momentan in diesem Objekt hinterlegten Daten.
	 */
	private void clear() {
		modeFailure=FailureMode.FAILURE_BY_NUMBER;
		servedNumber=1000;

		downTimeDistribution=new ExponentialDistribution(null,1800,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		downTimeExpression=null;
	}

	/**
	 * Vergleicht zwei <code>ModelTransporterFailure</code>-Objekte
	 * @param otherModelTransporterFailure	Anderes <code>ModelTransporterFailure</code>-Objekt, welches mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsModelTransporterFailure(final ModelTransporterFailure otherModelTransporterFailure) {
		if (otherModelTransporterFailure==null) return false;

		if (modeFailure!=otherModelTransporterFailure.modeFailure) return false;
		switch (modeFailure) {
		case FAILURE_BY_NUMBER:
			if (servedNumber!=otherModelTransporterFailure.servedNumber) return false;
			break;
		case FAILURE_BY_DISTANCE:
		case FAILURE_BY_WORKING_TIME:
			if (servedTimeOrDistance!=otherModelTransporterFailure.servedTimeOrDistance) return false;
			break;
		case FAILURE_BY_DISTRIBUTION:
			if (!DistributionTools.compare(interDownTimeDistribution,otherModelTransporterFailure.interDownTimeDistribution)) return false;
			break;
		case FAILURE_BY_EXPRESSION:
			if (interDownTimeExpression==null) {
				if (otherModelTransporterFailure.interDownTimeExpression!=null) return false;
			} else {
				if (otherModelTransporterFailure.interDownTimeExpression==null) return false;
				if (!interDownTimeExpression.equals(otherModelTransporterFailure.interDownTimeExpression)) return false;
			}
			break;
		}

		if (downTimeExpression!=null) {
			if (otherModelTransporterFailure.downTimeExpression==null) return false;
			if (!downTimeExpression.equals(otherModelTransporterFailure.downTimeExpression)) return false;
		} else {
			if (otherModelTransporterFailure.downTimeExpression!=null) return false;
			if (!DistributionTools.compare(downTimeDistribution,otherModelTransporterFailure.downTimeDistribution)) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine Kopie des Objektes
	 * @return Kopiertes <code>ModelTransporterFailure</code>-Objekt
	 */
	@Override
	public ModelTransporterFailure clone() {
		final ModelTransporterFailure clone=new ModelTransporterFailure();

		clone.modeFailure=modeFailure;
		clone.servedNumber=servedNumber;
		clone.servedTimeOrDistance=servedTimeOrDistance;
		clone.interDownTimeDistribution=DistributionTools.cloneDistribution(interDownTimeDistribution);
		clone.interDownTimeExpression=interDownTimeExpression;

		clone.downTimeDistribution=DistributionTools.cloneDistribution(downTimeDistribution);
		clone.downTimeExpression=downTimeExpression;

		return clone;
	}

	/**
	 * Liefert den aktuellen Transporter-Ausfallmodus zurück
	 * @return	Aktueller Modus gemäß dessen der Transporter ausfällt
	 * @see FailureMode#FAILURE_BY_NUMBER
	 * @see FailureMode#FAILURE_BY_DISTANCE
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	public FailureMode getFailureMode() {
		return modeFailure;
	}

	/**
	 * Liefert im Falle, dass der Transporter nach einer bestimmten Anzahl an Bedienungen ausfällt (<code>FAILURE_BY_NUMBER</code>) diese Anzahl
	 * @return	Anzahl an Bedienungen, nach denen der Transporter in Pause/Ausfallzeit geht
	 * @see ModelTransporterFailure#setFailureByNumber(int)
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_NUMBER
	 */
	public int getFailureNumber() {
		return servedNumber;
	}

	/**
	 * Liefert die Arbeitszeit oder gefahrene Distanz nach der der Transporter ausfällt (<code>FAILURE_BY_WORKING_TIME</code> und <code>FAILURE_BY_DISTANCE</code>)
	 * @return	Zeitdauer oder Strecke, nach der der Transporter in Pause/Ausfallzeit geht
	 * @see ModelTransporterFailure#setFailureByWorkingTime(double)
	 * @see ModelTransporterFailure#setFailureByDistance(double)
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 * @see FailureMode#FAILURE_BY_DISTANCE
	 */
	public double getFailureTimeOrDistance() {
		return servedTimeOrDistance;
	}

	/**
	 * Liefert im Falle, dass der Transporter nach einer durch eine Verteilung bestimmten Zeit ausfällt (<code>FAILURE_BY_DISTRIBUTION</code>), diese Verteilung
	 * @return	Verteilung gemäß der der Transporter ausfällt
	 * @see ModelTransporterFailure#setFailureByDistribution(AbstractRealDistribution)
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 */
	public AbstractRealDistribution getFailureDistribution() {
		return interDownTimeDistribution;
	}

	/**
	 * Liefert im Falle, dass der Transporter nach einer durch einen Ausdruck bestimmten Zeit ausfällt (<code>FAILURE_BY_EXPRESSION</code>), diesen Ausdruck
	 * @return	Ausdruck gemäß der der Transporter ausfällt
	 * @see ModelTransporterFailure#setFailureByExpression(String)
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	public String getFailureExpression() {
		return interDownTimeExpression;
	}

	/**
	 * Stellt ein, dass der Transporter nach jeweils einer bestimmten Anzahl an bedienten Kunden in die Pausen/Ausfallzeit gehen
	 * @param clientCount	Anzahl an Kunden, nach denen der Transporter in die Pausen/Ausfallzeit geht
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_NUMBER
	 */
	public void setFailureByNumber(final int clientCount) {
		if (clientCount<=0) return;
		modeFailure=FailureMode.FAILURE_BY_NUMBER;
		servedNumber=clientCount;
	}

	/**
	 * Stellt ein, dass der Transporter nach einer gefahrenen Strecke in die Pausen/Ausfallzeit gehen
	 * @param distance Gefahrene Strecke nach der der Transporter in die Pausen/Ausfallzeit geht
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_DISTANCE
	 */
	public void setFailureByDistance(final double distance) {
		modeFailure=FailureMode.FAILURE_BY_DISTANCE;
		servedTimeOrDistance=distance;
	}

	/**
	 * Stellt ein, dass der Transporter nach einer bestimmten gearbeiteten Zeit in die Pausen/Ausfallzeit gehen
	 * @param time	Arbeitszeitdauer in Sekunden, nach der der Transporter in die Pausen/Ausfallzeit geht
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 */
	public void setFailureByWorkingTime(final double time) {
		modeFailure=FailureMode.FAILURE_BY_WORKING_TIME;
		servedTimeOrDistance=time;
	}

	/**
	 * Stellt ein, dass der Transporter nach einer durch eine Verteilung bestimmten Zeit in die Pausen/Ausfallzeit gehen
	 * @param interDownTimeDistribution	Verteilung, die die Abstände zwischen zwei Ausfällen bestimmt
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 */
	public void setFailureByDistribution(final AbstractRealDistribution interDownTimeDistribution) {
		if (interDownTimeDistribution!=null) {
			modeFailure=FailureMode.FAILURE_BY_DISTRIBUTION;
			this.interDownTimeDistribution=DistributionTools.cloneDistribution(interDownTimeDistribution);
		}
	}

	/**
	 * Stellt ein, dass der Transporter nach einer durch einen Ausdruck bestimmten Zeit in die Pausen/Ausfallzeit gehen
	 * @param expression	Ausdruck, der die Abstände zwischen zwei Ausfällen bestimmt
	 * @see ModelTransporterFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	public void setFailureByExpression(final String expression) {
		if (expression!=null && !expression.trim().isEmpty()) {
			modeFailure=FailureMode.FAILURE_BY_EXPRESSION;
			interDownTimeExpression=expression;
		}
	}

	/**
	 * Liefert die Verteilung der Pausen/Ausfallzeiten des Transporters.<br>
	 * Die Verteilung ist nur aktiv, wenn gleichzeitig <code>getDownTimeExpression</code> null liefert
	 * @return	Verteilung der Pausen/Ausfallzeiten des Transporters
	 * @see ModelTransporterFailure#setDownTimeDistribution(AbstractRealDistribution)
	 * @see ModelTransporterFailure#getDownTimeExpression()
	 */
	public AbstractRealDistribution getDownTimeDistribution() {
		return downTimeDistribution;
	}

	/**
	 * Liefert den Ausdruck gemäß dessen die Pausen/Ausfallzeiten des Transporters bestimmt werden sollen.<br>
	 * Wird hier <code>null</code> zurück geliefert, so ist stattdessen die Verteilung aktiv.
	 * @return Ausdruck gemäß dessen die Pausen/Ausfallzeiten des Transporters bestimmt werden sollen
	 * @see ModelTransporterFailure#setDownTimeExpression(String)
	 * @see ModelTransporterFailure#getDownTimeDistribution()
	 */
	public String getDownTimeExpression() {
		return downTimeExpression;
	}

	/**
	 * Stellt die Verteilung der Pausen/Ausfallzeiten des Transporters ein.<br>
	 * War bisher ein Ausdruck für die Bestimmung der Pausen/Ausfallzeiten hinterlegt,
	 * so wird durch den Aufruf dieser Funktion auf eine Verteilung umgeschaltet.
	 * @param distribution	Neue Verteilung der Pausen/Ausfallzeiten des Transporters
	 * @see ModelTransporterFailure#getDownTimeDistribution()
	 * @see ModelTransporterFailure#setDownTimeExpression(String)
	 */
	public void setDownTimeDistribution(final AbstractRealDistribution distribution) {
		if (distribution==null) return;
		downTimeDistribution=DistributionTools.cloneDistribution(distribution);
		downTimeExpression=null;
	}

	/**
	 * Stellt den Ausdruck gemäß dessen die Pausen/Ausfallzeiten des Transporters bestimmt werden sollen ein.<br>
	 * War bisher eine Verteilung Bestimmung der Pausen/Ausfallzeiten hinterlegt,
	 * so wird durch den Aufruf dieser Funktion auf einen Ausdruck umgeschaltet.
	 * @param expression	Neuer Ausdruck gemäß dessen die Pausen/Ausfallzeiten des Transporters bestimmt werden sollen
	 * @see ModelTransporterFailure#getDownTimeExpression()
	 * @see ModelTransporterFailure#setDownTimeDistribution(AbstractRealDistribution)
	 */
	public void setDownTimeExpression(final String expression) {
		if (expression==null) return;
		downTimeExpression=expression;
	}

	/**
	 * Speichert das Ressourcen-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		if (downTimeExpression!=null) {
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureExpression"),downTimeExpression);
		} else {
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureDistribution"),DistributionTools.distributionToString(downTimeDistribution));
		}

		switch (modeFailure) {
		case FAILURE_BY_NUMBER:
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureByNumber"),""+servedNumber);
			break;
		case FAILURE_BY_DISTANCE:
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureByDistance"),NumberTools.formatSystemNumber(servedTimeOrDistance));
			break;
		case FAILURE_BY_WORKING_TIME:
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureByWorkingTime"),NumberTools.formatSystemNumber(servedTimeOrDistance));
			break;
		case FAILURE_BY_DISTRIBUTION:
			if (interDownTimeDistribution!=null) node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureByDistribution"),DistributionTools.distributionToString(interDownTimeDistribution));
			break;
		case FAILURE_BY_EXPRESSION:
			if (interDownTimeExpression!=null) node.setAttribute(Language.trPrimary("Surface.XML.Transporter.FailureByExpression"),interDownTimeExpression);
			break;
		}
	}

	/**
	 * Versucht die Daten des Ressourcen-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @param transporterName	Name des Transporters (für Fehlermeldungen)
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node, final String transporterName) {
		clear();

		String s;

		s=Language.trAllAttribute("Surface.XML.Transporter.FailureExpression",node);
		if (!s.trim().isEmpty()) {
			downTimeExpression=s;
		} else {
			s=Language.trAllAttribute("Surface.XML.Transporter.FailureDistribution",node);
			if (!s.trim().isEmpty()) {
				final AbstractRealDistribution dist=DistributionTools.distributionFromString(s,86400);
				if (dist==null) return String.format(Language.tr("Surface.Transporter.ErrorDistribution"),s,transporterName);
				downTimeDistribution=dist;
			}
		}

		s=Language.trAllAttribute("Surface.XML.Transporter.FailureByNumber",node);
		if (!s.trim().isEmpty()) {
			final Integer I=NumberTools.getNotNegativeInteger(s);
			if (I==null) return String.format(Language.tr("Surface.Transporter.ErrorFailureNumber"),s,transporterName);
			modeFailure=FailureMode.FAILURE_BY_NUMBER;
			servedNumber=I;
		}

		s=Language.trAllAttribute("Surface.XML.Transporter.FailureByDistance",node);
		if (!s.trim().isEmpty()) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Transporter.ErrorFailureDistance"),s,transporterName);
			modeFailure=FailureMode.FAILURE_BY_DISTANCE;
			servedTimeOrDistance=D;
		}

		s=Language.trAllAttribute("Surface.XML.Transporter.FailureByWorkingTime",node);
		if (!s.trim().isEmpty()) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Transporter.ErrorFailureTime"),s,transporterName);
			modeFailure=FailureMode.FAILURE_BY_WORKING_TIME;
			servedTimeOrDistance=D;
		}

		s=Language.trAllAttribute("Surface.XML.Transporter.FailureByDistribution",node);
		if (!s.trim().isEmpty()) {
			final AbstractRealDistribution dist=DistributionTools.distributionFromString(s,86400);
			if (dist==null) return String.format(Language.tr("Surface.Transporter.ErrorInterDistribution"),s,transporterName);
			interDownTimeDistribution=dist;
			modeFailure=FailureMode.FAILURE_BY_DISTRIBUTION;
		}

		s=Language.trAllAttribute("Surface.XML.Transporter.FailureByExpression",node);
		if (!s.trim().isEmpty()) {
			modeFailure=FailureMode.FAILURE_BY_EXPRESSION;
			interDownTimeExpression=s;
		}

		return null;
	}

	/**
	 * Sucht einen Text in den Daten des Ausfall-Datensatzes.
	 * @param searcher	Such-System
	 * @param transporterGroupName	Name der Transportergruppe zu der dieser Ausfall gehört
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final String transporterGroupName) {
		/* Bestimmung der Ausfall-Abstände */
		switch (modeFailure) {
		case FAILURE_BY_NUMBER:
			searcher.testInteger(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.ServedNumber"),servedNumber,newServedNumber->{if (newServedNumber>0) servedNumber=newServedNumber;});
			break;
		case FAILURE_BY_DISTANCE:
			searcher.testDouble(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.Distance"),servedTimeOrDistance,newServedTimeOrDistance->{if (newServedTimeOrDistance>0) servedTimeOrDistance=newServedTimeOrDistance;});
			break;
		case FAILURE_BY_WORKING_TIME:
			searcher.testDouble(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.WorkingTime"),servedTimeOrDistance,newServedTimeOrDistance->{if (newServedTimeOrDistance>0) servedTimeOrDistance=newServedTimeOrDistance;});
			break;
		case FAILURE_BY_DISTRIBUTION:
			searcher.testDistribution(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.InterDownTimeDistribution"),interDownTimeDistribution);
			break;
		case FAILURE_BY_EXPRESSION:
			searcher.testString(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.InterDownTimeExpression"),interDownTimeExpression);
			break;
		}

		/* Verteilung der Pausen/Ausfallzeiten der Bediener dieser Transportergruppe */
		searcher.testDistribution(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.DownTimeDistribution"),downTimeDistribution);

		/* Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Transporter dieser Transportergruppe bestimmt werden sollen */
		searcher.testString(Language.tr("Editor.DialogBase.Search.TransporterGroup.Failure.DownTimeExpression"),downTimeExpression);
	}
}