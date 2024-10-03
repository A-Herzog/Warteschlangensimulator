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
package tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLTools;

/**
 * Basisklasse zur Erstellung von
 * <a href="https://drawio-app.com/">https://drawio-app.com/</a>-Dateien.
 * @author Alexander Herzog
 */
public class DrawIO {
	/** Ausgabe-xml-Objekt */
	private final XMLTools xml;
	/** Ausgabe-xml-Dokument */
	protected final Document doc;
	/** Repräsentation des "mxGraphModel"-xml-Elements in der Ausgabedatei */
	private final Element graphModel;
	/** Wurzelelement des Ausgabe-xml-Dokuments */
	protected final Element root;
	/** Basis-ID für die Ausgabeelemente */
	protected final String baseId;
	/** Fortlaufende ID der Ausgabeelemente */
	private int drawIds;

	/**
	 * Konstruktor der Klasse
	 * @param file	Datei in die die Ausgabe erfolgen soll
	 */
	public DrawIO(final File file) {
		baseId=generateID(13)+"-"+generateID(6);
		drawIds=1;

		xml=new XMLTools(file);

		final Element root=xml.generateRoot("mxfile");
		root.setAttribute("compressed","false");
		root.setAttribute("type","device");
		doc=root.getOwnerDocument();

		final Element diagram=doc.createElement("diagram");
		root.appendChild(diagram);
		diagram.setAttribute("id",generateID(20));

		diagram.appendChild(graphModel=doc.createElement("mxGraphModel"));
		graphModel.setAttribute("grid","1");
		graphModel.setAttribute("gridSize","10");
		graphModel.setAttribute("guides","1");
		graphModel.setAttribute("tooltips","1");
		graphModel.setAttribute("connect","1");
		graphModel.setAttribute("arrows","1");
		graphModel.setAttribute("fold","1");
		graphModel.setAttribute("page","1");
		graphModel.setAttribute("pageScale","1");
		graphModel.setAttribute("math","0");
		graphModel.setAttribute("shadow","0");

		graphModel.appendChild(this.root=doc.createElement("root"));

		Element cell;
		this.root.appendChild(cell=doc.createElement("mxCell"));
		cell.setAttribute("id","0");
		this.root.appendChild(cell=doc.createElement("mxCell"));
		cell.setAttribute("id","1");
		cell.setAttribute("parent","0");
	}

	/**
	 * Gültige Zeichen für eine ID
	 * @see #generateID(int)
	 */
	private static final String idChars="01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * Erzeugt eine ID einer vorgegebenen Länge
	 * @param len	Länge der ID
	 * @return	ID
	 * @see #baseId
	 */
	private String generateID(final int len) {
		final StringBuilder id=new StringBuilder();
		for (int i=0;i<len;i++) id.append(idChars.charAt((int)Math.floor(Math.random()*idChars.length())));
		return id.toString();
	}

	/**
	 * Erstellt ein neues Element und trägt es in das Dokument ein.
	 * @return	Neues Element
	 */
	protected Element addCell() {
		final Element node=doc.createElement("mxCell");

		final String id=baseId+"-"+drawIds;
		drawIds++;

		node.setAttribute("id",id);
		node.setAttribute("parent","1");
		root.appendChild(node);

		return node;
	}

	/**
	 * Erstellt ein neues kastenförmiges Element und trägt es in das Dokument ein.
	 * @param point	Position der linken oberen Ecke
	 * @param dimension	Größe des Kastens
	 * @return	Neues Element
	 */
	protected Element addBox(final Point point, final Dimension dimension) {
		final Element node=addCell();
		node.setAttribute("vertex","1");

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+point.x);
		geometry.setAttribute("y",""+point.y);
		geometry.setAttribute("width",""+dimension.width);
		geometry.setAttribute("height",""+dimension.height);

		return node;
	}

	/**
	 * Erstellt eine neue Verbindungskante und trägt diese in das Dokument ein.
	 * @param id1	ID des Ausgangselements
	 * @param id2	ID des Zielelements
	 * @param style	Aussehen der Kante
	 * @param text	Optionaler Text auf der Kante (kann <code>null</code> oder leer sein)
	 */
	protected void addEdge(final String id1, final String id2, final Map<String,String> style, final String text) {
		final Element node=addCell();
		node.setAttribute("edge","1");
		node.setAttribute("style",styleToString(style));
		node.setAttribute("source",id1);
		node.setAttribute("target",id2);
		if (text!=null && !text.isEmpty()) node.setAttribute("value",encodeHTML(text));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("relative","1");
	}

	/**
	 * Erstellt eine neue Verbindungskante und trägt diese in das Dokument ein.
	 * @param node1	Ausgangselement
	 * @param node2	Zielelement
	 * @param style	Aussehen der Kante
	 * @param text	Optionaler Text auf der Kante (kann <code>null</code> oder leer sein)
	 */
	protected void addEdge(final Element node1, final Element node2, final Map<String,String> style, final String text) {
		addEdge(node1.getAttribute("id"),node2.getAttribute("id"),style,text);
	}

	/**
	 * Speichert das Diagramm in der im Konstruktor angegebenen Datei.
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public boolean save() {
		return xml.save(doc.getDocumentElement(),true);
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" und auch Umlaute usw.
	 * in ihre entsprechenden HTML-Entitäten um. Außerdem werden Zeilenumbrüche
	 * durch &lt;br&gt; umgewandelt.
	 * @param text	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	protected static String encodeHTML(String text) {
		text=text.trim();
		final StringBuilder result=new StringBuilder();
		for (int i=0;i<text.length();i++) {
			final char c=text.charAt(i);
			if (c=='ä') {result.append("&auml;"); continue;}
			if (c=='ö') {result.append("&ouml;"); continue;}
			if (c=='ü') {result.append("&uuml;"); continue;}
			if (c=='ß') {result.append("&szlig;"); continue;}
			if (c=='Ä') {result.append("&Auml;"); continue;}
			if (c=='Ö') {result.append("&Ouml;"); continue;}
			if (c=='Ü') {result.append("&Uuml;"); continue;}
			if (c=='&') {result.append("&amp;"); continue;}
			if (c=='<') {result.append("&lt;"); continue;}
			if (c=='>') {result.append("&gt;"); continue;}
			if (c=='\\') {result.append("\\\\"); continue;}
			if (c=='\n') {result.append("<br>"); continue;}
			result.append(c);
		}
		return result.toString();
	}

	/**
	 * Lieft einen CSS-Farbcode für eine Farbe
	 * @param color	Umzuwandelnde Farbe
	 * @return	CSS-Farbcode
	 */
	protected static String getColor(final Color color) {
		return String.format("#%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue());
	}

	/**
	 * Wandelt eine Stil-Daten-Zuordnung in eine Zeichenkette um
	 * @param map	Stil-Daten-Zuordnung
	 * @return	Zeichenkette mit den Stil-Daten
	 */
	protected static String styleToString(final Map<String,String> map) {
		final StringBuilder style=new StringBuilder();
		for (Map.Entry<String,String> entry: map.entrySet()) {
			if (style.length()>0) style.append(';');
			style.append(entry.getKey());
			if (entry.getValue()!=null) {
				style.append('=');
				style.append(entry.getValue());
			}
		}
		return style.toString();
	}
}
