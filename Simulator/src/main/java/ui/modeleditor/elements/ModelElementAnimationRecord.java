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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementRecord;
import simulator.runmodel.SimulationData;
import statistics.StatisticsDataCollector;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationTableDialog;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Stellt die Daten eines {@link ModelElementRecord}-Elements während der Animation dar.
 * @author Alexander Herzog
 * @see ModelElementRecord
 */
public class ModelElementAnimationRecord extends ModelElementAnimationDiagramBase {
	private Semaphore drawLock=new Semaphore(1);
	private StatisticsDataCollector statistics1;
	private StatisticsDataCollector statistics2;
	private String valuesString;

	private int recordId;
	private int displayPoints;
	private Color dataColor;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationRecord</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationRecord(final EditModel model, final ModelSurface surface) {
		super(model,surface);
		recordId=-1;
		displayPoints=1000;
		dataColor=Color.BLUE;
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationRecord.Tooltip");
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_RECORD.getURL();
	}

	/**
	 * Liefert den Namen des {@link ModelElementRecord}-Elements dessen Daten angezeigt werden sollen
	 * @return	Name des {@link ModelElementRecord}-Elements dessen Daten angezeigt werden sollen
	 */
	public int getRecordId() {
		return recordId;
	}

	/**
	 * Stellt den den Namen des {@link ModelElementRecord}-Elements, dessen Daten angezeigt werden sollen, ein.
	 * @param recordId	Name des {@link ModelElementRecord}-Elements dessen Daten angezeigt werden sollen
	 */
	public void setRecordId(int recordId) {
		this.recordId=recordId;
	}

	/**
	 * Gibt an, wie viele Datenpunkte angezeigt werden sollen.
	 * @return	Anzahl der anzuzeigenden Datenpunkte (positive Zahl)
	 */
	public int getDisplayPoints() {
		return displayPoints;
	}

	/**
	 * Stellt ein, wie viele Datenpunkte angezeigt werden sollen.
	 * @param displayPoints	Anzahl der anzuzeigenden Datenpunkte (positive Zahl)
	 */
	public void setDisplayPoints(int displayPoints) {
		if (displayPoints>0) this.displayPoints=displayPoints;
	}

	/**
	 * Liefert die aktuelle Farbe der Daten
	 * @return	Aktuelle Farbe der Daten
	 */
	public Color getDataColor() {
		return dataColor;
	}

