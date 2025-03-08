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
package net.web;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import org.nanohttpd.protocols.http.IHTTPSession;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.AnimationPanel;
import ui.EditorPanel;
import ui.MainPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.statistics.StatisticsPanel;
import ui.tools.ServerPanel;

/**
 * Dieser Handler bietet verschiedene Funktionen zur Steuerung der Animation an.
 * @author Alexander Herzog
 * @see WebServerHandler
 */
public class HandlerAnimation implements WebServerHandler {
	/** Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen) */
	private final String serverURL;
	/** Pfad zu dem Animations-HTML-Dokument aus Java-Ressourcen-Sicht ("%LANG%" wird durch die aktuelle Sprache, also "de" oder "en" ersetzt) */
	private final String localURL;
	/** Hauptpanel des Simulators */
	private final MainPanel mainPanel;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param localURL	Pfad zu dem Animations-HTML-Dokument aus Java-Ressourcen-Sicht ("%LANG%" wird durch die aktuelle Sprache, also "de" oder "en" ersetzt)
	 * @param mainPanel	Hauptpanel des Simulators
	 */
	public HandlerAnimation(final String serverURL, final String localURL, final MainPanel mainPanel) {
		this.serverURL=serverURL;
		this.localURL=localURL;
		this.mainPanel=mainPanel;
	}

	/**
	 * Liefert den Wert eines weiteren HTML-Get-Parameters
	 * @param session	Anfragedaten
	 * @param parameterName	Name des HTML-Get-Parameters
	 * @return	Wert des Parameters oder <code>null</code>, wenn der Parameter nicht gesetzt ist
	 */
	private String getParameter(final IHTTPSession session, final String parameterName) {
		for (Map.Entry<String,List<String>> entry: session.getParameters().entrySet()) {
			if (entry.getKey().equalsIgnoreCase(parameterName) && entry.getValue()!=null && entry.getValue().size()==1) return entry.getValue().get(0);
		}
		return null;
	}

	/**
	 * Liefert die Basis-html-Webseite aus dem "res"-Ordner aus.
	 * @param response	Server-Antwort-Objekt welches die Basis-html-Webseite enthält
	 * @see #process(IHTTPSession)
	 */
	private void setGlobalHTMLResponse(final WebServerResponse response) {
		final String url=localURL.replace("%LANG%",Language.getCurrentLanguage());
		try (final InputStream stream=getClass().getResourceAsStream(url)) {
			response.setText(stream,WebServerResponse.Mime.HTML,false);
		} catch (IOException e) {}
	}

	/**
	 * Liefert das Modell als html-js-Fassung.
	 * @return	Modell als html-js-Fassung
	 */
	private String getStaticJS() {
		final HTMLOutputBuilder builder=new HTMLOutputBuilder(mainPanel.editorPanel.getModel(),HTMLOutputBuilder.Mode.JSONLY);
		final String[] data=builder.build();
		if (data.length<3) return "";
		return data[0]+data[1]+data[2];
	}

	/**
	 * Gibt das Modell als html-js-Fassung über eine Server-Antwort aus.
	 * @param response	Server-Antwort-Objekt
	 * @see #process(IHTTPSession)
	 */
	private void getModelJS(final WebServerResponse response) {
		response.setJS(getStaticJS(),false);
	}

