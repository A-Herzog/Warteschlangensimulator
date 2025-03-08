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
package ui.modeleditor.elements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Erfassung von Kosten (f�r die Station und f�r den Kunden), wenn ein Kunde diese Station passiert
 * @author Alexander Herzog
 */
public class ModelElementCosts extends ModelElementMultiInSingleOutBox {
	/**
	 * Kosten an der Station
	 * @see #getStationCosts()
	 * @see #setStationCosts(String)
	 */
	private String stationCosts;

	/**
	 * Zus�tzliche Kunden-Wartezeit-Kosten
	 * @see #getClientWaitingCosts()
	 * @see #setClientWaitingCosts(String)
	 */
	private String clientWaitingCosts;

	/**
	 * Zus�tzliche Kunden-Transferzeit-Kosten
	 * @see #getClientTransferCosts()
	 * @see #setClientTransferCosts(String)
	 */
	private String clientTransferCosts;

	/**
	 * Zus�tzliche Kunden-Bedienzeit-Kosten
	 * @see #getClientProcessCosts()
	 * @see #setClientProcessCosts(String)
	 */
	private String clientProcessCosts;

	/**
	 * Zus�tzliche optionale Bedingung, die f�r die Zuweisung erf�llt sein muss (kann <code>null</code> sein)
	 * @see #getCondition()
	 * @see #setCondition(String)
	 */
	private String condition;

	/**
	 * Konstruktor der Klasse <code>ModelElementCosts</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementCosts(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE);
		stationCosts="";
		clientWaitingCosts="";
		clientTransferCosts="";
		clientProcessCosts="";
		condition="";
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_COSTS.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Costs.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementCosts)) return false;
		final ModelElementCosts otherCosts=(ModelElementCosts)element;

		if (!stationCosts.equals(otherCosts.stationCosts)) return false;
		if (!clientWaitingCosts.equals(otherCosts.clientWaitingCosts)) return false;
		if (!clientTransferCosts.equals(otherCosts.clientTransferCosts)) return false;
		if (!clientProcessCosts.equals(otherCosts.clientProcessCosts)) return false;
		if (!otherCosts.condition.equals(condition)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementCosts) {
			final ModelElementCosts source=(ModelElementCosts)element;
			stationCosts=source.stationCosts;
			clientWaitingCosts=source.clientWaitingCosts;
			clientTransferCosts=source.clientTransferCosts;
			clientProcessCosts=source.clientProcessCosts;
			condition=source.condition;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementCosts clone(final EditModel model, final ModelSurface surface) {
		final ModelElementCosts element=new ModelElementCosts(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Costs.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Costs.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,255,180);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementCostsDialog(owner,ModelElementCosts.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param surfacePanel	Zeichenfl�che
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Costs.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (!clientWaitingCosts.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Costs.XML.WaitingTimeCosts")));
			sub.setTextContent(clientWaitingCosts);
		}

		if (!clientTransferCosts.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Costs.XML.TransferTimeCosts")));
			sub.setTextContent(clientTransferCosts);
		}

		if (!clientProcessCosts.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Costs.XML.ProcessTimeCosts")));
			sub.setTextContent(clientProcessCosts);
		}

		if (!stationCosts.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Costs.XML.StationCosts")));
			sub.setTextContent(stationCosts);
		}

		if (!condition.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Costs.XML.Condition")));
			sub.setTextContent(condition);
		}
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.Costs.XML.StationCosts",name)) {
			stationCosts=content;
			return null;
		}

		if (Language.trAll("Surface.Costs.XML.WaitingTimeCosts",name)) {
			clientWaitingCosts=content;
			return null;
		}

		if (Language.trAll("Surface.Costs.XML.TransferTimeCosts",name)) {
			clientTransferCosts=content;
			return null;
		}

		if (Language.trAll("Surface.Costs.XML.ProcessTimeCosts",name)) {
			clientProcessCosts=content;
			return null;
		}

		if (Language.trAll("Surface.Costs.XML.Condition",name)) {
			condition=content;
			return null;
		}

		return null;
	}

	/**
	 * Liefert die Kosten, die an der Station bei der Durchquerung eines Kunden entstehen
	 * @return	Kosten an der Station
	 */
	public String getStationCosts() {
		return stationCosts;
	}

	/**
	 * Stellt die Kosten, die an der Station bei der Durchquerung eines Kunden entstehen, ein
	 * @param stationCosts	Kosten an der Station
	 */
	public void setStationCosts(final String stationCosts) {
		if (stationCosts==null) return;
		this.stationCosts=stationCosts;
		fireChanged();
	}