	/**
	 * Stellt die Farbe der Daten ein
	 * @param color	Farbe der Daten
	 */
	public void setDataColor(final Color color) {
		if (color!=null) this.dataColor=color;
		fireChanged();
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationRecord)) return false;

		if (recordId!=((ModelElementAnimationRecord)element).recordId) return false;
		if (displayPoints!=((ModelElementAnimationRecord)element).displayPoints) return false;
		if (!((ModelElementAnimationRecord)element).dataColor.equals(dataColor)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationRecord) {
			recordId=((ModelElementAnimationRecord)element).recordId;
			displayPoints=((ModelElementAnimationRecord)element).displayPoints;
			dataColor=((ModelElementAnimationRecord)element).dataColor;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationRecord clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationRecord element=new ModelElementAnimationRecord(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private void drawDummyDiagramLines(final Graphics2D g, final Rectangle rectangle) {
		final int[] v=new int[]{1,3,7,2,5,8,4,2,5,8};

		final double[] data1=new double[30];
		final double[] data2=new double[30];
		for (int i=0;i<10;i++) {
			data1[3*i]=i+1;
			data2[3*i]=i+1;
			data1[3*i+1]=i+1;
			data2[3*i+1]=i+1+FastMath.pow(-1,i)*v[i];
			data1[3*i+2]=i+1;
			data2[3*i+2]=i+1+FastMath.pow(-1,i+1)*v[9-i];
		}
		drawDiagramPoints(g,rectangle,data1,data2,data1.length);
	}

	private final Stroke lineStroke=new BasicStroke(1);
	private final Stroke pointStroke=new BasicStroke(2);

	private double lastZoom;
	private Font lastFont;

	private void drawDiagramLines(final Graphics2D g, final Rectangle rectangle, final double[] data, final int count) {
		if (count<2 || displayPoints<2) return;
		final int index2=count-1;
		final int index1=Math.max(0,count-displayPoints);

		double min=data[index1];
		double max=data[index1];
		for (int i=index1+1;i<=index2;i++) {
			final double value=data[i];
			if (min>value) min=value;
			if (max<value) max=value;
		}

		if (min==max) return;

		final double deltaX=rectangle.width/((double)(index2-index1));
		final double deltaY=rectangle.height/(max-min);

		double last=data[index1];
		int x1=0;
		int y1=(int)((last-min)*deltaY+0.5);

		g.setColor(dataColor);
		g.setStroke(lineStroke);

		for (int i=index1+1;i<=index2;i++) {
			final double value=data[i];
			final int x2=(int)((i-index1)*deltaX+0.5);
			final int y2=(int)((value-min)*deltaY+0.5);
			g.drawLine(rectangle.x+x1,rectangle.y+rectangle.height-y1,rectangle.x+x2,rectangle.y+rectangle.height-y2);
			x1=x2;
			y1=y2;
		}

		g.setColor(Color.BLACK);

		final int ascent=g.getFontMetrics().getAscent();
		final int descent=g.getFontMetrics().getDescent();
		final int x=rectangle.x+2;
		g.drawString(NumberTools.formatNumber(min),x,rectangle.y+rectangle.height-1-descent);
		g.drawString(NumberTools.formatNumber(max)+", "+NumberTools.formatLong(Math.min(count,displayPoints))+" "+valuesString,x,rectangle.y+1+ascent);
	}

	private void drawDiagramPoints(final Graphics2D g, final Rectangle rectangle, final double[] data1, final double[] data2, final int count) {
		if (count<2 || displayPoints<2) return;
		final int index2=count-1;
		final int index1=Math.max(0,count-displayPoints);

		double maxX=data1[index1];
		double maxY=data2[index1];
		for (int i=index1+1;i<=index2;i++) {
			final double value1=data1[i];
			final double value2=data2[i];
			if (maxX<value1) maxX=value1;
			if (maxY<value2) maxY=value2;
		}

		final double deltaX=rectangle.width/maxX;
		final double deltaY=rectangle.height/maxY;

		g.setColor(dataColor);
		g.setStroke(pointStroke);

		for (int i=index1;i<=index2;i++) {
			final double valueX=data1[i];
			final double valueY=data2[i];
			final int x=(valueX<=0)?0:(int)(valueX*deltaX+0.5);
			final int y=(valueY<=0)?0:(int)(valueY*deltaY+0.5);

			g.drawOval(rectangle.x+x-1,rectangle.y+rectangle.height-y-1,2,2);
		}

		g.setColor(Color.BLACK);

		final int ascent=g.getFontMetrics().getAscent();
		final int descent=g.getFontMetrics().getDescent();
		final int x=rectangle.x+2;
		final int y=rectangle.y+rectangle.height-1-descent;
		g.drawString("0",x,y);
		if (valuesString==null) {
			g.drawString(NumberTools.formatNumber(maxY),x,rectangle.y+1+ascent);
		} else {
			g.drawString(NumberTools.formatNumber(maxY)+", "+NumberTools.formatLong(Math.min(count,displayPoints))+" "+valuesString,x,rectangle.y+1+ascent);
		}
		final String s=NumberTools.formatNumber(maxX);
		g.drawString(s,rectangle.x+rectangle.width-2-g.getFontMetrics().stringWidth(s),y);
	}

	@Override
	protected void drawDiagramData(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		if (statistics1==null) {
			drawDummyDiagramLines(g,rectangle);
			return;
		}

		if (zoom!=lastZoom || lastFont==null) {
			lastFont=FontCache.getFontCache().getFont(FontCache.defaultFamily,0,(int)FastMath.round(9*zoom));
			lastZoom=zoom;
		}

		final Font saveFont=g.getFont();
		g.setFont(lastFont);

		drawLock.acquireUninterruptibly();
		try {
			final double[] data1=statistics1.getValuesReadOnly();
			final double[] data2=(statistics2==null)?null:statistics2.getValuesReadOnly();
			final int count1=statistics1.getCount();
			final int count2=(statistics2==null)?0:statistics2.getCount();
			if (data2==null) drawDiagramLines(g,rectangle,data1,count1); else drawDiagramPoints(g,rectangle,data1,data2,Math.min(count1,count2));
		} finally {
			drawLock.release();
		}

		g.setFont(saveFont);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationRecord.Name");
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
			new ModelElementAnimationRecordDialog(owner,ModelElementAnimationRecord.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationRecord.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (recordId>=0) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationRecord.XML.RecordID"));
			node.appendChild(sub);
			sub.setTextContent(""+recordId);
		}

		if (displayPoints>0) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationRecord.XML.DisplayPoints"));
			node.appendChild(sub);
			sub.setTextContent(""+displayPoints);
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationRecord.XML.DataColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(dataColor));
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

		if (Language.trAll("Surface.AnimationRecord.XML.RecordID",name)) {
			final Long L=NumberTools.getNotNegativeLong(content);
			if (L==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			recordId=L.intValue();
			return null;
		}

		if (Language.trAll("Surface.AnimationRecord.XML.DisplayPoints",name)) {
			final Long L=NumberTools.getPositiveLong(content);
			if (L==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			displayPoints=L.intValue();
			return null;
		}

		if (Language.trAll("Surface.AnimationRecord.XML.DataColor",name) && !content.trim().isEmpty()) {
			dataColor=EditModel.loadColor(content);
			if (dataColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		return null;
	}

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (isPreview || recordId<0) return false;

		if (statistics1==null) {
			drawLock.acquireUninterruptibly();
			try {
				if (simData.runModel.elementsFast.length<=recordId) return false;
				final RunElement element=simData.runModel.elementsFast[recordId];
				if (!(element instanceof RunElementRecord)) return false;
				final StatisticsDataCollector[] statistics=((RunElementRecord)element).getStatistics(simData);
				statistics1=statistics[0];
				statistics2=statistics[1];
			} finally {
				drawLock.release();
			}
		}

		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		statistics1=null;
		statistics2=null;
		valuesString=Language.tr("Surface.AnimationRecord.Values");
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationRecord";
	}

	private String getHTMLAnimationRecord(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationRecord(rect,borderColor,borderWidth,fillColor) {\n");

		sb.append("  if (typeof(fillColor)!=\"undefined\") {\n");
		sb.append("    context.fillStyle=fillColor;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("  context.strokeStyle=\"blue\";\n");
		sb.append("  context.lineWidth=2;\n");
		sb.append("  var x1=rect.x;\n");
		sb.append("  var x2=Math.round(rect.x+0.25*rect.w);\n");
		sb.append("  var x3=Math.round(rect.x+0.5*rect.w);\n");
		sb.append("  var x4=Math.round(rect.x+0.75*rect.w);\n");
		sb.append("  var y1=Math.round(rect.y+0.75*rect.h);\n");
		sb.append("  var y2=Math.round(rect.y+0.25*rect.h);\n");
		sb.append("  var y3=Math.round(rect.y+0.5*rect.h);\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(x1,y1);\n");
		sb.append("  context.lineTo(x2,y2);\n");
		sb.append("  context.lineTo(x3,y3);\n");
		sb.append("  context.lineTo(x4,y2);\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationRecord",builder->getHTMLAnimationRecord(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationRecord("+rect+","+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationRecord("+rect+","+border+","+borderWidth+","+fill+");\n");
		}
	}

	@Override
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		return simData!=null;
	}

	private Table getAnimationRunTimeTableData(final SimulationData simData) {
		final Table table=new Table();

		final RunElementRecord record=(RunElementRecord)simData.runModel.elementsFast[recordId];

		if (statistics2==null) {
			table.addLine(new String[]{
					Language.tr("Surface.AnimationRecord.ValueNumber"),
					record.name+" - "+record.getExpression1()
			});
		} else {
			table.addLine(new String[]{
					Language.tr("Surface.AnimationRecord.ValueNumber"),
					record.name+" - "+record.getExpression1(),
					record.name+" - "+record.getExpression2()
			});
		}

		drawLock.acquireUninterruptibly();
		try {
			final int count;
			if (statistics2==null) {
				count=statistics1.getCount();
			} else {
				count=Math.min(statistics1.getCount(),statistics2.getCount());
			}
			final int index2=count-1;
			final int index1=Math.max(0,count-displayPoints);

			final double[] data1=statistics1.getValuesReadOnly();
			final double[] data2=(statistics2==null)?null:statistics2.getValuesReadOnly();

			for (int i=index1;i<=index2;i++) {
				final List<String> line=new ArrayList<>((data2==null)?2:3);
				line.add(NumberTools.formatLong(i+1));
				line.add(NumberTools.formatNumber(data1[i]));
				if (data2!=null) line.add(NumberTools.formatNumber(data2[i]));
				table.addLine(line);
			}
		} finally {
			drawLock.release();
		}

		return table;
	}

	@Override
	public void showElementAnimationStatisticsData(final Component owner, final SimulationData simData) {
		if (simData==null) return;
		new ModelElementAnimationTableDialog(owner,getContextMenuElementName()+" (id="+getId()+")",()->getAnimationRunTimeTableData(simData));
	}

	@Override
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
		if (simData==null) return;
		ModelElementAnimationTableDialog.buildPopupMenuItem(owner,menu,getAnimationRunTimeTableData(simData));
	}
}
