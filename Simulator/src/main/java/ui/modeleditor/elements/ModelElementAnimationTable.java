/**
 * Copyright 2024 Alexander Herzog
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.swing.Icon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementText.TextAlign;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Tabelle mit statischen und dynamischen Elementen
 * @author Alexander Herzog
 */
public class ModelElementAnimationTable extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Vorgabe-Textfarbe
	 * @see #color
	 */
	private static final Color DEFAULT_COLOR=Color.BLACK;

	/**
	 * Rahmenbreite
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private static final double BORDER_WIDTH=1;

	/**
	 * Abstände zwischen den Zellinhalten und dem Rahmen
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private static final double PADDING=2;

	/**
	 * Tabellenzellen
	 * @see Cell
	 */
	private List<List<Cell>> cells;

	/**
	 * Zu verwendende Schriftart
	 * @see #getFontFamily()
	 * @see #setFontFamily(ui.modeleditor.elements.FontCache.FontFamily)
	 */
	private FontCache.FontFamily fontFamily=FontCache.defaultFamily;

	/**
	 * Schriftgröße
	 * @see #getTextSize()
	 * @see #setTextSize(int)
	 */
	private int textSize=14;

	/**
	 * Ausgabe des Textes im Fettdruck
	 * @see #getTextBold()
	 * @see #setTextBold(boolean)
	 */
	private boolean bold;

	/**
	 * Ausgabe des Textes im Kursivdruck
	 * @see #getTextItalic()
	 * @see #setTextItalic(boolean)
	 */
	private boolean italic;

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 * @see #isInterpretSymbols()
	 * @see #setInterpretSymbols(boolean)
	 */
	private boolean interpretSymbols;

	/**
	 * Soll Markdown interpretiert werden?
	 * @see #isInterpretMarkdown()
	 * @see #setInterpretMarkdown(boolean)
	 */
	private boolean interpretMarkdown;

	/**
	 * Sollen LaTeX-Ausdrücke interpretiert werden?
	 * @see #isInterpretLaTeX()
	 * @see #setInterpretLaTeX(boolean)
	 */
	private boolean interpretLaTeX;

	/**
	 * Rahmen zwischen den Zellen (kann <code>null</code> sein für keinen Rahmen)
	 */
	private Color bordersInner;

	/**
	 * Rahmen um die Zellen herum (kann <code>null</code> sein für keinen Rahmen)
	 */
	private Color bordersOuter;

	/**
	 * Textfarbe
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color=DEFAULT_COLOR;

	/**
	 * Sichert an, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #cells} zugreifen.
	 */
	private final Semaphore drawLock=new Semaphore(1);

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationTable(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		useSizeOnCompare=false;
		cells=new ArrayList<>();
		cells.add(new ArrayList<>());
		cells.get(0).add(new Cell());
		interpretSymbols=true;
		interpretMarkdown=false;
		interpretLaTeX=false;
		bordersInner=Color.BLACK;
		bordersOuter=Color.BLACK;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.GENERAL_TABLE.getIcon();
	}

	@Override
	public boolean isVisualOnly() {
		return true;
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationTable.Tooltip");
	}

	/**
	 * Erstellt eine Kopie der Tabellenzellen
	 * @param source	Ausgangstabellenzellen
	 * @return	Kopie der Tabellenzellen
	 */
	private static List<List<Cell>> copyCells(final List<List<Cell>> source) {
		final List<List<Cell>> copy=new ArrayList<>();
		if (source==null || source.size()==0) {
			copy.add(new ArrayList<>());
			copy.get(0).add(new Cell());
		} else {
			final int colCount=source.stream().mapToInt(row->(row==null)?0:row.size()).max().orElse(0);
			for (var row: source) {
				final List<Cell> newRow=new ArrayList<>();
				for (int i=0;i<colCount;i++) newRow.add((row==null || row.size()<=i)?new Cell():new Cell(row.get(i)));
				copy.add(newRow);
			}
		}
		return copy;
	}

	/**
	 * Liefert die bisherigen Tabellenzellen.
	 * @return	Kopie der bisherigen Tabellenzellen
	 * @see #setCells(List)
	 */
	public List<List<Cell>> getCells() {
		return copyCells(cells);
	}

	/**
	 * Stellt neue Tabellenzellen ein.
	 * @param cells	Neue Tabellenzellen
	 * @see #getCells()
	 */
	public void setCells(final List<List<Cell>> cells) {
		this.cells=copyCells(cells);
		fireChanged();
	}

	/**
	 * Liefert die momentan eingestellte Schriftart
	 * @return	Aktuelle Schriftart
	 */
	public FontCache.FontFamily getFontFamily() {
		return fontFamily;
	}

	/**
	 * Stellt die zu verwendende Schriftart ein
	 * @param fontFamily	Neue Schriftart
	 */
	public void setFontFamily(FontCache.FontFamily fontFamily) {
		if (fontFamily!=null) this.fontFamily=fontFamily;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Größe der Schrift
	 * @return	Aktuelle Schriftgröße
	 */
	public int getTextSize() {
		return textSize;
	}

	/**
	 * Stellt die Schriftgröße ein
	 * @param textSize	Neue Schriftgröße
	 */
	public void setTextSize(final int textSize) {
		this.textSize=Math.max(6,Math.min(128,textSize));
		fireChanged();
	}

	/**
	 * Liefert die Angabe, ob der Text fett gedruckt werden soll.
	 * @return	Ausgabe des Textes im Fettdruck
	 */
	public boolean getTextBold() {
		return bold;
	}

	/**
	 * Stellt ein, ob der Text fett gedruckt werden soll.
	 * @param bold	Angabe, ob der Text fett gedruckt werden soll
	 */
	public void setTextBold(final boolean bold) {
		if (this.bold==bold) return;
		this.bold=bold;
		fireChanged();
	}

	/**
	 * Liefert die Angabe, ob der Text kursiv gedruckt werden soll.
	 * @return	Ausgabe des Textes im Kursivdruck
	 */
	public boolean getTextItalic() {
		return italic;
	}

	/**
	 * Stellt ein, ob der Text kursiv gedruckt werden soll.
	 * @param italic	Angabe, ob der Text kursiv gedruckt werden soll
	 */
	public void setTextItalic(final boolean italic) {
		if (this.italic==italic) return;
		this.italic=italic;
		fireChanged();
	}

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 * @return	HTML- und LaTeX-Symbole interpretieren
	 */
	public boolean isInterpretSymbols() {
		return interpretSymbols;
	}

	/**
	 * Stellt ein, ob HTML- und LaTeX-Symbole interpretiert werden sollen.
	 * @param interpretSymbols	HTML- und LaTeX-Symbole interpretier
	 */
	public void setInterpretSymbols(boolean interpretSymbols) {
		this.interpretSymbols=interpretSymbols;
	}

	/**
	 * Soll Markdown interpretiert werden?
	 * @return	Markdown interpretieren
	 */
	public boolean isInterpretMarkdown() {
		return interpretMarkdown;
	}

	/**
	 * Stellt ein, ob Markdown interpretiert werden soll.
	 * @param interpretMarkdown	Markdown interpretieren
	 */
	public void setInterpretMarkdown(final boolean interpretMarkdown) {
		this.interpretMarkdown=interpretMarkdown;
	}

	/**
	 * Sollen LaTeX-Formatierungen interpretiert werden?
	 * @return	LaTeX-Formatierungen interpretieren
	 */
	public boolean isInterpretLaTeX() {
		return interpretLaTeX;
	}

	/**
	 * Stellt ein, ob LaTeX-Formatierungen interpretiert werden soll.
	 * @param interpretLaTeX	LaTeX-Formatierungen interpretieren
	 */
	public void setInterpretLaTeX(final boolean interpretLaTeX) {
		this.interpretLaTeX=interpretLaTeX;
	}

	/**
	 * Liefert die aktuelle Textfarbe
	 * @return	Aktuelle Textfarbe
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Stellt die Textfarbe ein
	 * @param color	Textfarbe
	 */
	public void setColor(final Color color) {
		if (color!=null) {
			this.color=color;
			fireChanged();
		}
	}

	/**
	 * Liefert den Rahmen zwischen den Zellen (kann <code>null</code> sein für keinen Rahmen).
	 * @return	Rahmen zwischen den Zellen (kann <code>null</code> sein für keinen Rahmen)
	 * @see #setBordersInner(Color)
	 */
	public Color getBordersInner() {
		return bordersInner;
	}

	/**
	 * Stellt den Rahmen zwischen den Zellen (kann <code>null</code> sein für keinen Rahmen) ein.
	 * @param bordersInner	Rahmen zwischen den Zellen (kann <code>null</code> sein für keinen Rahmen)
	 * @see #getBordersInner()
	 */
	public void setBordersInner(final Color bordersInner) {
		if (Objects.equals(this.bordersInner,bordersInner)) return;
		this.bordersInner=bordersInner;
		fireChanged();
	}

	/**
	 * Liefert den Rahmen um die Zellen herum (kann <code>null</code> sein für keinen Rahmen).
	 * @return	Rahmen um die Zellen herum (kann <code>null</code> sein für keinen Rahmen)
	 * @see #setBordersOuter(Color)
	 */
	public Color getBordersOuter() {
		return bordersOuter;
	}

	/**
	 * Stellt den Rahmen um die Zellen herum (kann <code>null</code> sein für keinen Rahmen) ein.
	 * @param bordersOuter	Rahmen um die Zellen herum (kann <code>null</code> sein für keinen Rahmen)
	 * @see #getBordersOuter()
	 */
	public void setBordersOuter(final Color bordersOuter) {
		if (Objects.equals(this.bordersOuter,bordersOuter)) return;
		this.bordersOuter=bordersOuter;
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
		if (!(element instanceof ModelElementAnimationTable)) return false;
		final ModelElementAnimationTable otherTable=(ModelElementAnimationTable)element;

		if (cells.size()!=otherTable.cells.size()) return false;
		for (int i=0;i<cells.size();i++) {
			final var row1=cells.get(i);
			final var row2=otherTable.cells.get(i);
			if (row1.size()!=row2.size()) return false;
			for (int j=0;j<row1.size();j++) if (!row1.get(j).equalsCell(row2.get(j))) return false;
		}

		if (!otherTable.color.equals(color)) return false;
		if (otherTable.fontFamily!=fontFamily) return false;
		if (textSize!=otherTable.textSize) return false;
		if (bold!=otherTable.bold) return false;
		if (italic!=otherTable.italic) return false;
		if (interpretSymbols!=otherTable.interpretSymbols) return false;
		if (interpretMarkdown!=otherTable.interpretMarkdown) return false;
		if (interpretLaTeX!=otherTable.interpretLaTeX) return false;
		if (!Objects.equals(bordersInner,otherTable.bordersInner)) return false;
		if (!Objects.equals(bordersOuter,otherTable.bordersOuter)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationTable) {
			final ModelElementAnimationTable copySource=(ModelElementAnimationTable)element;
			cells=copyCells(copySource.cells);
			fontFamily=copySource.fontFamily;
			textSize=copySource.textSize;
			bold=copySource.bold;
			italic=copySource.italic;
			interpretSymbols=copySource.interpretSymbols;
			interpretMarkdown=copySource.interpretMarkdown;
			interpretLaTeX=copySource.interpretLaTeX;
			color=copySource.color;
			bordersInner=copySource.bordersInner;
			bordersOuter=copySource.bordersOuter;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationTable clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationTable element=new ModelElementAnimationTable(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Cache für das Array mit den Zeilenhöhen
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int[] rowHeights;

	/**
	 * Cache für das Array mit den Spaltenbreiten
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int[] colWidths;

	/**
	 * Cache für den Rahmenlinienstift
	 * @see #drawElementShape(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 * @see #strokeCacheWidth
	 */
	private BasicStroke strokeCache;

	/**
	 * Breite des gecachten Rahmenliniestifts
	 * @see #strokeCache
	 * @see #drawElementShape(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 */
	private int strokeCacheWidth;

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		drawLock.acquireUninterruptibly();
		try {
			final int rowCount=cells.size();
			final int colCount=cells.get(0).size();

			/* Größen der Felder aktualisieren */
			for (var row: cells) for (var cell: row) cell.getRenderer(fontFamily,textSize,bold,italic,interpretSymbols,interpretMarkdown,interpretLaTeX,zoom,graphics);

			/* Höhen der Zeilen */
			if (rowHeights==null || rowHeights.length!=rowCount) rowHeights=new int[rowCount];
			for (int i=0;i<rowCount;i++) rowHeights[i]=cells.get(i).stream().mapToInt(cell->cell.textRenderer.getHeight()).max().orElse(0);

			/* Breiten der Spalten */
			if (colWidths==null || colWidths.length!=colCount) colWidths=new int[colCount];
			for (int i=0;i<colWidths.length;i++) {
				final int finalI=i;
				colWidths[i]=cells.stream().mapToInt(row->row.get(finalI).textRenderer.getWidth()).max().orElse(0);
			}

			/* Rahmenbreite im aktuellen Zoomlevel */
			final int borderWidth=(int)Math.round(BORDER_WIDTH*zoom);

			/* Abstände zwischen Text und Rahmen im aktuellen Zoomlevel */
			final int cellPadding=(int)Math.round(PADDING*zoom);

			int boxW=0;
			for (var w: colWidths) boxW+=w+2*cellPadding;
			if (bordersInner!=null) boxW+=(colWidths.length-1)*borderWidth;
			if (bordersOuter!=null) boxW+=2*borderWidth;

			int boxH=0;
			for (var h: rowHeights) boxH+=h+2*cellPadding;
			if (bordersInner!=null) boxH+=(rowHeights.length-1)*borderWidth;
			if (bordersOuter!=null) boxH+=2*borderWidth;

			/* Wenn nötig Größe der Box anpassen */
			final Dimension boxSize=getSize();
			final int noZoomBoxW=(int)Math.round(boxW/zoom);
			final int noZoomBoxH=(int)Math.round(boxH/zoom);
			if (boxSize.width!=noZoomBoxW || boxSize.height!=noZoomBoxH) setSize(new Dimension(noZoomBoxW,noZoomBoxH));

			/* Texte ausgeben */
			final Point start=getPosition(true);
			final int startX=(int)Math.round(start.x*zoom);
			final int startY=(int)Math.round(start.y*zoom);
			int rowNr=0;
			int y=startY;
			if (bordersOuter!=null) y+=borderWidth;
			for (var row: cells) {
				int colNr=0;
				int x=startX;
				if (bordersOuter!=null) x+=borderWidth;
				y+=cellPadding;
				for (var cell: row) {
					x+=cellPadding;
					if (cell.backgroundColor!=null) {
						graphics.setColor(cell.backgroundColor);
						graphics.fillRect(x-cellPadding-1,y-cellPadding-1,colWidths[colNr]+2*cellPadding+2,rowHeights[rowNr]+2*cellPadding+2);
					}
					int add=0;
					switch (cell.align) {
					case LEFT: /* keine Veränderung */ break;
					case CENTER: add=(int)Math.round((colWidths[colNr]-cell.textRenderer.getWidth())/2.0); break;
					case RIGHT: add=colWidths[colNr]-cell.textRenderer.getWidth(); break;
					}
					cell.textRenderer.draw(graphics,x+add,y,(cell.textColor==null)?color:cell.textColor);
					x+=colWidths[colNr];
					x+=cellPadding;
					if (bordersInner!=null) x+=borderWidth;
					colNr++;
				}
				y+=rowHeights[rowNr];
				y+=cellPadding;
				if (bordersInner!=null) y+=borderWidth;
				rowNr++;
			}

			Stroke saveStroke=null;
			if (bordersInner!=null || bordersOuter!=null) {
				saveStroke=((Graphics2D)graphics).getStroke();
				if (strokeCache==null || strokeCacheWidth!=borderWidth) {
					strokeCache=new BasicStroke(borderWidth);
					strokeCacheWidth=borderWidth;
				}
				((Graphics2D)graphics).setStroke(strokeCache);
			}

			/* Innere Rahmenlinien zeichnen */
			if (bordersInner!=null) {
				graphics.setColor(bordersInner);
				if (colCount>1) {
					int x=startX;
					if (bordersOuter!=null) x+=borderWidth;
					for (int i=0;i<colCount-1;i++) {
						x+=cellPadding+colWidths[i]+cellPadding;
						graphics.drawLine(x,startY,x,startY+boxH);
						x+=borderWidth;
					}
				}
				if (rowCount>1) {
					y=startY;
					if (bordersOuter!=null) y+=borderWidth;
					for (int i=0;i<rowCount-1;i++) {
						y+=cellPadding+rowHeights[i]+cellPadding;
						graphics.drawLine(startX,y,startX+boxW,y);
						y+=borderWidth;
					}
				}
			}

			/* Äußere Rahmenlinien zeichnen */
			if (bordersOuter!=null) {
				graphics.setColor(bordersOuter);
				graphics.drawRect(startX,startY,boxW,boxH);
			}

			if (saveStroke!=null) ((Graphics2D)graphics).setStroke(saveStroke);

			/* Selektionsrahmen zeichnen */
			if (isSelected() && showSelectionFrames) {
				drawRect(graphics,drawRect,zoom,Color.GREEN,2,null,2);
			} else {
				if (isSelectedArea() && showSelectionFrames) {
					drawRect(graphics,drawRect,zoom,Color.BLUE,2,null,2);
				}
			}
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationTable.Name");
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
			new ModelElementAnimationTableDialog(owner,ModelElementAnimationTable.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationTableDialog(owner,ModelElementAnimationTable.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationTable.XML.Root");
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

		/* Zellen */
		for (var row: cells) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.Row"));
			node.appendChild(sub);
			for (var cell: row) {
				final Element cellSub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.Cell"));
				sub.appendChild(cellSub);
				cellSub.setTextContent(cell.text);
				if (cell.isExpression) {
					cellSub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.Cell.Mode"),cell.isExpressionIsPercent?Language.trPrimary("Surface.AnimationTable.XML.Cell.Mode.ExpressionPercent"):Language.trPrimary("Surface.AnimationTable.XML.Cell.Mode.Expression"));
				} else {
					cellSub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.Cell.Mode"),Language.trPrimary("Surface.AnimationTable.XML.Cell.Mode.Text"));
				}
				cellSub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.Cell.Align"),cell.align.getName());
				if (cell.textColor!=null) {
					cellSub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.Cell.Color"),EditModel.saveColor(cell.textColor));
				}
				if (cell.backgroundColor!=null) {
					cellSub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.Cell.BackgroundColor"),EditModel.saveColor(cell.backgroundColor));
				}
			}

		}

		/* Schriftart */
		if (fontFamily!=FontCache.defaultFamily) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.FontFamily"));
			node.appendChild(sub);
			sub.setTextContent(fontFamily.name);
		}

		/* Schriftgröße */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.FontSize"));
		node.appendChild(sub);
		sub.setTextContent(""+textSize);
		if (bold) sub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.FontSize.Bold"),"1");
		if (italic) sub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.FontSize.Italic"),"1");
		sub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.FontSize.Symbols"),interpretSymbols?"1":"0");
		if (interpretMarkdown) sub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.FontSize.Markdown"),"1");
		if (interpretLaTeX) sub.setAttribute(Language.trPrimary("Surface.AnimationTable.XML.FontSize.LaTeX"),"1");

		/* Rahmen innen */
		if (bordersInner!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.BordersInner"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(bordersInner));
		}

		/* Rahmen außen */
		if (bordersOuter!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.BordersOuter"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(bordersOuter));
		}

		/* Farbe */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationTable.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));
	}

	/**
	 * Wurde in einem früheren Aufruf von
	 * {@link #loadProperty(String, String, Element)}
	 * bereits eine Tabellenzeile geladen?
	 * @see #loadProperty(String, String, Element)
	 */
	private boolean lineLoaded=false;

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

		/* Zellen */
		if (Language.trAll("Surface.AnimationTable.XML.Row",name)) {
			if (!lineLoaded) {
				cells.clear();
				lineLoaded=true;
			}
			final List<Cell> row=new ArrayList<>();
			cells.add(row);
			final var list=node.getChildNodes();
			for (int i=0;i<list.getLength();i++) {
				final var child=list.item(i);
				if (!(child instanceof Element)) continue;
				final var sub=(Element)child;
				if (Language.trAll("Surface.AnimationTable.XML.Cell",sub.getNodeName())) {
					final Cell cell=new Cell();
					row.add(cell);
					cell.text=sub.getTextContent();
					final String mode=Language.trAllAttribute("Surface.AnimationTable.XML.Cell.Mode",sub);
					if (Language.trAll("Surface.AnimationTable.XML.Cell.Mode.Expression",mode)) cell.isExpression=true;
					if (Language.trAll("Surface.AnimationTable.XML.Cell.Mode.ExpressionPercent",mode)) {cell.isExpression=true; cell.isExpressionIsPercent=true;}
					cell.align=TextAlign.fromName(Language.trAllAttribute("Surface.AnimationTable.XML.Cell.Align",sub));

					final String textColor=Language.trAllAttribute("Surface.AnimationTable.XML.Cell.Color",sub);
					if (!textColor.isBlank()) cell.textColor=EditModel.loadColor(textColor);
					final String backgroundColor=Language.trAllAttribute("Surface.AnimationTable.XML.Cell.BackgroundColor",sub);
					if (!backgroundColor.isBlank()) cell.backgroundColor=EditModel.loadColor(backgroundColor);
				}
			}
			return null;
		}

		/* Schriftart */
		if (Language.trAll("Surface.AnimationTable.XML.FontFamily",name)) {
			fontFamily=FontCache.getFontCache().getFamilyFromName(content);
			return null;
		}

		/* Schriftgröße */
		if (Language.trAll("Surface.AnimationTable.XML.FontSize",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			textSize=I;
			bold=Language.trAllAttribute("Surface.AnimationTable.XML.FontSize.Bold",node).equals("1");
			italic=Language.trAllAttribute("Surface.AnimationTable.XML.FontSize.Italic",node).equals("1");
			interpretSymbols=!Language.trAllAttribute("Surface.AnimationTable.XML.FontSize.Symbols",node).equals("0");
			interpretMarkdown=Language.trAllAttribute("Surface.AnimationTable.XML.FontSize.Markdown",node).equals("1");
			interpretLaTeX=Language.trAllAttribute("Surface.AnimationTable.XML.FontSize.LaTeX",node).equals("1");
			return null;
		}

		/* Rahmen innen */
		if (Language.trAll("Surface.AnimationTable.XML.BordersInner",name)) {
			bordersInner=EditModel.loadColor(content);
			if (bordersInner==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		/* Rahmen außen */
		if (Language.trAll("Surface.AnimationTable.XML.BordersOuter",name)) {
			bordersOuter=EditModel.loadColor(content);
			if (bordersOuter==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		/* Farbe */
		if (Language.trAll("Surface.AnimationTable.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationTable";
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (var row: cells) for (var cell: row) {
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputText"),cell.text,newText->{cell.text=newText;});
		}
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.FontSize"),textSize,newFontSize->{if (newFontSize>0) textSize=newFontSize;});
	}

	@Override
	public void initAnimation(SimulationData simData) {
		for (var row: cells) for (var cell: row) cell.initExpression(simData);
	}

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		drawLock.acquireUninterruptibly();
		try {
			for (var row: cells) for (var cell: row) cell.updateExpressionValue(simData);
		} finally {
			drawLock.release();
		}
		return true;
	}

	/**
	 * Speichert die Daten einer einzelnen Tabellenzelle
	 */
	public static class Cell {
		/**
		 * Handelt es sich um einen Rechenausdruck (<code>true</code>) oder einen einfachen Text (<code>false</code>)
		 */
		public boolean isExpression;

		/**
		 * Anzuzeigende Text oder Rechenausdruck
		 */
		public String text;

		/**
		 * Wenn es sich um einen Rechenausdruck handelt: Soll das Ergebnis als Prozentwert dargestellt werden?
		 */
		public boolean isExpressionIsPercent;

		/**
		 * Wenn es sich um einen Rechenausdruck handelt, so wird hier während
		 * der Animation das Rechenausdruckobjekt hinterlegt.
		 * @see #initExpression(SimulationData)
		 * @see #updateExpressionValue(SimulationData)
		 */
		private ExpressionCalc calculatedExpression;

		/**
		 * Berechneter Wert des Rechenausdrucks.
		 * @see #initExpression(SimulationData)
		 * @see #updateExpressionValue(SimulationData)
		 */
		private String calculatedExpressionValue;

		/**
		 * Ausrichtung des Textes in der Zelle
		 */
		public TextAlign align;

		/**
		 * Textfarbe (kann <code>null</code> sein, dann kommt die globale Vorgabe zum tragen)
		 */
		public Color textColor;

		/**
		 * Hintergrundfarbe der Zelle (kann <code>null</code> sein, dann erhält die Zelle keine Hintergrundfarbe)
		 */
		public Color backgroundColor;

		/**
		 * Zu verwendender Text-Renderer
		 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
		 */
		private ModelElementTextRenderer textRenderer;

		/**
		 * Konstruktor
		 */
		public Cell() {
			isExpression=false;
			text=Language.tr("Surface.AnimationTable.DefaultText");
			isExpressionIsPercent=false;
			calculatedExpression=null;
			calculatedExpressionValue=null;
			align=TextAlign.LEFT;
			textColor=null;
			backgroundColor=null;
		}

		/**
		 * Copy-Konstruktor
		 * @param copySource	Ausgangszellenobjekt dessen Daten in das neue Objekt kopiert werden sollen
		 */
		public Cell(final Cell copySource) {
			this();
			if (copySource!=null) {
				isExpression=copySource.isExpression;
				text=copySource.text;
				isExpressionIsPercent=copySource.isExpressionIsPercent;
				align=copySource.align;
				textColor=copySource.textColor;
				backgroundColor=copySource.backgroundColor;
			}
		}

		/**
		 * Vergleicht die Tabellenzelle mit einem anderen Tabellenzellenobjekt
		 * @param cell	Anderes Tabellenzellenobjekt das mit diesem verglichen werden soll
		 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
		 */
		public boolean equalsCell(final Cell cell) {
			if (cell==null) return false;
			if (isExpression!=cell.isExpression) return false;
			if (!text.equals(cell.text)) return false;
			if (isExpressionIsPercent!=cell.isExpressionIsPercent) return false;
			if (align!=cell.align) return false;
			if (textColor!=cell.textColor) return false;
			if (backgroundColor!=cell.backgroundColor) return false;
			return true;
		}

		/**
		 * Initialisiert das {@link #calculatedExpression} Objekt, wenn es sich bei der Zelle um einen Rechenausdruck handelt.
		 * @param simData	Simulationsdatenobjekt
		 */
		private void initExpression(final SimulationData simData) {
			if (!isExpression) return;
			calculatedExpression=new ExpressionCalc(simData.runModel.variableNames,simData.runModel.modelUserFunctions);
			if (calculatedExpression.parse(text)>=0) calculatedExpression=null;
		}

		/**
		 * Aktualisiert den anzuzeigenden Wert für den Rechenausdruck
		 * (sofern es sich bei der Zelle um einen Rechenausdruck handelt und dieser über {@link #initExpression(SimulationData)} initialisiert werden konnte).
		 * @param simData	Simulationsdatenobjekt
		 */
		private void updateExpressionValue(final SimulationData simData) {
			calculatedExpressionValue=null;
			if (calculatedExpression==null) return;
			simData.runData.setClientVariableValues(null);
			final double value=calculatedExpression.calcOrDefault(simData.runData.variableValues,simData,null,0);
			calculatedExpressionValue=isExpressionIsPercent?NumberTools.formatPercent(value):NumberTools.formatNumber(value);
		}

		/**
		 * Konfiguriert ein Text-Renderer-Objekt und liefert es zurück.
		 * (Das Objekt wird intern gecacht und auch die Konfiguration / Größenberechnung wird nur neu ausgeführt, wenn dies notwendig ist.)
		 * @param fontFamily	Zu verwendende Schriftart
		 * @param textSize	Schriftgröße
		 * @param bold	Ausgabe des Textes im Fettdruck
		 * @param italic	Ausgabe des Textes im Kursivdruck
		 * @param interpretSymbols	Sollen HTML- und LaTeX-Symbole interpretiert werden?
		 * @param interpretMarkdown	Soll Markdown interpretiert werden?
		 * @param interpretLaTeX	Sollen LaTeX-Ausdrücke interpretiert werden?
		 * @param zoom	Zoomlevel
		 * @param graphics	Grafikobjekt in das die Ausgabe erfolgen soll
		 * @return	Text-Renderer-Objekt
		 */
		public ModelElementTextRenderer getRenderer(final FontCache.FontFamily fontFamily, final int textSize, final boolean bold, final boolean italic, final boolean interpretSymbols, final boolean interpretMarkdown, final boolean interpretLaTeX, final double zoom, final Graphics graphics) {
			/* Renderer vorbereiten */
			if ((interpretMarkdown || interpretLaTeX) && calculatedExpressionValue==null) {
				if (!(textRenderer instanceof ModelElementTextRendererMarkDownLaTeX)) textRenderer=new ModelElementTextRendererMarkDownLaTeX();
				((ModelElementTextRendererMarkDownLaTeX)textRenderer).setRenderMode(interpretMarkdown,interpretLaTeX);
			} else {
				if (!(textRenderer instanceof ModelElementTextRendererPlain)) textRenderer=new ModelElementTextRendererPlain();
			}

			/* Daten in Renderer laden */
			if (calculatedExpressionValue==null) {
				textRenderer.setText(text,interpretSymbols);
			} else {
				textRenderer.setText(calculatedExpressionValue,false);
			}
			textRenderer.setStyle(textSize,bold,italic,fontFamily.name,align);
			textRenderer.calc(graphics,zoom);

			return textRenderer;
		}
	}
}
