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
 * Diese Klasse enthält die Informationen über einen Ressourcenausfall vor.
 * @author Alexander Herzog
 * @see ModelResource#getFailures()
 */
public final class ModelResourceFailure implements Cloneable {
	/**
	 * Name des XML-Elements, das ein Ressourcen-Ausfall-Element enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"RessourceAusfall"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Ressourcen-Ausfallmodus
	 * @see ModelResourceFailure#getFailureMode()
	 * @see ModelResourceFailure#getFailureNumber()
	 * @see ModelResourceFailure#setFailureByNumber(int)
	 */
	public enum FailureMode {
		/** Ausfall der Bediener der Ressource nach eine bestimmten Anzahl an bedienten Kunden */
		FAILURE_BY_NUMBER,

		/** Ausfall der Bediener der Ressource nach einer bestimmten Anwesenheitszeit */
		FAILURE_BY_AVAILABLE_TIME,

		/** Ausfall der Bediener der Ressource nach einer bestimmten Bedienzeit */
		FAILURE_BY_WORKING_TIME,

		/** Ausfall der Bediener der Ressource gemäß einer Abstände-Verteilung */
		FAILURE_BY_DISTRIBUTION,

		/** Ausfall der Bediener der Ressource nach eines Abstände-Ausdrucks */
		FAILURE_BY_EXPRESSION,
	}

	/**
	 * Ressourcen-Ausfallmodus
	 * @see #getFailureMode()
	 * @see FailureMode
	 */
	private FailureMode modeFailure;

	/**
	 * Anzahl an Bedienungen, nach denen ein Bediener dieser Ressource in Pause/Ausfallzeit geht
	 * @see #getFailureNumber()
	 * @see #setFailureByNumber(int)
	 */
	private int servedNumber;

	/**
	 * Zeitdauer, nach der ein Bediener dieser Ressource in Pause/Ausfallzeit geht
	 * @see #getFailureTime()
	 * @see #setFailureByWorkingTime(double)
	 * @see #setFailureByAvailableTime(double)
	 */
	private double servedTime;

	/**
	 * Verteilung gemäß der die Ressource ausfällt
	 * @see #getFailureDistribution()
	 * @see #setFailureByDistribution(AbstractRealDistribution)
	 */
	private AbstractRealDistribution interDownTimeDistribution;

	/**
	 * Ausdruck gemäß der die Ressource ausfällt
	 * @see #getFailureExpression()
	 * @see #setFailureByExpression(String)
	 */
	private String interDownTimeExpression;

	/**
	 * Verteilung der Pausen/Ausfallzeiten der Bediener dieser Ressource
	 * @see #getDownTimeDistribution()
	 * @see #setDownTimeDistribution(AbstractRealDistribution)
	 */
	private AbstractRealDistribution downTimeDistribution;