	/**
	 * Liefert eine base64 encodierte Fassung eines Icons
	 * @param icon	Zu codierendes Icon
	 * @return	base64-html-encodiertes Icon für die Verwendung in html-Code
	 */
	private String base64Icon(final Icon icon) {
		try (final ByteArrayOutputStream output=new ByteArrayOutputStream()) {

			final BufferedImage image=new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_INT_RGB);
			final Graphics g=image.createGraphics();
			icon.paintIcon(null,g,0,0);
			g.dispose();

			if (!ImageIO.write(image,"png",output)) return "";
			final String base64bytes=Base64.getEncoder().encodeToString(output.toByteArray());
			return "data:image/png;base64,"+base64bytes;
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * Trägt die Daten aller Stationen auf einer Zeichenfläche in eine Zuordnung ein.
	 * @param editModel	Editor-Modell
	 * @param surface	Zeichenfläche deren Stationen (und die Stationen von Unter-Zeichenflächen) bearbeitet werden sollen
	 * @param map	Zuordnung in die die Stationen eingetragen werden sollen
	 * @see #listStationsJSON(WebServerResponse)
	 */
	private void addStationsToMap(final EditModel editModel, final ModelSurface surface, final Map<String,Object> map) {
		for (ModelElement element: surface.getElements()) {
			final Map<String,String> data=new HashMap<>();
			data.put("xmltype",element.getXMLNodeNames()[0]);
			data.put("type",element.getContextMenuElementName());
			data.put("name",element.getName());

			final Point p1=element.getPosition(true);
			final Point p2=element.getLowerRightPosition();

			if (p1!=null) {
				data.put("x",""+p1.x);
				data.put("y",""+p1.y);
				if (p2!=null) {
					data.put("w",""+(p2.x-p1.x));
					data.put("h",""+(p2.y-p1.y));
				}
			}

			final Icon icon=element.buildIcon();
			if (icon!=null) {
				data.put("icon",base64Icon(icon));
			}

			if (element instanceof ModelElementBox) {
				final ModelDescriptionBuilderStyled descriptionBuilder=new ModelDescriptionBuilderStyled(editModel,true);
				((ModelElementBox)element).buildDescription(descriptionBuilder);
				String info=descriptionBuilder.getText();
				info=info.replaceAll("\n","\\\\n");
				data.put("description",info);
			}

			map.put(""+element.getId(),data);

			if (element instanceof ModelElementSub) addStationsToMap(editModel,((ModelElementSub)element).getSubSurface(),map);
		}
	}

	/**
	 * Liefert eine Liste aller im Modell enthaltenen Stationen als json-Objekt.
	 * @param response	Server-Antwort-Objekt
	 * @see #process(IHTTPSession)
	 */
	private void listStationsJSON(final WebServerResponse response) {
		final Map<String,Object> info=new HashMap<>();

		final EditModel model=mainPanel.editorPanel.getModel();
		addStationsToMap(model,model.surface,info);

		final String json=makeJSON(info);
		response.setJSON(json,true);
	}

	/**
	 * Führt einen Animationsschritt aus.
	 * @param response	Server-Antwort-Objekt
	 * @see #process(IHTTPSession)
	 */
	private void doAnimationStep(final WebServerResponse response) {
		if (mainPanel.currentPanel instanceof ServerPanel) {
			((ServerPanel)mainPanel.currentPanel).requestClose();
			mainPanel.setCurrentPanel(mainPanel.editorPanel);
		}
		if (mainPanel.currentPanel instanceof AnimationPanel) {
			((AnimationPanel)mainPanel.currentPanel).step(true);
			try {Thread.sleep(50);} catch (InterruptedException e) {}
			response.setText("",true);
			return;
		}

		if (!(mainPanel.currentPanel instanceof EditorPanel) && !(mainPanel.currentPanel instanceof StatisticsPanel)) {
			response.setText(Language.tr("WebServer.Animation.StartError.WrongMode"),true);
			return;
		}

		final String error=mainPanel.startRemoteControlledAnimation();
		if (error!=null) {
			response.setText(error,true);
			return;
		}


		try {Thread.sleep(50);} catch (InterruptedException e) {}

		response.setText("",true);
	}

	/**
	 * Escaped doppelte Anführungszeichen
	 * @param text	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String escapeString(final String text) {
		return text.replace("\"","\\\"");
	}

	/**
	 * Wandelt eine Zuordnung von Zeichenketten zu Objekten in
	 * eine json-Zeichenkette um. Die Objekte in der Zuordnung
	 * dürfen dabei Zeichenketten oder erneut Zuordnung von
	 * Zeichenketten zu Objekten sein.
	 * @param map	Zuordnung von Zeichenketten zu Objekten
	 * @param indent	Einrückung der Zeilen in der json-Zeichenkette
	 * @return	json-Zeichenkette
	 */
	private String makeJSON(final Map<String,Object> map, String indent) {
		final StringBuilder sb=new StringBuilder();
		if (indent.isEmpty()) sb.append("{\n");
		boolean first=true;
		for (Map.Entry<String,Object> entry: map.entrySet()) {
			if (first) first=false; else sb.append(",\n");

			sb.append(indent+"  \""+escapeString(entry.getKey())+"\":");

			if (entry.getValue() instanceof String) {
				final String text=escapeString((String)entry.getValue());
				sb.append(" \""+text+"\"");
			}

			if (entry.getValue() instanceof Map<?,?>) {
				@SuppressWarnings("unchecked")
				final Map<String,Object> sub=(Map<String,Object>)entry.getValue();
				sb.append(" {\n");
				sb.append(makeJSON(sub,indent+"    "));
				sb.append("\n"+indent+"  }");
			}
		}
		if (indent.isEmpty()) sb.append("\n}\n");
		return sb.toString();
	}

	/**
	 * Wandelt eine Zuordnung von Zeichenketten zu Objekten in
	 * eine json-Zeichenkette um. Die Objekte in der Zuordnung
	 * dürfen dabei Zeichenketten oder erneut Zuordnung von
	 * Zeichenketten zu Objekten sein.
	 * @param info	Zuordnung von Zeichenketten zu Objekten
	 * @return	json-Zeichenkette
	 */
	private String makeJSON(final Map<String,Object> info) {
		return makeJSON(info,"");
	}

	/**
	 * Liefert den Status der Animation als Server-Antwort zurück.
	 * @param response	Server-Antwort-Objekt
	 * @see #process(IHTTPSession)
	 */
	private void getAnimationStatusJSON(final WebServerResponse response) {
		final Map<String,Object> info;

		if (!(mainPanel.currentPanel instanceof AnimationPanel)) {
			info=new HashMap<>();
			info.put("staticImages",new HashMap<>());
			info.put("movingImages",new HashMap<>());
			info.put("logs",Language.tr("WebServer.Animation.Error.NoAnimationRunning"));
		} else {
			info=((AnimationPanel)(mainPanel.currentPanel)).getAnimationStepInfo();
		}

		final String json=makeJSON(info);
		response.setJSON(json,true);
	}

	/**
	 * Bricht die laufende Animation ab.
	 * @see #process(IHTTPSession)
	 */
	private void terminateAnimation() {
		if (mainPanel.currentPanel instanceof AnimationPanel) {
			((AnimationPanel)mainPanel.currentPanel).closeRequest();
		}
	}

	/**
	 * Stellt das Modell als Grafikdatei als Server-Antwort bereit
	 * @param response	Server-Antwort-Objekt
	 * @see #process(IHTTPSession)
	 */
	private void getScreenshotPNG(final WebServerResponse response) {
		final BufferedImage image;
		if (!(mainPanel.currentPanel instanceof AnimationPanel)) {
			image=mainPanel.editorPanel.getPrintImage(-1);
		} else {
			image=((AnimationPanel)mainPanel.currentPanel).getScreenshot();
		}
		response.setPNG(image);
	}

	/**
	 * Berechnet einen Ausdruck.
	 * @param response	Server-Antwort-Objekt über das das Ergebnis ausgegeben werden soll
	 * @param expression	Zu berechnender Ausdruck
	 * @see #process(IHTTPSession)
	 */
	private void calculateExpression(final WebServerResponse response, final String expression) {
		if (expression==null || expression.isBlank()) return;
		if (!(mainPanel.currentPanel instanceof AnimationPanel)) return;

		AnimationPanel animationPanel=(AnimationPanel)mainPanel.currentPanel;
		final Double D=animationPanel.calculateExpression(expression);
		if (D==null) {
			response.setText("",false);
		} else {
			response.setText(NumberTools.formatSystemNumber(D.doubleValue()),false);
		}
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (!testURL(session,serverURL)) return null;

		final String cmd=getParameter(session,"command");
		final WebServerResponse response=new WebServerResponse();

		if (cmd==null) {
			setGlobalHTMLResponse(response);
		} else {
			if (cmd.equalsIgnoreCase("model")) getModelJS(response);
			if (cmd.equalsIgnoreCase("stations")) listStationsJSON(response);
			if (cmd.equalsIgnoreCase("step")) doAnimationStep(response);
			if (cmd.equalsIgnoreCase("status")) getAnimationStatusJSON(response);
			if (cmd.equalsIgnoreCase("quit")) terminateAnimation();
			if (cmd.equalsIgnoreCase("image")) getScreenshotPNG(response);
			if (cmd.equalsIgnoreCase("calc")) calculateExpression(response,getParameter(session,"expression"));
		}

		return response;
	}
}