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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.PathEditorDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Wegpunkt für die Fahrt eines Transporters von einer Station zu einer anderen.<br>
 * Wird nur für die Animation verwendet, hat keine Auswirkungen auf die Simulationsdaten.
 * @author Alexander Herzog
 */
public class ModelElementWayPoint extends ModelElementPosition {
	/**
	 * Vorgabe-Icon für den Wegpunkt
	 */
	public static final String DEFAULT_ICON="flag_red";

	/**
	 * Namen des Icons, das in dem Element angezeigt werden soll
	 * @see #getIcon()
	 * @see #setIcon(String)
	 */
	private String icon;

	/**
	 * Liste mit allen Wegpunkte-Datensätzen
	 * @see #getRecords()
	 */
	private List<WayPointRecord> wayPoints;

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource;

	/**
	 * Konstruktor der Klasse <code>ModelElementVertex</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementWayPoint(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(10,10),Shapes.ShapeType.SHAPE_NONE);
		wayPoints=new ArrayList<>();
		imageSource=new AnimationImageSource();
		setIcon(DEFAULT_ICON);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_WAY_POINT.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.WayPoint.Tooltip");
	}

	/**
	 * Liefert den Namen des Icons, das in dem Element angezeigt werden soll.
	 * @return	Namen des Icons, das in dem Element angezeigt werden soll
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Stellt den Namen des Icons, das in dem Element angezeigt werden soll, ein.
	 * @param icon	Namen des Icons, das in dem Element angezeigt werden soll
	 */
	public void setIcon(final String icon) {
		if (icon==null || icon.trim().isEmpty()) this.icon=DEFAULT_ICON; else this.icon=icon;
		final int[] size=imageSource.getNativeSize(this.icon,getModel().animationImages);
		setSize(new Dimension(size[0],size[1]));
	}