	/**
	 * Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Bediener dieser Ressource bestimmt werden sollen
	 * @see #getDownTimeExpression()
	 * @see #setDownTimeExpression(String)
	 */
	private String downTimeExpression;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelResourceFailure() {
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
	 * Vergleicht zwei <code>ModelResourceFailure</code>-Objekte
	 * @param otherModelResourceFailure	Anderes <code>ModelResourceFailure</code>-Objekt, welches mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsModelResourceFailure(final ModelResourceFailure otherModelResourceFailure) {
		if (otherModelResourceFailure==null) return false;

		if (modeFailure!=otherModelResourceFailure.modeFailure) return false;
		switch (modeFailure) {
		case FAILURE_BY_NUMBER:
			if (servedNumber!=otherModelResourceFailure.servedNumber) return false;
			break;
		case FAILURE_BY_AVAILABLE_TIME:
		case FAILURE_BY_WORKING_TIME:
			if (servedTime!=otherModelResourceFailure.servedTime) return false;
			break;
		case FAILURE_BY_DISTRIBUTION:
			if (!DistributionTools.compare(interDownTimeDistribution,otherModelResourceFailure.interDownTimeDistribution)) return false;
			break;
		case FAILURE_BY_EXPRESSION:
			if (interDownTimeExpression==null) {
				if (otherModelResourceFailure.interDownTimeExpression!=null) return false;
			} else {
				if (otherModelResourceFailure.interDownTimeExpression==null) return false;
				if (!interDownTimeExpression.equals(otherModelResourceFailure.interDownTimeExpression)) return false;
			}
			break;
		}

		if (downTimeExpression!=null) {
			if (otherModelResourceFailure.downTimeExpression==null) return false;
			if (!downTimeExpression.equals(otherModelResourceFailure.downTimeExpression)) return false;
		} else {
			if (otherModelResourceFailure.downTimeExpression!=null) return false;
			if (!DistributionTools.compare(downTimeDistribution,otherModelResourceFailure.downTimeDistribution)) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine Kopie des Objektes
	 * @return Kopiertes <code>ModelResourceFailure</code>-Objekt
	 */
	@Override
	public ModelResourceFailure clone() {
		ModelResourceFailure clone=new ModelResourceFailure();

		clone.modeFailure=modeFailure;
		clone.servedNumber=servedNumber;
		clone.servedTime=servedTime;
		clone.interDownTimeDistribution=DistributionTools.cloneDistribution(interDownTimeDistribution);
		clone.interDownTimeExpression=interDownTimeExpression;

		clone.downTimeDistribution=DistributionTools.cloneDistribution(downTimeDistribution);
		clone.downTimeExpression=downTimeExpression;

		return clone;
	}

	/**
	 * Liefert den aktuellen Ressourcen-Ausfallmodus zurück
	 * @return	Aktueller Modus gemäß dessen die Bediener der Ressource ausfallen
	 * @see FailureMode#FAILURE_BY_NUMBER
	 * @see FailureMode#FAILURE_BY_AVAILABLE_TIME
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	public FailureMode getFailureMode() {
		return modeFailure;
	}

	/**
	 * Liefert im Falle, dass die Ressource nach einer bestimmten Anzahl an Bedienungen ausfällt (<code>FAILURE_BY_NUMBER</code>) diese Anzahl
	 * @return	Anzahl an Bedienungen, nach denen ein Bediener dieser Ressource in Pause/Ausfallzeit geht
	 * @see ModelResourceFailure#setFailureByNumber(int)
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_NUMBER
	 */
	public int getFailureNumber() {
		return servedNumber;
	}

	/**
	 * Liefert die Verfügbarkeits- oder Arbeitszeit nach der ein Bediener der Ressource ausfällt (<code>FAILURE_BY_AVAILABLE_TIME</code> und <code>FAILURE_BY_WORKING_TIME</code>)
	 * @return	Zeitdauer, nach der ein Bediener dieser Ressource in Pause/Ausfallzeit geht
	 * @see ModelResourceFailure#setFailureByAvailableTime(double)
	 * @see ModelResourceFailure#setFailureByWorkingTime(double)
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_AVAILABLE_TIME
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 */
	public double getFailureTime() {
		return servedTime;
	}

	/**
	 * Liefert im Falle, dass die Ressource nach einer durch eine Verteilung bestimmten Zeit ausfällt (<code>FAILURE_BY_DISTRIBUTION</code>), diese Verteilung
	 * @return	Verteilung gemäß der die Ressource ausfällt
	 * @see ModelResourceFailure#setFailureByDistribution(AbstractRealDistribution)
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 */
	public AbstractRealDistribution getFailureDistribution() {
		return interDownTimeDistribution;
	}

	/**
	 * Liefert im Falle, dass die Ressource nach einer durch einen Ausdruck bestimmten Zeit ausfällt (<code>FAILURE_BY_EXPRESSION</code>), diesen Ausdruck
	 * @return	Ausdruck gemäß der die Ressource ausfällt
	 * @see ModelResourceFailure#setFailureByExpression(String)
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	public String getFailureExpression() {
		return interDownTimeExpression;
	}

	/**
	 * Stellt ein, dass die Bediener der Ressource nach jeweils einer bestimmten Anzahl an bedienten Kunden in die Pausen/Ausfallzeit gehen
	 * @param clientCount	Anzahl an Kunden, nach denen ein Bediener dieser Ressource in die Pausen/Ausfallzeit geht
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_NUMBER
	 */
	public void setFailureByNumber(final int clientCount) {
		if (clientCount<=0) return;
		modeFailure=FailureMode.FAILURE_BY_NUMBER;
		servedNumber=clientCount;
	}

	/**
	 * Stellt ein, dass die Bediener der Ressource nach einer bestimmten Anwesenheitszeit in die Pausen/Ausfallzeit gehen
	 * @param time	Anwesenheitszeitdauer in Sekunden, nach der ein Bediener dieser Ressource in die Pausen/Ausfallzeit geht
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_AVAILABLE_TIME
	 */
	public void setFailureByAvailableTime(final double time) {
		modeFailure=FailureMode.FAILURE_BY_AVAILABLE_TIME;
		servedTime=time;
	}

	/**
	 * Stellt ein, dass die Bediener der Ressource nach einer bestimmten gearbeiteten Zeit in die Pausen/Ausfallzeit gehen
	 * @param time	Arbeitszeitdauer in Sekunden, nach der ein Bediener dieser Ressource in die Pausen/Ausfallzeit geht
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 */
	public void setFailureByWorkingTime(final double time) {
		modeFailure=FailureMode.FAILURE_BY_WORKING_TIME;
		servedTime=time;
	}

	/**
	 * Stellt ein, dass die Bediener der Ressource nach einer durch eine Verteilung bestimmten Zeit in die Pausen/Ausfallzeit gehen
	 * @param interDownTimeDistribution	Verteilung, die die Abstände zwischen zwei Ausfällen bestimmt
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 */
	public void setFailureByDistribution(final AbstractRealDistribution interDownTimeDistribution) {
		if (interDownTimeDistribution!=null) {
			modeFailure=FailureMode.FAILURE_BY_DISTRIBUTION;
			this.interDownTimeDistribution=DistributionTools.cloneDistribution(interDownTimeDistribution);
		}
	}

	/**
	 * Stellt ein, dass die Bediener der Ressource nach einer durch einen Ausdruck bestimmten Zeit in die Pausen/Ausfallzeit gehen
	 * @param expression	Ausdruck, der die Abstände zwischen zwei Ausfällen bestimmt
	 * @see ModelResourceFailure#getFailureMode()
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	public void setFailureByExpression(final String expression) {
		if (expression!=null && !expression.isBlank()) {
			modeFailure=FailureMode.FAILURE_BY_EXPRESSION;
			interDownTimeExpression=expression;
		}
	}

	/**
	 * Liefert die Verteilung der Pausen/Ausfallzeiten der Bediener dieser Ressource.<br>
	 * Die Verteilung ist nur aktiv, wenn gleichzeitig <code>getDownTimeExpression</code> null liefert
	 * @return	Verteilung der Pausen/Ausfallzeiten der Bediener dieser Ressource
	 * @see ModelResourceFailure#setDownTimeDistribution(AbstractRealDistribution)
	 * @see ModelResourceFailure#getDownTimeExpression()
	 */
	public AbstractRealDistribution getDownTimeDistribution() {
		return downTimeDistribution;
	}

	/**
	 * Liefert den Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Bediener dieser Ressource bestimmt werden sollen.<br>
	 * Wird hier <code>null</code> zurück geliefert, so ist stattdessen die Verteilung aktiv.
	 * @return Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Bediener dieser Ressource bestimmt werden sollen
	 * @see ModelResourceFailure#setDownTimeExpression(String)
	 * @see ModelResourceFailure#getDownTimeDistribution()
	 */
	public String getDownTimeExpression() {
		return downTimeExpression;
	}

	/**
	 * Stellt die Verteilung der Pausen/Ausfallzeiten der Bediener dieser Ressource ein.<br>
	 * War bisher ein Ausdruck für die Bestimmung der Pausen/Ausfallzeiten hinterlegt,
	 * so wird durch den Aufruf dieser Funktion auf eine Verteilung umgeschaltet.
	 * @param distribution	Neue Verteilung der Pausen/Ausfallzeiten der Bediener dieser Ressource
	 * @see ModelResourceFailure#getDownTimeDistribution()
	 * @see ModelResourceFailure#setDownTimeExpression(String)
	 */
	public void setDownTimeDistribution(final AbstractRealDistribution distribution) {
		if (distribution==null) return;
		downTimeDistribution=DistributionTools.cloneDistribution(distribution);
		downTimeExpression=null;
	}

	/**
	 * Stellt den Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Bediener dieser Ressource bestimmt werden sollen ein.<br>
	 * War bisher eine Verteilung Bestimmung der Pausen/Ausfallzeiten hinterlegt,
	 * so wird durch den Aufruf dieser Funktion auf einen Ausdruck umgeschaltet.
	 * @param expression	Neuer Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Bediener dieser Ressource bestimmt werden sollen
	 * @see ModelResourceFailure#getDownTimeExpression()
	 * @see ModelResourceFailure#setDownTimeDistribution(AbstractRealDistribution)
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
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureExpression"),downTimeExpression);
		} else {
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureDistribution"),DistributionTools.distributionToString(downTimeDistribution));
		}

		switch (modeFailure) {
		case FAILURE_BY_NUMBER:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureByNumber"),""+servedNumber);
			break;
		case FAILURE_BY_AVAILABLE_TIME:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureByAvailableTime"),NumberTools.formatSystemNumber(servedTime));
			break;
		case FAILURE_BY_WORKING_TIME:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureByWorkingTime"),NumberTools.formatSystemNumber(servedTime));
			break;
		case FAILURE_BY_DISTRIBUTION:
			if (interDownTimeDistribution!=null) node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureByDistribution"),DistributionTools.distributionToString(interDownTimeDistribution));
			break;
		case FAILURE_BY_EXPRESSION:
			if (interDownTimeExpression!=null) node.setAttribute(Language.trPrimary("Surface.XML.Resource.FailureByExpression"),interDownTimeExpression);
			break;
		}
	}

	/**
	 * Versucht die Daten des Ressourcen-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @param resourceName	Name der Ressource (für Fehlermeldungen)
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node, final String resourceName) {
		clear();

		String s;

		boolean dataLoadedPart1=false;
		boolean dataLoadedPart2=false;

		s=Language.trAllAttribute("Surface.XML.Resource.FailureExpression",node);
		if (!s.isBlank()) {
			downTimeExpression=s;
			dataLoadedPart1=true;
		} else {
			s=Language.trAllAttribute("Surface.XML.Resource.FailureDistribution",node);
			if (!s.isBlank()) {
				final AbstractRealDistribution dist=DistributionTools.distributionFromString(s,86400);
				if (dist==null) return String.format(Language.tr("Surface.Resource.ErrorDistribution"),s,resourceName);
				downTimeDistribution=dist;
				dataLoadedPart1=true;
			}
		}

		s=Language.trAllAttribute("Surface.XML.Resource.FailureByNumber",node);
		if (!s.isBlank()) {
			final Integer I=NumberTools.getNotNegativeInteger(s);
			if (I==null) return String.format(Language.tr("Surface.Resource.ErrorFailureNumber"),s,resourceName);
			modeFailure=FailureMode.FAILURE_BY_NUMBER;
			servedNumber=I;
			dataLoadedPart2=true;
		}

		s=Language.trAllAttribute("Surface.XML.Resource.FailureByAvailableTime",node);
		if (!s.isBlank()) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Resource.ErrorFailureTime"),s,resourceName);
			modeFailure=FailureMode.FAILURE_BY_AVAILABLE_TIME;
			servedTime=D;
			dataLoadedPart2=true;
		}

		s=Language.trAllAttribute("Surface.XML.Resource.FailureByWorkingTime",node);
		if (!s.isBlank()) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(s));
			if (D==null) return String.format(Language.tr("Surface.Resource.ErrorFailureTime"),s,resourceName);
			modeFailure=FailureMode.FAILURE_BY_WORKING_TIME;
			servedTime=D;
			dataLoadedPart2=true;
		}

		s=Language.trAllAttribute("Surface.XML.Resource.FailureByDistribution",node);
		if (!s.isBlank()) {
			final AbstractRealDistribution dist=DistributionTools.distributionFromString(s,86400);
			if (dist==null) return String.format(Language.tr("Surface.Resource.ErrorInterDistribution"),s,resourceName);
			interDownTimeDistribution=dist;
			modeFailure=FailureMode.FAILURE_BY_DISTRIBUTION;
			dataLoadedPart2=true;
		}

		s=Language.trAllAttribute("Surface.XML.Resource.FailureByExpression",node);
		if (!s.isBlank()) {
			modeFailure=FailureMode.FAILURE_BY_EXPRESSION;
			interDownTimeExpression=s;
			dataLoadedPart2=true;
		}

		if (dataLoadedPart1 && dataLoadedPart2) return null;
		return String.format(Language.tr("Surface.Resource.NoData"),s,resourceName);
	}

	/**
	 * Sucht einen Text in den Daten des Ausfall-Datensatzes.
	 * @param searcher	Such-System
	 * @param resourceName	Name der Bedienergruppe zu der dieser Ausfall gehört
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final String resourceName) {
		/* Bestimmung der Ausfall-Abstände */
		switch (modeFailure) {
		case FAILURE_BY_NUMBER:
			searcher.testInteger(Language.tr("Editor.DialogBase.Search.Resource.Failure.ServedNumber"),servedNumber,newServedNumber->{if (newServedNumber>0) servedNumber=newServedNumber;});
			break;
		case FAILURE_BY_AVAILABLE_TIME:
			searcher.testDouble(Language.tr("Editor.DialogBase.Search.Resource.Failure.AvailableTime"),servedTime,newServedTime->{if (newServedTime>0) servedTime=newServedTime;});
			break;
		case FAILURE_BY_WORKING_TIME:
			searcher.testDouble(Language.tr("Editor.DialogBase.Search.Resource.Failure.WorkingTime"),servedTime,newServedTime->{if (newServedTime>0) servedTime=newServedTime;});
			break;
		case FAILURE_BY_DISTRIBUTION:
			searcher.testDistribution(Language.tr("Editor.DialogBase.Search.Resource.Failure.InterDownTimeDistribution"),interDownTimeDistribution);
			break;
		case FAILURE_BY_EXPRESSION:
			searcher.testString(Language.tr("Editor.DialogBase.Search.Resource.Failure.InterDownTimeExpression"),interDownTimeExpression);
			break;
		}

		/* Verteilung der Pausen/Ausfallzeiten der Bediener dieser Ressource */
		searcher.testDistribution(Language.tr("Editor.DialogBase.Search.Resource.Failure.DownTimeDistribution"),downTimeDistribution);

		/* Ausdruck gemäß dessen die Pausen/Ausfallzeiten der Bediener dieser Ressource bestimmt werden sollen */
		searcher.testString(Language.tr("Editor.DialogBase.Search.Resource.Failure.DownTimeExpression"),downTimeExpression);
	}
}
