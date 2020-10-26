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
package ui.modeleditor.outputbuilder;

import java.awt.Color;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import simulator.editmodel.EditModel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Exportiert ein Modell als html-Datei
 * @author Alexander Herzog
 * @see SpecialOutputBuilder
 */
public class HTMLOutputBuilder extends SpecialOutputBuilder {
	/**
	 * Ausgabemodus
	 * @author Alexander Herzog
	 */
	public enum Mode {
		/** Normale Verarbeitung */
		FULL,
		/** Normale Verarbeitung, aber Modell-XML-Datei nicht in base64-Form einbetten */
		NOXML,
		/** Nur den Javascript-Code für das Modell ausgeben (für Ausgabe über den Fernsteuerungsserver) */
		JSONLY
	}

	/** Ausgabemodus */
	private final Mode mode;
	/** Größe der Zeichenfläche */
	private final Point canvasSize;
	/** JS-Hilfsfunktionen, die vor dem JS-Hauptbereich ausgegeben werden sollen */
	private final Map<String,String> userJSFunctions;

	/**
	 * Konstruktor der Klasse
	 * @param model	Zu exportierendes Modell
	 * @param mode	Ausgabemodus
	 * @see Mode
	 */
	public HTMLOutputBuilder(final EditModel model, final Mode mode) {
		super(model);
		this.mode=mode;

		canvasSize=new Point(0,0);
		for (ModelElement element: model.surface.getElements()) {
			Point p=element.getLowerRightPosition();
			if (p==null) p=element.getPosition(true);
			if (p==null) continue;
			canvasSize.x=Math.max(canvasSize.x,p.x);
			canvasSize.y=Math.max(canvasSize.y,p.y);
		}
		canvasSize.x+=25;
		canvasSize.y+=25;

		userJSFunctions=new HashMap<>();
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Zu exportierendes Modell
	 */
	public HTMLOutputBuilder(final EditModel model) {
		this(model,Mode.FULL);
	}

	/**
	 * Wandelt ein Farb-Objekt in einen HTML-Farbbezeichner (inkl. führendem "#") um.
	 * @param color	Umzuwandelnde Farbe
	 * @return	HTML-Farbbezeichner
	 */
	public static String colorToHTML(final Color color) {
		if (color==null) return "#000000";
		return String.format("#%02X%02X%02X",color.getRed(),color.getGreen(),color.getBlue());
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param text	Umzuwandelnder Text
	 * @param extended	Ersetzt außerdem doppelte Anführungszeichen durch \"
	 * @return	Umgewandelter Text
	 */
	public static String encodeHTML(String text, final boolean extended) {
		if (extended) text=text.replace("\"","\\\"");
		text=text.replace("&","&amp;");
		text=text.replace("<","&lt;");
		text=text.replace(">","&gt;");
		text=new String(text.getBytes(StandardCharsets.UTF_8));
		return text;
	}

	/**
	 * Escaped doppelte Anführungszeichen
	 * @param text	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	public static String encodeTextFieldHTML(String text) {
		text=text.replace("\"","\\\"");
		text=new String(text.getBytes(StandardCharsets.UTF_8));
		return text;
	}

	/**
	 * Datenkopf der Textausgabe
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	@Override
	protected String processHead() {
		if (mode!=Mode.JSONLY) {
			outputHead.append("<!doctype html>\n");
			outputHead.append("<html>\n");
			outputHead.append("<head>\n");
			outputHead.append("  <meta charset=\"utf-8\">\n");
			outputHead.append("  <title>"+encodeHTML(model.name,true)+"</title>\n");
			outputHead.append("  <meta name=\"description\" content=\""+encodeHTML(model.description,true).replaceAll("\\n"," ")+"\">\n");
			outputHead.append("  <style type=\"text/css\">\n");
			outputHead.append("    body {margin: 0; padding: 0; background-color: "+colorToHTML(ModelSurface.DEFAULT_BACKGROUND_COLOR)+";}\n");
			outputHead.append("    canvas {margin: 0; padding: 0; background-color: "+colorToHTML(ModelSurface.DEFAULT_BACKGROUND_COLOR)+";}\n");
			outputHead.append("  </style>\n");
			outputHead.append("</head>\n");
			outputHead.append("<body>\n");
			outputHead.append("<div id=\"surface_box\" style=\"position: relative;\">\n");
			outputHead.append("<canvas id=\"surface\">\n");
			outputHead.append("</canvas>\n");
			outputHead.append("</div>\n");
			outputHead.append("<script type=\"text/javascript\">\n");
			outputHead.append("<!--\n");
		}
		outputHead.append("var surface;\n");
		outputHead.append("var context;\n");
		outputHead.append("\n");
		outputHead.append("function initDrawElements() {\n");
		outputHead.append("  surface=document.getElementById(\"surface\");\n");
		outputHead.append("  surface.width="+canvasSize.x+";\n");
		outputHead.append("  surface.height="+canvasSize.y+";\n");
		outputHead.append("  context=surface.getContext(\"2d\");\n");
		outputHead.append("}\n");
		return null;
	}

	/**
	 * Abschluss der Textausgabe
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	@Override
	protected String processFoot() {
		if (userJSFunctions.size()>0) outputHead.append("\n");
		for (Map.Entry<String,String> entry: userJSFunctions.entrySet()) {
			outputHead.append(entry.getValue());
			outputHead.append("\n");
		}

		final StringBuilder sb=new StringBuilder();
		sb.append("function drawElements() {\n");
		for (String line: outputBody.toString().split("\\n")) {sb.append("  "); sb.append(line); sb.append('\n');}
		sb.append("}\n");
		outputBody.setLength(0);
		outputBody.append(sb.toString());

		if (mode!=Mode.JSONLY) {
			outputBody.append("\n");
			outputBody.append("initDrawElements();\n");
			outputBody.append("drawElements();\n");

			outputFoot.append("//-->\n");
			outputFoot.append("</script>\n");

			if (mode==Mode.FULL) {
				outputFoot.append("<!--\n");
				outputFoot.append("QSModel\n");
				final ByteArrayOutputStream out=new ByteArrayOutputStream();
				model.saveToStream(out);
				final String base64bytes=Base64.getEncoder().encodeToString(out.toByteArray());
				outputFoot.append("data:application/xml;base64,"+base64bytes+"\n");
				outputFoot.append("-->\n");
			}

			outputFoot.append("</body>\n");
			outputFoot.append("</html>\n");
		}
		return null;
	}

	/**
	 * Gibt den Namen des Modells in in einem html-Kommentar aus.
	 * @param element	Element dessen Name ausgegeben werden soll
	 * @see #processElement(ModelElement)
	 */
	private void outputElementName(final ModelElement element) {
		final String name=element.getName();
		if (name==null || name.trim().isEmpty()) {
			outputBody.append("/* id="+element.getId()+" */\n");
		} else {
			outputBody.append("/* \""+encodeHTML(name,true)+"\" id="+element.getId()+" */\n");
		}
	}

	/**
	 * Fügt ein Element zu der Ausgabe hinzu
	 * @param element	Element, das in die Ausgabe aufgenommen werden soll
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	@Override
	protected String processElement(final ModelElement element) {
		outputElementName(element);
		return super.processElement(element);
	}

	/**
	 * Nimmt eine JS-Hilfsfunktion in die Liste der Funktionen, die vor dem JS-Hauptbereich
	 * ausgegeben werden sollen, auf.
	 * @param key	Interner Bezeichner für die Funktion
	 * @param buildJSUserFunction	Lambda-Ausdruck, der bei Bedarf die JS-Funktion liefert
	 */
	public void addJSUserFunction(final String key, final Function<HTMLOutputBuilder,String> buildJSUserFunction) {
		if (!userJSFunctions.containsKey(key)) userJSFunctions.put(key,buildJSUserFunction.apply(this));
	}
}