/**
 * Copyright 2025 Alexander Herzog
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import mathtools.Table;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAnimationBarChart;
import ui.modeleditor.elements.ModelElementAnimationBarStack;
import ui.modeleditor.elements.ModelElementAnimationDiagramBase;
import ui.modeleditor.elements.ModelElementAnimationImage;
import ui.modeleditor.elements.ModelElementAnimationLCD;
import ui.modeleditor.elements.ModelElementAnimationPieChart;
import ui.modeleditor.elements.ModelElementAnimationPointerMeasuring;
import ui.modeleditor.elements.ModelElementAnimationRecord;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementEllipse;
import ui.modeleditor.elements.ModelElementLine;
import ui.modeleditor.elements.ModelElementLink;
import ui.modeleditor.elements.ModelElementRectangle;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.elements.ModelElementVertex;
import ui.modeleditor.elements.TextTransformer;

/**
 * Ermöglicht den Export von Modellen als LaTeX-TikZ-Zeichnungen.
 */
public class TikZExport {
	/**
	 * Zeichenfläche deren Elemente exportiert werden sollen
	 */
	private final ModelSurface surface;

	/**
	 * Sollen die IDs unter den Stationsboxen angezeigt werden?
	 */
	private final boolean showIDs;

	/**
	 * Liste der Mathematik-Modus LaTeX-Befehle, die in $...$ eingekapselt werden sollen.
	 * @see #escapeText(String)
	 */
	private static final Set<String> laTeXMathCommands;

	/**
	 * Liste der auszugebenen Elemente
	 * @see #generateOutput()
	 * @see #save(File)
	 */
	private List<ExportElement> elements;

	/**
	 * Nutzerdefinierte Farbdefinitionen
	 * @see #getLaTeXColorName(Color)
	 * @see #generateOutput()
	 */
	private List<String> colorDefinitions;

	/**
	 * Zuordnung von nutzerdefinierten Farbe
	 * zu den Farbnamen
	 * @see #getLaTeXColorName(Color)
	 * @see #generateOutput()
	 */
	private final Map<Color,String> colorNames;

	/**
	 * Ausgabetext
	 * @see #generateOutput()
	 * @see #save(File)
	 */
	private List<String> output;

	static {
		/* Liste der LaTeX-Mathematik-Befehle zusammenstellen */
		laTeXMathCommands=new HashSet<>();
		for (var map: TextTransformer.entitiesLaTeX.values()) laTeXMathCommands.addAll(map.keySet());
	}

	/**
	 * Konstruktor
	 * @param surface	Zeichenfläche deren Elemente exportiert werden sollen
	 * @param showIDs	Sollen die IDs unter den Stationsboxen angezeigt werden?
	 */
	public TikZExport(final ModelSurface surface, final boolean showIDs) {
		this.surface=surface;
		this.showIDs=showIDs;
		colorNames=new HashMap<>();
	}

	/**
	 * Ermittelt einen LaTeX-Farbnamen für eine Farbe.
	 * @param color	Farbe
	 * @return	LaTeX-Farbname
	 * @see #colorDefinitions
	 * @see #colorNames
	 */
	private String getLaTeXColorName(final Color color) {
		if (color.equals(Color.BLACK)) return "black";
		if (color.equals(Color.WHITE)) return "white";
		if (color.equals(Color.YELLOW)) return "yellow";
		if (color.equals(Color.RED)) return "red";
		if (color.equals(Color.GREEN)) return "green";
		if (color.equals(Color.BLUE)) return "blue";
		if (color.equals(Color.GRAY)) return "gray";
		if (color.equals(Color.CYAN)) return "cyan";
		if (color.equals(Color.MAGENTA)) return "magenta";
		if (color.equals(Color.ORANGE)) return "orange";
		if (color.equals(Color.PINK)) return "pink";
		if (color.equals(Color.LIGHT_GRAY)) return "lightgray";
		if (color.equals(Color.DARK_GRAY)) return "darkgray";

		if (colorDefinitions==null) colorDefinitions=new ArrayList<>();

		final String colorName=colorNames.get(color);
		if (colorName!=null) return colorName;

		final String newColorName="qsColor"+(colorNames.size()+1);
		colorNames.put(color,newColorName);
		final double r=Math.round((color.getRed()/255.0)*100.0)/100.0;
		final double g=Math.round((color.getGreen()/255.0)*100.0)/100.0;
		final double b=Math.round((color.getBlue()/255.0)*100.0)/100.0;
		colorDefinitions.add("\\definecolor{"+newColorName+"}{rgb}{"+r+","+g+","+b+"}");
		return newColorName;
	}

