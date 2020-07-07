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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * �ndert die maximalen Durchflussmengen an den Ventilen von Tanks
 * @author Alexander Herzog
 * @see ModelElementTank
 */
public class ModelElementTankValveSetup extends ModelElementMultiInSingleOutBox {
	private final List<ValveSetup> valveSetups;

	/**
	 * Konstruktor der Klasse <code>ModelElementTankValveSetup</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementTankValveSetup(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		valveSetups=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_VALVE_SETUP.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.TankValveSetup.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementTankValveSetup)) return false;

		if (valveSetups.size()!=((ModelElementTankValveSetup)element).valveSetups.size()) return false;
		for (int i=0;i<valveSetups.size();i++) {
			if (!valveSetups.get(i).equalsValveSetup(((ModelElementTankValveSetup)element).valveSetups.get(i))) return false;
		}

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementTankValveSetup) {
			valveSetups.clear();
			valveSetups.addAll(((ModelElementTankValveSetup)element).valveSetups.stream().map(ValveSetup::new).collect(Collectors.toList()));
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementTankValveSetup clone(final EditModel model, final ModelSurface surface) {
		final ModelElementTankValveSetup element=new ModelElementTankValveSetup(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.TankValveSetup.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.TankValveSetup.Name");
	}

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
			new ModelElementTankValveSetupDialog(owner,ModelElementTankValveSetup.this,readOnly);
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
		NextStationHelper.nextStationsAnalog(this,parentMenu,addNextStation);
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
	 * Liefert die Liste der Ventil-Setups
	 * @return	Liste der Ventil-Setups
	 */
	public List<ValveSetup> getValveSetups() {
		return valveSetups;
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.TankValveSetup.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		for (ValveSetup valveSetup: valveSetups) valveSetup.saveToXML(doc,node);
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

		if (Language.trAll("Surface.TankValveSetup.XML.Setup",name)) {
			final ValveSetup valveSetup=new ValveSetup();
			error=valveSetup.loadFromXML(node);
			if (error!=null) return error;
			valveSetups.add(valveSetup);
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementTankValveSetup";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		for (ValveSetup valveSetup: valveSetups) {
			descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.TankValveSetup.Setup"),valveSetup.tankId,valveSetup.valveNr+1),valveSetup.maxFlow,1000);
		}
	}

	/**
	 * Diese Klasse h�lt einen Ventil-Konfigurationsdatensatz vor
	 * @author Alexander Herzog
	 * @see ModelElementTankValveSetup
	 */
	public static class ValveSetup {
		/**
		 * ID des zu Tanks an dem sich das zu konfigurierende Ventil befindet
		 */
		public int tankId;

		/**
		 * 0-basierende Nummer des Ventils
		 */
		public int valveNr;

		/**
		 * Neuer maximaler Durchfluss
		 */
		public String maxFlow;

		/**
		 * Konstruktor der Klasse
		 */
		public ValveSetup() {
			tankId=0;
			valveNr=0;
			maxFlow="0";
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param copySource	Ausgangselement von dem die Daten �bernommen werden sollen
		 */
		public ValveSetup(final ValveSetup copySource) {
			tankId=copySource.tankId;
			valveNr=copySource.valveNr;
			maxFlow=copySource.maxFlow;
		}

		/**
		 * Vergleicht zwei Ventil-Setups
		 * @param otherSetup	Zweites Ventil-Setup, das mit diesem Objekt verglichen werden soll
		 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind.
		 */
		public boolean equalsValveSetup(final ValveSetup otherSetup) {
			if (otherSetup==null) return false;
			if (tankId!=otherSetup.tankId) return false;
			if (valveNr!=otherSetup.valveNr) return false;
			if (!maxFlow.equals(otherSetup.maxFlow)) return false;
			return true;
		}

		/**
		 * Speichert die Eigenschaften des Setup-Eintrags als Untereintr�ge eines xml-Knotens
		 * @param doc	�bergeordnetes xml-Dokument
		 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
		 */
		public void saveToXML(final Document doc, final Element node) {
			Element sub;

			node.appendChild(sub=doc.createElement(Language.tr("Surface.TankValveSetup.XML.Setup")));
			sub.setTextContent(maxFlow);
			sub.setAttribute(Language.tr("Surface.TankValveSetup.XML.Setup.TankID"),""+tankId);
			sub.setAttribute(Language.tr("Surface.TankValveSetup.XML.Setup.ValveNr"),""+(valveNr+1));
		}

		/**
		 * L�dt die Eigenschaften des Setup-Eintrags aus einem xml-Element.
		 * @param node	xml-Element, aus dem die Daten geladen werden soll
		 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
		 */
		public String loadFromXML(final Element node) {
			maxFlow=node.getTextContent();

			final String tankIdString=Language.trAllAttribute("Surface.TankValveSetup.XML.Setup.TankID",node);
			if (!tankIdString.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(tankIdString);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.TankValveSetup.XML.Setup.TankID"),node.getNodeName(),node.getParentNode().getNodeName());
				tankId=I.intValue();
			}

			final String valveNrString=Language.trAllAttribute("Surface.TankValveSetup.XML.Setup.ValveNr",node);
			if (!valveNrString.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(valveNrString);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.TankValveSetup.XML.Setup.ValveNr"),node.getNodeName(),node.getParentNode().getNodeName());
				valveNr=I.intValue()-1;
			}

			return null;
		}
	}
}