	/**
	 * Liefert die Kosten, die zu den Kunden-Wartezeit-Kosten addiert werden sollen, wenn ein Kunde die Station durchquert
	 * @return	Zus�tzliche Kunden-Wartezeit-Kosten
	 */
	public String getClientWaitingCosts() {
		return clientWaitingCosts;
	}

	/**
	 * Stellt die Kosten, die zu den Kunden-Wartezeit-Kosten addiert werden sollen, wenn ein Kunde die Station durchquert, ein
	 * @param clientWaitingCosts	Zus�tzliche Kunden-Wartezeit-Kosten
	 */
	public void setClientWaitingCosts(final String clientWaitingCosts) {
		if (clientWaitingCosts==null) return;
		this.clientWaitingCosts=clientWaitingCosts;
		fireChanged();
	}

	/**
	 * Liefert die Kosten, die zu den Kunden-Transferzeit-Kosten addiert werden sollen, wenn ein Kunde die Station durchquert
	 * @return	Zus�tzliche Kunden-Transferzeit-Kosten
	 */
	public String getClientTransferCosts() {
		return clientTransferCosts;
	}

	/**
	 * Stellt die Kosten, die zu den Kunden-Transferzeit-Kosten addiert werden sollen, wenn ein Kunde die Station durchquert, ein
	 * @param clientTransferCosts	Zus�tzliche Kunden-Transferzeit-Kosten
	 */
	public void setClientTransferCosts(final String clientTransferCosts) {
		if (clientTransferCosts==null) return;
		this.clientTransferCosts=clientTransferCosts;
		fireChanged();
	}

	/**
	 * Liefert die Kosten, die zu den Kunden-Bedienzeit-Kosten addiert werden sollen, wenn ein Kunde die Station durchquert
	 * @return	Zus�tzliche Kunden-Bedienzeit-Kosten
	 */
	public String getClientProcessCosts() {
		return clientProcessCosts;
	}

	/**
	 * Stellt die Kosten, die zu den Kunden-Bedienzeit-Kosten addiert werden sollen, wenn ein Kunde die Station durchquert, ein
	 * @param clientProcessCosts	Zus�tzliche Kunden-Bedienzeit-Kosten
	 */
	public void setClientProcessCosts(final String clientProcessCosts) {
		if (clientProcessCosts==null) return;
		this.clientProcessCosts=clientProcessCosts;
		fireChanged();
	}

	/**
	 * Liefert die optionale Bedingung, die f�r die Zuweisung erf�llt sein muss.
	 * @return	Bedingung, die f�r die Zuweisung erf�llt sein muss (kann <code>null</code> sein)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung, die f�r die Zuweisung erf�llt sein muss, ein.
	 * @param condition	Optionale Bedingung, die f�r die Zuweisung erf�llt sein muss (kann <code>null</code> sein oder leer sein)
	 */
	public void setCondition(final String condition) {
		this.condition=(condition==null)?"":condition;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station w�hrend der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return super.showAnimationRunData(); /* statt einfach "false". Schadet ja auch an dieser Station nicht. */
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementCosts";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!stationCosts.isBlank()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Costs.Station"),stationCosts,1000);
		if (!clientWaitingCosts.isBlank()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Costs.ClientWaiting"),clientWaitingCosts,2000);
		if (!clientTransferCosts.isBlank()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Costs.ClientTransfer"),clientTransferCosts,3000);
		if (!clientProcessCosts.isBlank()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Costs.ClientProcess"),clientProcessCosts,4000);
		if (!condition.isBlank()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Costs.Condition"),condition,5000);
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.hold,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Surface.Costs.Dialog.StationCosts"),stationCosts,newStationCosts->{stationCosts=newStationCosts;});
		searcher.testString(this,Language.tr("Surface.Costs.Dialog.WaitingCosts"),clientWaitingCosts,newClientWaitingCosts->{clientWaitingCosts=newClientWaitingCosts;});
		searcher.testString(this,Language.tr("Surface.Costs.Dialog.TransferCosts"),clientTransferCosts,newClientTransferCosts->{clientTransferCosts=newClientTransferCosts;});
		searcher.testString(this,Language.tr("Surface.Costs.Dialog.ProcessCosts"),clientProcessCosts,newClientProcessCosts->{clientProcessCosts=newClientProcessCosts;});
		if (!condition.isEmpty()) searcher.testString(this,Language.tr("Editor.DialogBase.Search.Condition"),condition,newCondition->condition=newCondition);
	}
}