	/**
	 * Liefert die Liste mit allen Wegpunkte-Datensätzen
	 * @return	Liste mit allen Wegpunkte-Datensätzen
	 */
	public List<WayPointRecord> getRecords() {
		return wayPoints;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementWayPoint)) return false;

		if (!icon.equals(((ModelElementWayPoint)element).icon)) return false;

		if (wayPoints.size()!=((ModelElementWayPoint)element).wayPoints.size()) return false;
		for (int i=0;i<wayPoints.size();i++) if (!wayPoints.get(i).equalsWayPointRecord(((ModelElementWayPoint)element).wayPoints.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		icon=((ModelElementWayPoint)element).icon;
		wayPoints=new ArrayList<>(((ModelElementWayPoint)element).wayPoints.size());
		if (element instanceof ModelElementWayPoint) {
			for (WayPointRecord wayPoint: ((ModelElementWayPoint)element).wayPoints) wayPoints.add(new WayPointRecord(wayPoint));
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementWayPoint clone(final EditModel model, final ModelSurface surface) {
		final ModelElementWayPoint element=new ModelElementWayPoint(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private String lastDrawIcon;
	private int lastDrawWidth;
	private BufferedImage lastDrawImage;

	/**
	 * Zeichnet die eigentliche Form des Elements
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Gesamter für Zeichnungen zur Verfügung stehender Bereich
	 * @param objectRect	Gemäß Zoomfaktor skalierter Rahmen des Elements
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (in Pixeln)
	 * @param fillColor	Füllfarbe (oder <code>null</code>, wenn die Box transparent sein soll)
	 * @param zoom	Aktueller Zoomfaktor
	 * @param stage	Gibt an, ob nur der Hintergrund (stage=1) oder nur der Rahmen (stage=2) gezeichnet werden soll
	 */
	@Override
	protected void drawElementShape(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect, final Color borderColor, final int borderWidth, final Color fillColor, final double zoom, final int stage) {
		if (stage==1) {
			if (lastDrawIcon==null || !lastDrawIcon.equals(icon) || lastDrawWidth!=objectRect.width || lastDrawImage==null) {
				lastDrawIcon=icon;
				lastDrawWidth=objectRect.width;
				lastDrawImage=imageSource.get(icon,getModel().animationImages,objectRect.width);
			}
			graphics.drawImage(lastDrawImage,objectRect.x,objectRect.y,objectRect.width,objectRect.height,null);
		}

		if (stage==2) {
			graphics.setColor(borderColor);
			for (int i=0;i<borderWidth;i++) graphics.drawRect(objectRect.x+i,objectRect.y+i,objectRect.width-2*i,objectRect.height-2*i);
		}
	}

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		Color borderColor;
		int borderWidth;
		if (isSelected() && showSelectionFrames) {
			borderColor=Color.GREEN;
			borderWidth=2;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				borderColor=Color.BLUE;
				borderWidth=2;
			} else {
				borderColor=Color.DARK_GRAY;
				borderWidth=0;
			}
		}

		drawRect(graphics,drawRect,zoom,borderColor,borderWidth,new Color(235,235,235),1);
		drawRect(graphics,drawRect,zoom,borderColor,borderWidth,new Color(235,235,235),2);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü ("Wegpunkt")
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.WayPoint.Name");
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementWayPointDialog(owner,ModelElementWayPoint.this,readOnly);
		};
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		final JMenuItem item=new JMenuItem(Language.tr("PathEditor.MenuItem"));
		item.setIcon(Images.MODELEDITOR_ELEMENT_VERTEX.getIcon());
		item.addActionListener(e->commandPathEditor(owner));
		item.setEnabled(!readOnly);
		popupMenu.add(item);
	}

	private void commandPathEditor(final Component owner) {
		if (getSurface()==null) return;
		final ModelSurface mainSurface=(getSurface().getParentSurface()==null)?getSurface():getSurface().getParentSurface();
		final PathEditorDialog dialog=new PathEditorDialog(owner,mainSurface,getModel().pathSegments);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			dialog.storeSetup(getModel().pathSegments);
			fireChanged();
		}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.WayPoint.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		final Element sub=doc.createElement(Language.tr("Surface.WayPoint.XML.Icon"));
		node.appendChild(sub);
		sub.setTextContent(icon);

		for (WayPointRecord wayPoint: wayPoints) wayPoint.addToXML(doc,node);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementWayPoint";
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.WayPoint.XML.Icon",name)) {
			if (!content.isEmpty()) icon=content;
			return null;
		}

		if (Language.trAll("Surface.WayPoint.XML.Record",name)) {
			final WayPointRecord wayPoint=new WayPointRecord();
			error=wayPoint.loadFromXML(node);
			if (error!=null) return error+ "("+String.format(Language.tr("Surface.WayPoint.XML.Record.Error"),wayPoints.size()+1)+")";
			wayPoints.add(wayPoint);
			return null;
		}

		return null;
	}

	private String getHTMLDrawWayPoint(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		final BufferedImage imageObj=imageSource.get(icon,getModel().animationImages,getSize().width);

		String image;
		try {
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			ImageIO.write(imageObj,"png",out);
			final byte[] bytes=out.toByteArray();
			final String base64bytes=Base64.getEncoder().encodeToString(bytes);
			image="data:image/png;base64,"+base64bytes;
		} catch (IOException e) {
			image="";
		}

		sb.append("function drawWayPoint(point) {\n");

		sb.append("  var img=new Image();\n");
		sb.append("  img.src=\""+image+"\";\n");
		sb.append("  img.onload=function(){context.drawImage(img,point.x,point.y);};\n");

		sb.append("}\n");

		return sb.toString();
	}

	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawWayPoint",builder->getHTMLDrawWayPoint(builder));

		final Point p=getPosition(true);
		outputBuilder.outputBody.append("drawWayPoint({x: "+p.x+",y: "+p.y+"});\n");
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}
}
