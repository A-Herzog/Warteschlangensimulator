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
package ui.modeleditor.coreelements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import tools.SetupData;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.ElementWithAnimationDisplay;
import ui.modeleditor.elements.ModelElementAnimationBar;
import ui.modeleditor.elements.ModelElementAnimationLCD;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationPointerMeasuring;
import ui.modeleditor.elements.ModelElementAnimationRecord;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementAnimationTrafficLights;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementReference;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.speedup.BackgroundSystem;

/**
 * Basisklasse für alle Box-förmigen Modell-Elemente
 * @author Alexander Herzog
 */
public class ModelElementBox extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Benutzerdefinierte Hintergrundfarbe
	 * @see #getUserBackgroundColor()
	 * @see #setUserBackgroundColor(Color)
	 * @see #getDrawBackgroundColor()
	 */
	private Color userBackgroundColor;

	/**
	 * Referenz auf das Setup-Singleton.
	 */
	private final SetupData setup=SetupData.getSetup();

	private boolean drawText=true;

	/**
	 * Konstruktor der Klasse <code>ModelElementBox</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param shape	Darzustellende Form ({@link Shapes})
	 */
	public ModelElementBox(final EditModel model, final ModelSurface surface, final Shapes.ShapeType shape) {
		super(model,surface,new Dimension(100,50),shape);
	}

	/**
	 * Gibt an, ob Texte in die Stationsbox gezeichnet werden sollen
	 * @return	Texte in die Stationsbox zeichnen
	 */
	protected boolean isDrawText() {
		return drawText;
	}

	/**
	 * Stellt ein, ob Texte in die Stationsbox gezeichnet werden sollen
	 * @param drawText	Texte in die Stationsbox zeichnen
	 */
	protected void setDrawText(final boolean drawText) {
		this.drawText=drawText;
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmenü angezeigt werden soll.<br>
	 * Generiert im Falle eines Elements mit eigener Position (= einem normal darstellbaren Element) ein Icon basierend auf der Darstellung des Elements selber.
	 * @return	Icon zur Beschriftung des Elements im Kontextmenü oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	@Override
	public Icon buildIcon() {
		final Dimension size=getSize();
		final BufferedImage image=new BufferedImage(size.width+15,size.height+15,BufferedImage.TYPE_4BYTE_ABGR);

		temporaryMoveToTop();
		try {
			final boolean saveShowIDs=setup.showIDs;
			try {
				setup.showIDs=false;
				drawToGraphics(image.getGraphics(),new Rectangle(0,0,size.width+15,size.height+15),1.0,false);
			} finally {
				setup.showIDs=saveShowIDs;
			}
		} finally {
			temporaryMoveRestore();
		}

		return getScaledElementIcon(image);
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementBox)) return false;

		if (userBackgroundColor!=null || ((ModelElementBox)element).userBackgroundColor!=null) {
			if (userBackgroundColor==null || ((ModelElementBox)element).userBackgroundColor==null) return false;
			if (!((ModelElementBox)element).userBackgroundColor.equals(userBackgroundColor)) return false;
		}

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementBox) {
			userBackgroundColor=((ModelElementBox)element).userBackgroundColor;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementBox clone(final EditModel model, final ModelSurface surface) {
		final ModelElementBox element=new ModelElementBox(model,surface,shape.shapeType);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Box.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	public String getTypeName() {
		return Language.tr("Surface.Box.Name");
	}

	/**
	 * Liefert einen Infotext, der unter der Box angezeigt werden soll.<br>
	 * Es kann auch <code>null</code> zurückgegeben werden.
	 * @return	Optionale Infomeldung oder <code>null</code> wenn keine Meldung angezeigt werden soll.
	 */
	protected String getInfoMessage() {
		return null;
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zurückgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	protected String getErrorMessage() {
		return BackgroundSystem.checkModelElement(this);
	}

	/**
	 * Liefert optional eine zusätzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zusätzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	public String getSubTypeName() {
		return null;
	}

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	public Color getTypeDefaultBackgroundColor() {
		return Color.LIGHT_GRAY;
	}

	private static final Color highContrastColor=new Color(255,255,240);

	/**
	 * Liefert die tatsächlich zum Zeichnen zu verwendende Farbe
	 * @return	Zu verwendende Farbe (entweder Vorgabefarbe oder benutzerdefinierte Farbe, aber nie <code>null</code>)
	 */
	public final Color getDrawBackgroundColor() {
		if (setup.useHighContrasts) return highContrastColor;

		if (userBackgroundColor!=null) return userBackgroundColor;
		return getTypeDefaultBackgroundColor();
	}

	/**
	 * Liefert die momentan eingestellte benutzerdefinierte Hintergrundfarbe
	 * @return	Benutzerdefinierte Hintergrundfarbe oder <code>null</code>, wenn die Vorgabefarbe verwendet werden soll.
	 */
	public Color getUserBackgroundColor() {
		return userBackgroundColor;
	}

	/**
	 * Stellt die Hintergrundfarbe ein
	 * @param userBackgroundColor	Hintergrundfarbe oder <code>null</code>, wenn die Vorgabefarbe verwendet werden soll
	 */
	public void setUserBackgroundColor(final Color userBackgroundColor) {
		this.userBackgroundColor=userBackgroundColor;
		fireChanged();
	}

	/**
	 * Liefert das momentan eingestellte benutzerdefiniertes Stationsbild
	 * @return	Benutzerdefiniertes Stationsbild oder <code>null</code>, wenn die Vorgabeform verwendet werden soll.
	 */
	public BufferedImage getUserBackgroundImage() {
		return shape.getCustomImage();
	}

	/**
	 * Stellt die ein benutzerdefiniertes Stationsbild ein
	 * @param customImage	Benutzerdefiniertes Stationsbild oder <code>null</code>, wenn die Vorgabeform verwendet werden soll.
	 */
	public void setUserBackgroundImage(final BufferedImage customImage) {
		shape.setCustomImage(customImage);
		fireChanged();
	}

	private double lastZoomFontBox=-1;
	private boolean lastUseHighContrasts;
	private Font lastFontDefaultBox;
	private Font lastFontBoldBox;

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
				borderWidth=1;
			}
		}

		if (lastZoomFontBox!=zoom || lastFontDefaultBox==null || lastFontBoldBox==null || lastUseHighContrasts!=setup.useHighContrasts) {
			lastFontDefaultBox=new Font(Font.DIALOG,setup.useHighContrasts?Font.BOLD:0,(int)FastMath.round(11*zoom));
			lastFontBoldBox=new Font(Font.DIALOG,Font.BOLD,(int)FastMath.round(13*zoom));
			lastZoomFontBox=zoom;
			lastUseHighContrasts=setup.useHighContrasts;
		}

		final Rectangle objectRect=drawRect(graphics,drawRect,zoom,borderColor,borderWidth,getDrawBackgroundColor(),1);
		if (objectRect==null) return;

		graphics.setColor(Color.BLACK);

		setClip(graphics,drawRect,objectRect);

		if (drawText  && getUserBackgroundImage()==null) {

			int x,y;
			String textType=getTypeName();
			final String textName=getName();

			int yStart;
			graphics.setFont(lastFontDefaultBox);
			if (textType!=null && !textType.isEmpty()) {
				final String subType=getSubTypeName();
				if (subType!=null && !subType.isEmpty()) {
					x=objectRect.x+(objectRect.width-graphics.getFontMetrics().stringWidth(textType))/2;
					if (textName!=null && !textName.isEmpty()) {
						y=objectRect.y+2*objectRect.height/3-2*graphics.getFontMetrics().getDescent()-graphics.getFontMetrics().getAscent();
					} else {
						y=objectRect.y+objectRect.height/2-graphics.getFontMetrics().getDescent();
					}
					graphics.drawString(textType,x,y);
					final String textSub="("+subType+")";
					x=objectRect.x+(objectRect.width-graphics.getFontMetrics().stringWidth(textSub))/2;
					if (textName!=null && !textName.isEmpty()) {
						y=objectRect.y+2*objectRect.height/3-graphics.getFontMetrics().getDescent();
					} else {
						y=objectRect.y+objectRect.height/2+graphics.getFontMetrics().getAscent();
					}
					graphics.drawString(textSub,x,y);
					yStart=objectRect.y+2*objectRect.height/3;
				} else {
					x=objectRect.x+(objectRect.width-graphics.getFontMetrics().stringWidth(textType))/2;
					if (textName!=null && !textName.isEmpty()) {
						y=objectRect.y+objectRect.height/2-graphics.getFontMetrics().getDescent();
					} else {
						y=objectRect.y+objectRect.height/2+(graphics.getFontMetrics().getAscent()-graphics.getFontMetrics().getDescent())/2;
					}
					graphics.drawString(textType,x,y);
					yStart=objectRect.y+objectRect.height/2;
				}
			} else {
				yStart=objectRect.y+objectRect.height/2;
			}

			if (textName!=null && !textName.isEmpty()) {
				graphics.setFont(lastFontBoldBox);
				x=objectRect.x+(objectRect.width-graphics.getFontMetrics().stringWidth(textName))/2;
				y=yStart+graphics.getFontMetrics().getAscent();
				graphics.drawString(textName,x,y);
			}
		}

		drawRect(graphics,drawRect,zoom,borderColor,borderWidth,getDrawBackgroundColor(),2);
	}

	private double infoFontZoom;
	private Font infoFont;
	private int infoFontAscent;
	private int infoFontHeight;

	/**
	 * Zeichnet das Element inkl. optionalem Markierungsrahmen und optionalen Animations-Laufzeitdaten
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (in Pixeln)
	 * @param fillColor	Füllfarbe (oder <code>null</code>, wenn die Box transparent sein soll)
	 * @param stage	Gibt an, ob nur der Hintergrund (stage=1) oder nur der Rahmen (stage=2) gezeichnet werden soll
	 * @return	Liefert das Rechteck der Box, sofern dieses zumindest teilweise sichtbar ist, oder <code>null</code> wenn die komplette Box außerhalb des Sichtbereichs liegt und daher nicht gezeichnet werden muss.
	 * @see #drawElementShape(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 * @see #getRunDataAnimationInfo()
	 */
	@Override
	protected final Rectangle drawRect(final Graphics graphics, final Rectangle drawRect, final double zoom, final Color borderColor, final int borderWidth, final Color fillColor, final int stage) {
		Rectangle objectRect=super.drawRect(graphics,drawRect,zoom,borderColor,borderWidth,fillColor,stage);

		if (stage==2) {
			String info=getRunDataAnimationInfo();
			String[] infoMulti=null;
			Color color=Color.GRAY;

			if (surface!=null) {
				final String msg=getInfoMessage();
				if (msg!=null) {
					if (info==null) info=msg; else info=msg+"\n, "+info;
					color=Color.BLACK;
				}

				final String error=getErrorMessage();
				if (error!=null) {
					if (info!=null) {
						infoMulti=(info+"\n"+splitLine(error,20)).split("\\n");
						info=null;
					} else {
						infoMulti=splitLine(error,20).split("\\n");
					}
					color=Color.RED;
				}
			}

			if (info!=null || infoMulti!=null) {
				if (infoFont==null || zoom!=infoFontZoom) {
					infoFontZoom=zoom;
					infoFont=new Font(Font.DIALOG,0,(int)FastMath.round(10*zoom));
					graphics.setFont(infoFont);
					infoFontAscent=graphics.getFontMetrics().getAscent();
					infoFontHeight=infoFontAscent+graphics.getFontMetrics().getDescent();
				} else {
					graphics.setFont(infoFont);
				}

				final Shape clipShape=graphics.getClip();
				graphics.setClip(drawRect);
				graphics.setColor(color);
				if (info!=null) {
					graphics.drawString(info,objectRect.x,objectRect.y+objectRect.height+infoFontAscent);
				} else {
					if (infoMulti!=null) for (int i=0;i<infoMulti.length;i++) graphics.drawString(infoMulti[i],objectRect.x,objectRect.y+objectRect.height+infoFontAscent+i*infoFontHeight);
				}
				graphics.setClip(clipShape);
			}
		}

		return objectRect;
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (userBackgroundColor!=null) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(userBackgroundColor));
		}

		final BufferedImage image=shape.getCustomImage();
		if (image!=null) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.BackgroundColor.Image"));
			node.appendChild(sub);
			try {
				final ByteArrayOutputStream stream=new ByteArrayOutputStream();
				ImageIO.write(image,"png",stream);
				sub.setTextContent(new String(Base64.getEncoder().encode(stream.toByteArray())));
			} catch (IOException e) {}
		}
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

		if (Language.trAll("Surface.XML.BackgroundColor",name)) {
			if (!content.trim().isEmpty()) {
				final Color color=EditModel.loadColor(content);
				if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
				userBackgroundColor=color;
			}
			return null;
		}

		if (Language.trAll("Surface.XML.BackgroundColor.Image",name)) {
			if (!content.trim().isEmpty()) 	try {
				final ByteArrayInputStream stream=new ByteArrayInputStream(Base64.getDecoder().decode(content));
				final BufferedImage image=ImageIO.read(stream);
				if (image!=null) shape.setCustomImage(image);
			} catch (IOException | IllegalArgumentException e) {
				return null;
			}
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation überhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung übersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation berücksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden müssten).
	 * @return	Gibt <code>true</code> zurück, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	public boolean inputConnected() {
		return true;
	}

	private Semaphore runDataDrawLock=new Semaphore(1);
	private RunElementData runData=null;
	private String runDataCurrentString;
	private String runDataSumString;
	private StringBuilder runDataInfo;
	private String runDataInfoString;
	private boolean runDataFullInformation;

	/**
	 * Liefert den Text, der als Laufzeitinformation während der Animation
	 * unter dem Element angezeigt werden soll.
	 * @return	Laufzeittext während der Animation (kann <code>null</code> sein, wenn nichts angezeigt werden soll)
	 */
	protected String getRunDataAnimationInfo() {
		if (runData==null) {
			if (SetupData.getSetup().showIDs) {
				final String s=getIDInfo();
				if (s.length()>30) return s.substring(0,25)+"...";
				return s;
			}
			return null;
		}
		runDataDrawLock.acquireUninterruptibly();
		try {
			if (runDataInfoString==null) {
				if (runDataInfo!=null) runDataInfoString=runDataInfo.toString();
			}
			return runDataInfoString;
		} finally {
			runDataDrawLock.release();
		}
	}

	/**
	 * Liefert den Text, der im Editor-Modus unter dem Element angezeigt werden soll
	 * @return	Informationstext (z.B. ID des Elements)
	 */
	protected String getIDInfo() {
		return "id="+getId();
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station während der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	public boolean showAnimationRunData() {
		return SetupData.getSetup().showStationRunTimeData;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zusätzlich auch die aktuelle Anzahl an Kunden an der Station
	 * während der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	public boolean showFullAnimationRunData() {
		return false;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		if (!showAnimationRunData()) return;
		final RunElement runElement=simData.runModel.elements.get(getId());
		if (runElement==null) return;
		runData=runElement.getData(simData);
		runDataCurrentString=Language.tr("Animation.StationRunInfo.Current")+"=";
		runDataSumString=Language.tr("Animation.StationRunInfo.Sum")+"=";
		runDataFullInformation=showFullAnimationRunData();
	}

	/**
	 * Liefert das Daten-Element zu dem Laufzeit-Element zu diesem Modell-Element.<br>
	 * Läuft keine Animation, so liefert die Funktion <code>null</code>
	 * @return	Laufzeit-Daten-Element während der Animation
	 */
	protected RunElementData getRunData() {
		return runData;
	}

	private long lastClientsAtStation=-1;
	private long lastClients=-1;
	private StringBuilder[] animationSB;
	private int animationSBNext;
	private StringBuilder animationFormatLongSB;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (runData==null || isPreview) return false;

		if (runDataFullInformation) {
			final int clientsAtStation=runData.reportedClientsAtStation(simData);
			if (clientsAtStation==lastClientsAtStation && runData.clients==lastClients) return false;
			lastClientsAtStation=clientsAtStation;
			lastClients=runData.clients;
		} else {
			if (runData.clients==lastClients) return false;
			lastClients=runData.clients;
		}

		if (animationFormatLongSB==null) animationFormatLongSB=new StringBuilder();

		if (runDataFullInformation) {
			if (animationSB==null) {
				animationSB=new StringBuilder[2];
				animationSB[0]=new StringBuilder(runDataCurrentString.length()+10+2+runDataSumString.length()+10);
				animationSB[1]=new StringBuilder(runDataCurrentString.length()+10+2+runDataSumString.length()+10);
			} else {
				animationSB[animationSBNext].setLength(0);
			}
			final StringBuilder sb=animationSB[animationSBNext];
			sb.append(runDataCurrentString);
			NumberTools.formatLongAndAppendToBuilder(lastClientsAtStation,sb);
			sb.append(',');
			sb.append(' ');
			sb.append(runDataSumString);
			NumberTools.formatLongAndAppendToBuilder(lastClients,sb);
		} else {
			if (animationSB==null) {
				animationSB=new StringBuilder[2];
				animationSB[0]=new StringBuilder(runDataSumString.length()+10);
				animationSB[1]=new StringBuilder(runDataSumString.length()+10);
			} else {
				animationSB[animationSBNext].setLength(0);
			}
			final StringBuilder sb=animationSB[animationSBNext];
			sb.append(runDataSumString);
			NumberTools.formatLongAndAppendToBuilder(lastClients,sb);
		}

		setAnimationStringBuilder(animationSB[animationSBNext],updateSimulationDataRunnableInstance);

		return true;
	}

	private Runnable updateSimulationDataRunnableInstance=()->{animationSBNext=(animationSBNext++)%2;}; /* Spart Speicherplatz */

	/**
	 * Stellt einen {@link StringBuilder} ein, dem beim Ausgeben der Infodaten unter einer Station während der Animation der Text entnommen werden soll.
	 * @param sb	{@link StringBuilder} dem während der Animation der Text mit den Stations-Infodaten entnommen werden soll
	 * @param processLocked	Optionales {@link Runnable}, welches nach der Speicherung des {@link StringBuilder} ausgeführt wird.
	 */
	protected final void setAnimationStringBuilder(final StringBuilder sb, final Runnable processLocked) {
		runDataDrawLock.acquireUninterruptibly();
		try {
			runDataInfo=sb;
			runDataInfoString=null;
			if (processLocked!=null) processLocked.run();
		} finally {
			runDataDrawLock.release();
		}
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		descriptionBuilder.beginStation(this);
	}

	private String getInfoBox(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function addBoxElementInfo(rect,info) {\n");
		sb.append("  if (info==\"\") return;\n");
		sb.append("  var span=document.createElement(\"span\");\n");
		sb.append("  span.title=info;\n");
		sb.append("  span.style.position=\"absolute\";\n");
		sb.append("  span.style.left=rect.x+\"px\";\n");
		sb.append("  span.style.top=rect.y+\"px\";\n");
		sb.append("  span.style.width=rect.w+\"px\";\n");
		sb.append("  span.style.height=rect.h+\"px\";\n");
		sb.append("  document.getElementById(\"surface_box\").appendChild(span);\n");
		sb.append("}\n");

		return sb.toString();
	}

	private String getBoxElementFont(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function setBoxElementFont(large) {\n");

		sb.append("  var size=(typeof(large)!=\"undefined\" && large)?12:9;\n");
		sb.append("  context.font=size+\"px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.textAlign=\"center\";\n");
		sb.append("  context.textBaseline=\"middle\";\n");
		sb.append("  context.fillStyle=\"black\";\n");

		sb.append("}\n");

		return sb.toString();
	}

	private void specialHTMLOutput(final HTMLOutputBuilder outputBuilder) {
		/* Notwendige JS-Funktionen bereithalten */
		outputBuilder.addJSUserFunction("infoBox",builder->getInfoBox(builder));
		outputBuilder.addJSUserFunction("boxElementFont",builder->getBoxElementFont(builder));

		/* Elementenform ausgeben */
		specialOutputShape(outputBuilder,Color.DARK_GRAY,1,getDrawBackgroundColor());

		final Point p=getPosition(true);
		final Dimension s=getSize();

		/* Text auf Element schreiben */
		if (drawText) {
			String textType=getTypeName();
			final String textName=getName();
			final int x=p.x+s.width/2;
			int y;
			int yStart;

			outputBuilder.outputBody.append("setBoxElementFont();\n");

			if (textType!=null && !textType.isEmpty()) {
				final String subType=getSubTypeName();
				if (subType!=null && !subType.isEmpty()) {
					if (textName!=null && !textName.isEmpty()) y=s.height/6; else y=s.height/3;
					outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textType)+"\","+x+","+(p.y+y)+");\n");
					final String textSub="("+subType+")";
					if (textName!=null && !textName.isEmpty()) y=s.height/3; else y=2*s.height/3;
					outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textSub)+"\","+x+","+(p.y+y)+");\n");
					yStart=2*s.height/3;
				} else {
					if (textName!=null && !textName.isEmpty()) y=s.height/3; else y=s.height/2;
					outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textType)+"\","+x+","+(p.y+y)+");\n");
					yStart=2*s.height/3;
				}
			} else {
				yStart=s.height/2;
			}

			if (textName!=null && !textName.isEmpty()) {
				outputBuilder.outputBody.append("setBoxElementFont(true);\n");
				outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textName)+"\","+x+","+(p.y+yStart)+");\n");
			}
		}

		/* Tooltip-Beschreibung einfügen */
		final ModelDescriptionBuilderStyled descriptionBuilder=new ModelDescriptionBuilderStyled(getModel(),true);
		buildDescription(descriptionBuilder);
		String info=descriptionBuilder.getText();
		info=HTMLOutputBuilder.encodeTextFieldHTML(info);
		info=info.replaceAll("\n","\\\\n");
		outputBuilder.outputBody.append("addBoxElementInfo({x: "+p.x+",y: "+p.y+",w: "+s.width+",h: "+s.height+"},\""+info+"\");\n");
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialHTMLOutput((HTMLOutputBuilder)outputBuilder);
	}

	/**
	 * Prüft mögliche angebundene externe Datenquellen und liefert das Ergebnis zurück.
	 * @return	Informationen zur Verfügbarkeit externer Datenquellen.
	 * @see DataCheckResult
	 */
	public DataCheckResult checkExternalData() {
		return DataCheckResult.noCheckNeeded(this);
	}

	/**
	 * Stellt neue Verbindungskanten ein, ohne dabei die alten Kanten löschen und ohne die neuen
	 * Kanten zu benachrichtigen. Diese Funktion wird von {@link ModelElementReference} verwendet,
	 * um die Kopie ein konkreten Elements an der Platzhalterstelle einzufügen
	 * @param connectionsIn	Liste der einlaufenden Kanten (kann leer sind, aber ist nie <code>null</code>)
	 * @param connectionsOut	Liste der auslaufenden Kanten (kann leer sind, aber ist nie <code>null</code>)
	 * @return	Gibt <code>true</code> zurück, wenn die Anzahl an ein- und auslaufenden Kanten zu dem Element passt und eingetragen werden konnte
	 */
	public boolean setReferenceEdges(final List<ModelElementEdge> connectionsIn, final List<ModelElementEdge> connectionsOut) {
		return false;
	}

	private String lastAdditionalIcon=null;
	private String lastAdditionalClientIcon=null;
	private String lastAdditionalTransporterIcon=null;

	/**
	 * Stellt ein zusätzliches Icon ein, welches auf das Shape gezeichnet wird.
	 * @param name	Name des Icons (gemäß {@link ModelClientData}).
	 */
	protected final void setAdditionalClientIconFromName(final String name) {
		lastAdditionalClientIcon=null;
		lastAdditionalTransporterIcon=null;

		final EditModel model=getModel();
		if (model==null || surface==null || shape==null) return;

		if (name==null || name.trim().isEmpty()) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}

		lastAdditionalClientIcon=name;
		setAdditionalIconFromName(getModel().clientData.getIcon(name));
	}

	/**
	 * Stellt ein zusätzliches Icon ein, welches auf das Shape gezeichnet wird.
	 * @param name	Name des Icons (gemäß {@link ModelTransporters})
	 */
	protected final void setAdditionalTransporterIconFromName(final String name) {
		lastAdditionalClientIcon=null;
		lastAdditionalTransporterIcon=null;

		final EditModel model=getModel();
		if (model==null || surface==null || shape==null) return;

		if (name==null || name.trim().isEmpty()) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}
		final ModelTransporter transporter=getModel().transporters.get(name);

		if (transporter==null) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}

		lastAdditionalTransporterIcon=name;
		setAdditionalIconFromName(transporter.getEastLoadedIcon());
	}

	/**
	 * Stellt ein zusätzliches Icon ein, welches auf das Shape gezeichnet wird.
	 * @param name	Name des Icons (gemäß {@link ModelClientData}).
	 */
	protected final void setAdditionalIconFromName(final String name) {
		final EditModel model=getModel();
		if (model==null || surface==null || shape==null) return;

		if (name==null || name.trim().isEmpty()) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}

		if (lastAdditionalIcon!=null && lastAdditionalIcon.equals(name)) return;
		lastAdditionalIcon=name;
		final AnimationImageSource imageSource=new AnimationImageSource();
		final BufferedImage image=imageSource.get(name,getModel().animationImages,16);
		shape.setAdditionalIcon(image);
	}

	/**
	 * Aktualisiert die kleinen Icons auf den Stationen, wenn zuvor z.B. das Icon für einen Kundentypen verändert wurde.
	 * @see ModelSurface#updateAdditionalIcons()
	 */
	public final void updateAdditionalIcon() {
		if (lastAdditionalClientIcon!=null) {
			setAdditionalIconFromName(getModel().clientData.getIcon(lastAdditionalClientIcon));
		}
		if (lastAdditionalTransporterIcon!=null) {
			final ModelTransporter transporter=getModel().transporters.get(lastAdditionalTransporterIcon);
			if (transporter==null) {
				lastAdditionalTransporterIcon=null;
			} else {
				setAdditionalIconFromName(transporter.getEastLoadedIcon());
			}
		}
	}

	private String getNameForReference() {
		final StringBuilder sb=new StringBuilder();
		sb.append(getTypeName());
		if (!getName().trim().isEmpty()) {
			sb.append(" \"");
			sb.append(getName());
			sb.append("\"");
		}
		sb.append(String.format(" (id=%d)",getId()));
		return sb.toString();
	}

	/**
	 * Erstellt einen Kontextmenüpunkt (und fügt ihn in ein Menü ein) zur Visualisierung von Daten des aktuellen Elements in Form einer Ampel
	 * @param condition	Bedingung für "rot"
	 * @param parentMenu	Menü in das der Menüpunkt eingefügt werden soll
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 * @see ModelElementBox#addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	protected final JMenuItem addVisualizationTrafficLightsMenuItem(final String condition, final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		final String referenceName=getNameForReference();
		final ModelElementAnimationTrafficLights element=new ModelElementAnimationTrafficLights(null,null);
		element.setLightsCount(2);
		element.setExpressionOne(condition);

		element.setName(referenceName);
		return addVisualizationMenuItem(element,element.getContextMenuElementName(),parentMenu,addElement);
	}

	/**
	 * Erstellt einen Kontextmenüpunkt (und fügt ihn in ein Menü ein) zur Visualisierung von Daten des aktuellen Elements in Form einer Ampel
	 * @param condition	Bedingung für "rot"
	 * @param parentMenu	Menü in das der Menüpunkt eingefügt werden soll
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 * @param info	Zusätzliche Ausgabe beim Menüpunkt
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 * @see ModelElementBox#addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	protected final JMenuItem addVisualizationTrafficLightsMenuItem(final String condition, final JMenu parentMenu, final Consumer<ModelElementPosition> addElement, final String info) {
		final String referenceName=getNameForReference();
		final ModelElementAnimationTrafficLights element=new ModelElementAnimationTrafficLights(null,null);
		element.setLightsCount(2);
		element.setExpressionOne(condition);

		element.setName(referenceName);
		return addVisualizationMenuItem(element,element.getContextMenuElementName()+((info==null || info.isEmpty())?"":(" - "+info)),parentMenu,addElement);
	}

	/**
	 * Art der zusätzlichen Visualisierung, die über das Kontextmenü angeboten werden soll
	 * @author Alexander Herzog
	 * @see ModelElementBox#addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	protected enum VisualizationType {
		/**
		 * Aktuelle Anzahl an Kunden an der Station als Text
		 */
		TEXT_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als Text
		 */
		TEXT_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als Text
		 */
		TEXT_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als Text
		 */
		TEXT_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Text
		 */
		TEXT_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Text
		 */
		TEXT_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Text
		 */
		TEXT_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Text
		 */
		TEXT_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Text
		 */
		TEXT_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Text
		 */
		TEXT_NQ_AVERAGE_3,

		/**
		 * Aktueller Wert des Zählers als Text
		 */
		TEXT_COUNTER_VALUE,

		/**
		 * Aktueller Anteil des Zählers in der Zählergruppe als Text
		 */
		TEXT_COUNTER_PART,

		/**
		 * Aktueller Durchsatzwert als Text
		 */
		TEXT_THROUGHPUT,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als Text
		 */
		TEXT_ANALOG_VALUE,

		/**
		 * Änderungsrate eines "Analoger Wert"-Elements als Text
		 */
		TEXT_ANALOG_RATE,

		/**
		 * Aktuelle Anzahl an Kunden (aller Typen) im System
		 */
		TEXT_WIP_CLIENT,

		/**
		 * Mittlere Wartezeit der Kunden des Typs, der an der aktuellen Station erzeugt oder zugewiesen wird
		 */
		TEXT_W_CLIENT,

		/**
		 * Aktuelle Anzahl an Kunden an der Station als LCD-Anzeige
		 */
		LCD_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als LCD-Anzeige
		 */
		LCD_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als LCD-Anzeige
		 */
		LCD_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als LCD-Anzeige
		 */
		LCD_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als LCD-Anzeige
		 */
		LCD_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als LCD-Anzeige
		 */
		LCD_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE_3,

		/**
		 * Aktueller Wert des Zählers als LCD-Anzeige
		 */
		LCD_COUNTER_VALUE,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als LCD-Anzeige
		 */
		LCD_ANALOG_VALUE,

		/**
		 * Aktuelle Anzahl an Kunden (aller Typen) im System als LCD-Anzeige
		 */
		LCD_WIP_CLIENT,

		/**
		 * Mittlere Wartezeit der Kunden des Typs, der an der aktuellen Station erzeugt oder zugewiesen wird, als LCD-Anzeige
		 */
		LCD_W_CLIENT,

		/**
		 * Aktuelle Anzahl an Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE_3,

		/**
		 * Aktueller Wert des Zählers als Analogskala-Anzeige
		 */
		SCALE_COUNTER_VALUE,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als Analogskala-Anzeige
		 */
		SCALE_ANALOG_VALUE,

		/**
		 * Aktuelle Anzahl an Kunden (aller Typen) im System als Analogskala-Anzeige
		 */
		SCALE_WIP_CLIENT,

		/**
		 * Mittlere Wartezeit der Kunden des Typs, der an der aktuellen Station erzeugt oder zugewiesen wird, als Analogskala-Anzeige
		 */
		SCALE_W_CLIENT,

		/**
		 * Aktuelle Anzahl an Kunden an der Station als Balken
		 */
		BAR_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als Balken
		 */
		BAR_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als Balken
		 */
		BAR_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als Balken
		 */
		BAR_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Balken
		 */
		BAR_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Balken
		 */
		BAR_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Balken
		 */
		BAR_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Balken
		 */
		BAR_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Balken
		 */
		BAR_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Balken
		 */
		BAR_NQ_AVERAGE_3,

		/**
		 * Aktueller Anteil des Zählers in der Zählergruppe als Balken
		 */
		BAR_COUNTER_PART,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als Balken
		 */
		BAR_ANALOG_VALUE,

		/**
		 * Anzahl an Kunden an der Station als Verlaufsgrafik
		 */
		CHART_WIP,

		/**
		 * Anzahl an wartenden Kunden an der Station als Verlaufsgrafik
		 */
		CHART_NQ,

		/**
		 * Wert eines "Analoger Wert"- oder "Tank"-Elements als Verlaufsgrafik
		 */
		CHART_ANALOG_VALUE,

		/**
		 * Anzeige der Werte eines Datenaufzeichnung-Elements
		 */
		RECORD
	}

	/**
	 * Erstellt einen Kontextmenüpunkt (und fügt ihn in ein Menü ein) zur Visualisierung von Daten des aktuellen Elements
	 * @param parentMenu	Menü in das der Menüpunkt eingefügt werden soll
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 * @param type	Art der Visualisierung
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 * @see ModelElementBox.VisualizationType
	 */
	protected final JMenuItem addVisualizationMenuItem(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement, final VisualizationType type) {
		final String referenceName=getNameForReference();
		String addonInfo=null;
		ModelElementPosition element=null;
		List<Object[]> data;

		switch (type) {
		case TEXT_WIP_CURRENT:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			break;
		case TEXT_WIP_AVERAGE:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			break;
		case TEXT_NQ_CURRENT:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			break;
		case TEXT_NQ_AVERAGE:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			break;
		case TEXT_NQ_CURRENT_1:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			break;
		case TEXT_NQ_CURRENT_2:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			break;
		case TEXT_NQ_CURRENT_3:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			break;
		case TEXT_NQ_AVERAGE_1:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			break;
		case TEXT_NQ_AVERAGE_2:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			break;
		case TEXT_NQ_AVERAGE_3:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			break;
		case TEXT_COUNTER_VALUE:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("counter("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			break;
		case TEXT_COUNTER_PART:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("part("+getId()+")");
			((ModelElementAnimationTextValue)element).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterPart");
			break;
		case TEXT_THROUGHPUT:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("counter("+getId()+")");
			((ModelElementAnimationTextValue)element).setDigits(5);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			break;
		case TEXT_ANALOG_VALUE:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			break;
		case TEXT_ANALOG_RATE:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("AnalogRate("+getId()+")");
			((ModelElementAnimationTextValue)element).setDigits(5);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogRate");
			break;
		case TEXT_WIP_CLIENT:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("wip()");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal");
			break;
		case TEXT_W_CLIENT:
			element=new ModelElementAnimationTextValue(null,null);
			((ModelElementAnimationTextValue)element).setExpression("WaitingTime_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime");
			break;
		case LCD_WIP_CURRENT:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			break;
		case LCD_WIP_AVERAGE:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			break;
		case LCD_NQ_CURRENT:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			break;
		case LCD_NQ_AVERAGE:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			break;
		case LCD_NQ_CURRENT_1:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			break;
		case LCD_NQ_CURRENT_2:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			break;
		case LCD_NQ_CURRENT_3:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			break;
		case LCD_NQ_AVERAGE_1:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			break;
		case LCD_NQ_AVERAGE_2:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			break;
		case LCD_NQ_AVERAGE_3:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			break;
		case LCD_COUNTER_VALUE:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("counter("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			break;
		case LCD_ANALOG_VALUE:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			break;
		case LCD_WIP_CLIENT:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("wip()");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal");
			break;
		case LCD_W_CLIENT:
			element=new ModelElementAnimationLCD(null,null);
			((ModelElementAnimationLCD)element).setExpression("WaitingTime_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime");
			break;
		case SCALE_WIP_CURRENT:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			break;
		case SCALE_WIP_AVERAGE:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			break;
		case SCALE_NQ_CURRENT:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			break;
		case SCALE_NQ_AVERAGE:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			break;
		case SCALE_NQ_CURRENT_1:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			break;
		case SCALE_NQ_CURRENT_2:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			break;
		case SCALE_NQ_CURRENT_3:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			break;
		case SCALE_NQ_AVERAGE_1:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			break;
		case SCALE_NQ_AVERAGE_2:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			break;
		case SCALE_NQ_AVERAGE_3:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			break;
		case SCALE_COUNTER_VALUE:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("counter("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			break;
		case SCALE_ANALOG_VALUE:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			break;
		case SCALE_WIP_CLIENT:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("wip()");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal");
			break;
		case SCALE_W_CLIENT:
			element=new ModelElementAnimationPointerMeasuring(null,null);
			((ModelElementAnimationPointerMeasuring)element).setExpression("WaitingTime_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime");
			break;
		case BAR_WIP_CURRENT:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("wip("+getId()+")");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			break;
		case BAR_WIP_AVERAGE:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("wip_avg("+getId()+")");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			break;
		case BAR_NQ_CURRENT:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ("+getId()+")");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			break;
		case BAR_NQ_AVERAGE:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ_avg("+getId()+")");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			break;
		case BAR_NQ_CURRENT_1:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ("+getId()+";1)");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			break;
		case BAR_NQ_CURRENT_2:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ("+getId()+";2)");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			break;
		case BAR_NQ_CURRENT_3:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ("+getId()+";3)");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			break;
		case BAR_NQ_AVERAGE_1:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ_avg("+getId()+";1)");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			break;
		case BAR_NQ_AVERAGE_2:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ_avg("+getId()+";2)");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			break;
		case BAR_NQ_AVERAGE_3:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("NQ_avg("+getId()+";3)");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			break;
		case BAR_COUNTER_PART:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("part("+getId()+")");
			((ModelElementAnimationBar)element).setMaxValue(1);
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterPart");
			break;
		case BAR_ANALOG_VALUE:
			element=new ModelElementAnimationBar(null,null);
			((ModelElementAnimationBar)element).setExpression("AnalogValue("+getId()+")");
			((ModelElementAnimationBar)element).setSize(new Dimension(25,50));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			break;
		case CHART_WIP:
			element=new ModelElementAnimationLineDiagram(null,null);
			data=new ArrayList<>();
			data.add(new Object[]{"wip("+getId()+")",Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(2)});
			((ModelElementAnimationLineDiagram)element).setExpressionData(data);
			((ModelElementAnimationLineDiagram)element).setTimeArea(3600);
			((ModelElementAnimationLineDiagram)element).setSize(new Dimension(400,200));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			break;
		case CHART_NQ:
			element=new ModelElementAnimationLineDiagram(null,null);
			data=new ArrayList<>();
			data.add(new Object[]{"NQ("+getId()+")",Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(2)});
			((ModelElementAnimationLineDiagram)element).setExpressionData(data);
			((ModelElementAnimationLineDiagram)element).setTimeArea(3600);
			((ModelElementAnimationLineDiagram)element).setSize(new Dimension(400,200));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			break;
		case CHART_ANALOG_VALUE:
			element=new ModelElementAnimationLineDiagram(null,null);
			data=new ArrayList<>();
			data.add(new Object[]{"AnalogValue("+getId()+")",Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(2)});
			((ModelElementAnimationLineDiagram)element).setExpressionData(data);
			((ModelElementAnimationLineDiagram)element).setTimeArea(3600);
			((ModelElementAnimationLineDiagram)element).setSize(new Dimension(400,200));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			break;
		case RECORD:
			element=new ModelElementAnimationRecord(null,null);
			((ModelElementAnimationRecord)element).setRecordId(getId());
			((ModelElementAnimationRecord)element).setDisplayPoints(1000);
			((ModelElementAnimationRecord)element).setSize(new Dimension(400,200));
			addonInfo=Language.tr("Surface.Popup.AddVisualization.Record");
			break;
		}

		if (element!=null) {
			element.setName(referenceName+((addonInfo==null)?"":(" - "+addonInfo)));
			return addVisualizationMenuItem(element,element.getContextMenuElementName()+((addonInfo==null)?"":(" - "+addonInfo)),parentMenu,addElement);
		} else {
			return null;
		}
	}

	/**
	 * Art der Laufzeitstatistik, die über das Kontextmenü angeboten werden soll
	 * @author Alexander Herzog
	 * @see ModelElementBox#addLongRunStatisticsMenuItem(JMenu, Consumer, LongRunStatisticsType)
	 */
	protected enum LongRunStatisticsType {
		/**
		 * Anzahl an Kunden an der Station
		 */
		WIP,

		/**
		 * Anzahl an wartenden Kunden an der Station
		 */
		NQ,

		/**
		 * Anzahl an Kunden an der Station in Bedienung
		 */
		PROCESS,

		/**
		 * Anzahl an Ankünften an der Station
		 */
		NUMBER_IN,

		/**
		 * Anzahl an Abgängen an der Station
		 */
		NUMBER_OUT,

		/**
		 * Aktueller Analogwert an der Station
		 */
		ANALOG_VALUE,

		/**
		 * Aktuelle Änderungsrate des Analogwertes an der Station
		 */
		ANALOG_RATE,

		/**
		 * Aktueller Wert des Zählers an der Station
		 */
		COUNTER,

		/**
		 * Aktzueller Anteils des Zählers an der Station in der Zählergruppe
		 */
		COUNTER_PART
	}

	/**
	 * Erstellt einen Kontextmenüpunkt (und fügt ihn in ein Menü ein) zum Hinzufügen von Laufzeitstatistikdaten
	 * @param parentMenu	Menü in das der Menüpunkt eingefügt werden soll
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Laufzeitstatistik-Eintrag zu dem Modell hinzugefügt werden soll
	 * @param type	Art der Laufzeitstatistik
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 * @see ModelElementBox.LongRunStatisticsType
	 */
	protected final JMenuItem addLongRunStatisticsMenuItem(final JMenu parentMenu, final Consumer<String> addLongRunStatistics, final LongRunStatisticsType type) {
		String name=null;
		String value=null;

		switch (type) {
		case WIP:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.WIP");
			value="WIP("+getId()+")";
			break;
		case NQ:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.NQ");
			value="NQ("+getId()+")";
			break;
		case PROCESS:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.Process");
			value="Process("+getId()+")";
			break;
		case NUMBER_IN:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.NumberIn");
			value="NumberIn("+getId()+")";
			break;
		case NUMBER_OUT:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.NumberOut");
			value="NumberOut("+getId()+")";
			break;
		case ANALOG_VALUE:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.AnalogValue");
			value="AnalogValue("+getId()+")";
			break;
		case ANALOG_RATE:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.AnalogRate");
			value="AnalogRate("+getId()+")";
			break;
		case COUNTER:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.Counter");
			value="counter("+getId()+")";
			break;
		case COUNTER_PART:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.Part");
			value="part("+getId()+")";
			break;
		}

		/* Wert schon in Liste? */
		for (ModelLongRunStatisticsElement item: getModel().longRunStatistics.getData()) if (item.expression.equals(value)) return null;

		if (name!=null && value!=null) {
			return addLongRunStatisticsMenuItem(name,value,parentMenu,addLongRunStatistics);
		} else {
			return null;
		}
	}

	/**
	 * Erstellt einen Kontextmenüpunkt (und fügt ihn in ein Menü ein) zum Hinzufügen einer typischen Folgestation des aktuellen Elements
	 * @param parentMenu	Menü in das der Menüpunkt eingefügt werden soll
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 * @param nextStationTemplate	Vorlage-Objekt für die Folgestation
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 */
	public final JMenuItem addNextStationMenuItem(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation, final ModelElementBox nextStationTemplate) {
		final JMenuItem item=new JMenuItem(nextStationTemplate.getContextMenuElementName());
		item.setToolTipText(nextStationTemplate.getToolTip());
		final URL imgURL=nextStationTemplate.getAddElementIcon();
		if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
		item.addActionListener(e->addNextStation.accept(nextStationTemplate));
		parentMenu.add(item);
		return item;
	}
}