	/**
	 * Stellt die für den Export notwendigen Daten zusammen.
	 * @see #elements
	 * @see #save(File)
	 */
	private void collectData() {
		elements=new ArrayList<>();

		for (var element: surface.getElements()) {
			if (element instanceof ModelElementBox) elements.add(new ExportBox((ModelElementBox)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementEdge) elements.add(new ExportEdge((ModelElementEdge)element));
			if (element instanceof ModelElementVertex) elements.add(new ExportVertex((ModelElementVertex)element));
			if (element instanceof ModelElementText) elements.add(new ExportText((ModelElementText)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementLink) elements.add(new ExportText((ModelElementLink)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementLine) elements.add(new ExportLine((ModelElementLine)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementRectangle) elements.add(new ExportRectangle((ModelElementRectangle)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationBarChart) elements.add(new ExportRectangle((ModelElementAnimationBarChart)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationBarStack) elements.add(new ExportRectangle((ModelElementAnimationBarStack)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationDiagramBase) elements.add(new ExportRectangle((ModelElementAnimationDiagramBase)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationImage) elements.add(new ExportRectangle((ModelElementAnimationImage)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationLCD) elements.add(new ExportRectangle((ModelElementAnimationLCD)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationPieChart) elements.add(new ExportRectangle((ModelElementAnimationPieChart)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationPointerMeasuring) elements.add(new ExportRectangle((ModelElementAnimationPointerMeasuring)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementAnimationRecord) elements.add(new ExportRectangle((ModelElementAnimationRecord)element,color->getLaTeXColorName(color)));
			if (element instanceof ModelElementEllipse) elements.add(new ExportEllipse((ModelElementEllipse)element,color->getLaTeXColorName(color)));
		}
	}

	/**
	 * Erzeugt die tatsächliche Ausgabe auf Basis der
	 * Liste der exportierenden Daten.
	 * @see #elements
	 * @see #save(File)
	 */
	private void generateOutput() {
		output=new ArrayList<>();

		final boolean hasSmallBox=elements.stream().filter(e->e instanceof ExportBox).map(e->(ExportBox)e).filter(e->e.smallBox).findFirst().isPresent();
		final boolean hasEdge=elements.stream().filter(e->e instanceof ExportEdge).findFirst().isPresent();
		final boolean hasVertex=elements.stream().filter(e->e instanceof ExportVertex).findFirst().isPresent();
		final boolean hasText=elements.stream().filter(e->e instanceof ExportText).findFirst().isPresent();
		final boolean hasDecoration=elements.stream().filter(e->e.type==ExportElementType.DECORATION).findFirst().isPresent();

		/* Header */
		output.add("\\documentclass[a4paper]{article}");
		output.add("");
		output.add("\\usepackage[utf8]{inputenc}");
		output.add("\\usepackage[T1]{fontenc}");
		output.add("\\usepackage{tikz}");
		output.add("\\renewcommand{\\familydefault}{\\sfdefault}");
		output.add("");
		output.add("\\begin{document}");
		output.add("");

		/* Style-Konfiguration */
		output.add("\\tikzset{");
		output.add("  qsModel/.style={x=0.02cm,y=-0.02cm, every node/.style={transform shape}},");
		output.add("  qsStation/.style 2 args={rectangle,draw,below right,align=center,minimum height=1cm,minimum width=2cm,left color=#1!40,right color=#1!10,label=below:{\\color{gray}\\scriptsize #2}}"+((hasSmallBox|hasEdge||hasVertex||hasText)?",":""));
		if (hasSmallBox) output.add("  qsStationSmall/.style 2 args={rectangle,draw,below right,align=center,minimum height=0.5cm,minimum width=0.5cm,left color=#1!40,right color=#1!10,label=below:{\\color{gray}\\scriptsize #2}}"+((hasEdge||hasVertex||hasText)?",":""));
		if (hasEdge) output.add("  qsEdge/.style={->,auto,thick}"+((hasVertex||hasText)?",":""));
		if (hasVertex) output.add("  qsVertex/.style={rectangle,draw=black,fill=black!20}"+(hasText?",":""));
		if (hasText) output.add("  qsText/.style={below right}");
		output.add("}");
		output.add("");

		/* Farbdefinitionen */
		if (colorDefinitions!=null) {
			output.addAll(colorDefinitions);
			output.add("");
		}

		/* Größe berechnen */
		final var minP=new Point(1_000_000,1_000_000);
		final var maxP=new Point(-1_000_000,-1_000_000);
		for (var element: elements) {
			final var p1=element.getUpperLeft();
			final var p2=element.getLowerRight();
			if (p1!=null) {
				minP.x=Math.min(minP.x,p1.x);
				minP.y=Math.min(minP.y,p1.y);
			}
			if (p2!=null) {
				maxP.x=Math.max(maxP.x,p2.x);
				maxP.y=Math.max(maxP.y,p2.y);
			}
		}
		final double widthCM=Math.max(maxP.x-minP.x,1)/50.0;
		final double scale=Math.round(Math.min(1.0,14.0/widthCM)*100.0)/100.0;

		/* Zeichnung */
		output.add("\\begin{tikzpicture}[qsModel,scale="+scale+"]");
		output.add("");

		elements.stream().filter(e->e.type==ExportElementType.BOX).forEach(e->e.draw(output,color->getLaTeXColorName(color),showIDs));
		if (hasEdge) {
			output.add("");
			elements.stream().filter(e->e.type==ExportElementType.EDGE).forEach(e->e.draw(output,color->getLaTeXColorName(color),showIDs));
		}

		if (hasDecoration) {
			output.add("");
			elements.stream().filter(e->e.type==ExportElementType.DECORATION).forEach(e->e.draw(output,color->getLaTeXColorName(color),showIDs));
		}

		output.add("");
		output.add("\\end{tikzpicture}");
		output.add("");

		/* Footer */
		output.add("\\end{document}");
	}

	/**
	 * Erstellt (wenn noch nicht geschehen) den Ausgabetext
	 * und speichert diesen in der angegebenen Datei.
	 * @param file	Zieldatei
	 * @return	Liefert <code>true</code>, wenn der Export erfolgreich war.
	 */
	public boolean save(final File file) {
		if (output==null) {
			collectData();
			generateOutput();
		}

		final String outputString=String.join("\n",output);
		return Table.saveTextToFile(outputString,file);
	}

	/**
	 * Interpretiert Fett- und Kursiv-Markdown-Befehle und wandelt
	 * diese in LaTeX-Anweisungen um.
	 * @param text	Zu interpretierender Text
	 * @param markdownSymbol	Zu findenden Markdown-Auszeichnung
	 * @param laTeXSymbol	Einzufügendes LaTeX-Symbol
	 * @return	Veränderter Text
	 */
	private static String processMarkdown(String text, final String markdownSymbol, final String laTeXSymbol) {
		while (true) {
			final int index1=text.indexOf(markdownSymbol);
			if (index1<0) return text;
			final int index2=text.indexOf(markdownSymbol,index1+markdownSymbol.length());
			if (index2<0) return text;

			final StringBuilder newText=new StringBuilder();
			newText.append(text.substring(0,index1));
			newText.append(laTeXSymbol);
			newText.append("{");
			newText.append(text.substring(index1+markdownSymbol.length(),index2));
			newText.append("}");
			newText.append(text.substring(index2+markdownSymbol.length()));
			text=newText.toString();
		}

	}

	/**
	 * Filtert Zeichen, die in LaTeX eine besondere Bedeutung haben, aus.
	 * @param text	Zu prüfender Text
	 * @return	Text für die Ausgabe im TikZ-Code
	 */
	private static String escapeText(String text) {
		text=text.trim();

		/* Symbole */
		final StringBuilder newText=new StringBuilder();

		while (text.length()>0) {
			final int index=text.indexOf("\\");
			if (index<0) {
				newText.append(text);
				break;
			}

			newText.append(text.substring(0,index));
			text=text.substring(index+1);

			String symbol=null;
			for (var sym: laTeXMathCommands) if (text.startsWith(sym)) {symbol=sym; break;}

			if (symbol==null) {
				newText.append("\\\\");
			} else {
				newText.append("$\\");
				newText.append(symbol);
				newText.append("$");
				text=text.substring(symbol.length());
			}
		}
		text=newText.toString();


		/* LaTeX-Symbole filtern */
		text=text.replace("^","\\^");
		text=text.replace("_","\\_");
		text=text.replace("{","\\{");
		text=text.replace("}","\\}");
		text=text.replace("&","\\&");
		text=text.replace("%","\\%");

		/* Markdown */
		text=processMarkdown(text,"**","\\textbf");
		text=processMarkdown(text,"__","\\textbf");
		text=processMarkdown(text,"*","\\textit");
		text=processMarkdown(text,"_","\\textit");

		return text;
	}

	/**
	 * Übersetzt Schriftgrößen in pt in ungefähre LaTeX-Bezeichner.
	 * @param size	Schriftgröße
	 * @return	Zugehöriger LaTeX-Bezeichner
	 */
	private static String getFontSizeName(final int size) {
		if (size>=24) return "\\Huge";
		if (size>=22) return "\\huge";
		if (size>=20) return "\\LARGE";
		if (size>=18) return "\\Large";
		if (size>=16) return "\\Large";
		if (size<=6) return "\\tiny";
		if (size<=8) return "\\scriptsize";
		if (size<=10) return "\\footnotesize";
		if (size<=12) return "\\small";
		return "";
	}

	/**
	 * Art des Elements
	 */
	private enum ExportElementType {
		/** Station oder Verbindungsecke */
		BOX,
		/** Kante zwischen zwei Stationen bzw. Verbindungsecken */
		EDGE,
		/** Dekoratives Element */
		DECORATION
	}

	/**
	 * Basisklasse für die Daten für alle zu exportierenden Elemente.
	 */
	private static abstract class ExportElement {
		/**
		 * Art des Elements
		 */
		public final ExportElementType type;

		/**
		 * ID des Elements
		 */
		protected final int id;


		/**
		 * Konstruktor
		 * @param type	Art des Elements
		 * @param element	ID des Elements
		 */
		public ExportElement(final ExportElementType type, final ModelElement element) {
			this.type=type;
			this.id=element.getId();
		}

		/**
		 * Generiert die TikZ-Ausgabe für das Element.
		 * @param output	Liste der TikZ-Ausgabezeilen
		 * @param getColorName	Ermittelt auf Basis einer Farbe den zugehörigen LaTeX-Farbnamen
		 * @param showIDs	Sollen die IDs unter den Stationsboxen angezeigt werden?
		 */
		public abstract void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs);

		/**
		 * Liefert die obere linke Ecke des Elements.
		 * @return	Obere linke Ecke des Elements (kann auch <code>null</code> sein, wenn keine Größe verfügbar ist)
		 */
		public Point getUpperLeft() {
			return null;
		}

		/**
		 * Liefert die untere rechte Ecke des Elements.
		 * @return	Untere rechte Ecke des Elements (kann auch <code>null</code> sein, wenn keine Größe verfügbar ist)
		 */
		public Point getLowerRight() {
			return null;
		}
	}

	/**
	 * Datenobjekt für eine Station
	 */
	private static class ExportBox extends ExportElement {
		/** x-Position der linken oberen Ecke */
		private final int x;
		/** y-Position der linken oberen Ecke */
		private final int y;
		/** Hintergrundfarbe */
		private final Color color;
		/** Typ der Station */
		private final String textType;
		/** Optionaler, zusätzlich in der Box anzuzeigender Text */
		private final String textName;
		/** Handelt es sich um eine kleine Box, d.h. um eine Box, deren Größe von der Standard-Stationsgröße nach unten abweicht? */
		public final boolean smallBox;

		/**
		 * Konstruktor
		 * @param box	Zugehöriges Stations-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportBox(final ModelElementBox box, final Consumer<Color> registerColor) {
			super(ExportElementType.BOX,box);
			x=box.getPosition(true).x;
			y=box.getPosition(true).y;
			color=box.getDrawBackgroundColor();
			registerColor.accept(color);
			textType=box.getTypeName();
			textName=box.getName();
			smallBox=(box.getSize().width<100);
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			final StringBuilder text=new StringBuilder();
			text.append(textType);
			if (textName!=null && !textName.isBlank()) {
				text.append("\\\\\\textbf{");
				text.append(escapeText(textName));
				text.append("}");
			}
			if (smallBox) {
				output.add("  \\node [qsStationSmall={"+getColorName.apply(color)+"}{"+(showIDs?("id="+id):"")+"}] (id"+id+") at ("+x+","+y+") {};");
			} else {
				output.add("  \\node [qsStation={"+getColorName.apply(color)+"}{"+(showIDs?("id="+id):"")+"}] (id"+id+") at ("+x+","+y+") {"+text.toString()+"};");
			}
		}

		@Override
		public Point getUpperLeft() {
			return new Point(x,y);
		}

		@Override
		public Point getLowerRight() {
			if (smallBox) {
				return new Point(x+30,y+30);
			} else {
				return new Point(x+100,y+50);
			}
		}
	}

	/**
	 * Datenobjekt für eine Verbindungskante
	 */
	private static class ExportEdge extends ExportElement {
		/** ID des Elements, das den Startpunkt der Kante darstellt */
		private final int id1;
		/** ID des Elements, das den Zielpunkt der Kante darstellt */
		private final int id2;
		/** Optionaler, zusätzlich an der Kante anzuzeigender Text */
		private final String text;

		/**
		 * Konstruktor
		 * @param edge	Zugehöriges Verbindungskanten-Objekt aus dem Modell
		 */
		public ExportEdge(final ModelElementEdge edge) {
			super(ExportElementType.EDGE,edge);
			id1=edge.getConnectionStart().getId();
			id2=edge.getConnectionEnd().getId();
			text=edge.getName();
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			if (text==null || text.isBlank()) {
				output.add("  \\draw [qsEdge] (id"+id1+") to (id"+id2+");");
			} else {
				output.add("  \\draw [qsEdge] (id"+id1+") to node {\\scriptsize "+escapeText(text)+"} (id"+id2+");");
			}
		}
	}

	/**
	 * Datenobjekt für eine Verbindungsecke
	 */
	private static class ExportVertex extends ExportElement {
		/** x-Position der linken oberen Ecke */
		private final int x;
		/** y-Position der linken oberen Ecke */
		private final int y;

		/**
		 * Konstruktor
		 * @param vertex	Zugehöriges Verbindungsecken-Objekt aus dem Modell
		 */
		public ExportVertex(final ModelElementVertex vertex) {
			super(ExportElementType.BOX,vertex);
			x=vertex.getPosition(true).x;
			y=vertex.getPosition(true).y;
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			output.add("  \\node [qsVertex] (id"+id+") at ("+x+","+y+") {};");
		}

		@Override
		public Point getUpperLeft() {
			return new Point(x,y);
		}

		@Override
		public Point getLowerRight() {
			return new Point(x+10,y+10);
		}
	}

	/**
	 * Datenobjekt für ein Text-Objekt
	 */
	private static class ExportText extends ExportElement {
		/** x-Position der linken oberen Ecke */
		private final int x;
		/** y-Position der linken oberen Ecke */
		private final int y;
		/** Text fett ausgeben */
		private final boolean bold;
		/** Text kursiv ausgeben */
		private final boolean italic;
		/** Schriftgröße */
		private final int size;
		/** Auszugebender Text */
		private final String text;
		/** Textfarbe */
		private final Color color;

		/**
		 * Konstruktor
		 * @param text	Zugehöriges Text-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportText(final ModelElementText text, final Consumer<Color> registerColor) {
			super(ExportElementType.DECORATION,text);
			this.x=text.getPosition(true).x;
			this.y=text.getPosition(true).y;
			bold=text.getTextBold();
			italic=text.getTextItalic();
			size=text.getTextSize();
			this.text=text.getText();
			this.color=text.getColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param link	Zugehöriges Link-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportText(final ModelElementLink link, final Consumer<Color> registerColor) {
			super(ExportElementType.DECORATION,link);
			this.x=link.getPosition(true).x;
			this.y=link.getPosition(true).y;
			bold=false;
			italic=false;
			size=link.getTextSize();
			this.text=link.getText();
			this.color=Color.BLUE;
			registerColor.accept(color);
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			int currentY=y;
			for (String line: text.split("\\n")) {
				final StringBuilder text=new StringBuilder();

				int hLevel=0;
				if (line.startsWith("###")) {hLevel=3; if (line.length()==3) line=""; else line=line.substring(3).trim();}
				if (line.startsWith("##")) {hLevel=2; if (line.length()==2) line=""; else line=line.substring(2).trim();}
				if (line.startsWith("#")) {hLevel=1; if (line.length()==1) line=""; else line=line.substring(1).trim();}
				final int sizeThisLine=size+hLevel;
				final String sizeName=getFontSizeName(sizeThisLine);
				text.append(sizeName+(sizeName.isBlank()?"":" "));
				if (bold) text.append("\\textbf{");
				if (italic) text.append("\\textit{");

				text.append(escapeText(line));

				if (bold) text.append("}");
				if (italic) text.append("}");

				output.add("  \\node [qsText,color="+getColorName.apply(color)+"] at ("+x+","+currentY+") {"+text.toString()+"};");
				currentY+=Math.round(sizeThisLine/72.0*2.54*50);
			}
		}

		@Override
		public Point getUpperLeft() {
			return new Point(x,y);
		}

		@Override
		public Point getLowerRight() {
			final int lines=text.split("\\n").length;
			final int lineHeightInUnits=(int)Math.round(size/72.0*2.54*50);
			final int maxLineLength=Stream.of(text.split("\\n")).map(s->s.length()).max(Math::max).orElseGet(()->0);
			return new Point(x+maxLineLength*lineHeightInUnits,y+lines*lineHeightInUnits);
		}
	}

	/**
	 * Basisklasse für die dekorativen Datenelemente
	 */
	private static abstract class ExportPositionElement extends ExportElement {
		/** x-Position der linken oberen Ecke */
		protected final int x;
		/** y-Position der linken oberen Ecke */
		protected final int y;
		/** Breite */
		protected final int width;
		/** Höhe */
		protected final int height;

		/**
		 * Konstruktor
		 * @param element	Zugehöriges Linien-Objekt aus dem Modell
		 */
		public ExportPositionElement(final ModelElementPosition element) {
			super(ExportElementType.DECORATION,element);
			final Point p=element.getPosition(false);
			final Dimension d=element.getSize();
			if (d.width<0) {p.x-=d.width; d.width=-d.width;}
			if (d.height<0) {p.y-=d.height; d.height=-d.height;}
			x=p.x;
			y=p.y;
			width=d.width;
			height=d.height;
		}

		@Override
		public Point getUpperLeft() {
			return new Point(x,y);
		}

		@Override
		public Point getLowerRight() {
			return new Point(x+width,y+height);
		}
	}

	/**
	 * Datenobjekt für eine Linie (als dekoratives Element)
	 */
	private static class ExportLine extends ExportPositionElement {
		/** Rahmenfarbe */
		private final Color color;

		/**
		 * Konstruktor
		 * @param line	Zugehöriges Linien-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportLine(final ModelElementLine line, final Consumer<Color> registerColor) {
			super(line);
			color=line.getColor();
			registerColor.accept(color);
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			output.add("  \\draw [color="+getColorName.apply(color)+"] ("+x+","+y+") -- +("+width+","+height+");");
		}
	}

	/**
	 * Datenobjekt für ein Rechteck (als dekoratives Element)
	 */
	private static class ExportRectangle extends ExportPositionElement {
		/** Rahmenfarbe */
		private final Color color;

		/**
		 * Konstruktor
		 * @param rectangle	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementRectangle rectangle, final Consumer<Color> registerColor) {
			super(rectangle);
			color=rectangle.getColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationBarChart animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=animationElement.getBorderColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationBarStack animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=animationElement.getBorderColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationDiagramBase animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=animationElement.getBorderColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationImage animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=animationElement.getBorderColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationLCD animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=Color.BLACK;
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationPieChart animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=animationElement.getBorderColor();
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationPointerMeasuring animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=Color.BLACK;
			registerColor.accept(color);
		}

		/**
		 * Konstruktor
		 * @param animationElement	Zugehöriges Rechteck-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportRectangle(final ModelElementAnimationRecord animationElement, final Consumer<Color> registerColor) {
			super(animationElement);
			color=animationElement.getBorderColor();
			registerColor.accept(color);
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			output.add("  \\draw [color="+getColorName.apply(color)+"] ("+x+","+y+") rectangle +("+width+","+height+");");
		}
	}

	/**
	 * Datenobjekt für eine Ellipse (als dekoratives Element)
	 */
	private static class ExportEllipse extends ExportPositionElement {
		/** Rahmenfarbe */
		private final Color color;

		/**
		 * Konstruktor
		 * @param ellipse	Zugehöriges Ellipsen-Objekt aus dem Modell
		 * @param registerColor	Callback zur Registrierung von in der Ausgabe zu verwendenden Farben
		 */
		public ExportEllipse(final ModelElementEllipse ellipse, final Consumer<Color> registerColor) {
			super(ellipse);
			color=ellipse.getColor();
			registerColor.accept(color);
		}

		@Override
		public void draw(final List<String> output, final Function<Color,String> getColorName, final boolean showIDs) {
			final int rx=(int)Math.round(width/2.0);
			final int ry=(int)Math.round(height/2.0);
			output.add("  \\draw [color="+getColorName.apply(color)+",x radius="+rx+",y radius="+ry+"] ("+(x+rx)+","+(y+ry)+") ellipse;");
		}
	}
}
