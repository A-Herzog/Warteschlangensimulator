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
package ui.modeleditor.descriptionbuilder;

import java.util.List;
import java.util.Map;

import simulator.editmodel.EditModel;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Erstellt eine Stationsbeschreibung als html-Code für eine
 * einzelne Station zur Anzeige in einem Stations-Tooltip.
 * @author Alexander Herzog
 * @see ModelSurfacePanel#getTooltipDescription
 */
public class ModelDescriptionBuilderSingleStation extends ModelDescriptionBuilder {
	/**
	 * Ausgabemodus
	 */
	public enum Mode {
		/** Ausgabe als reiner Text */
		PLAIN,
		/** HTML-formatierte Ausgabe */
		HTML
	}

	/**
	 * Ausgabemodus
	 */
	private final Mode mode;

	/**
	 * Sammelt die Ausgabezeilen
	 */
	private final StringBuilder description;

	/**
	 * Maximalanzahl an auszugebenden Zeilen (-1 für kein Limit)
	 */
	private final int linesLimit;

	/**
	 * Anzahl an bislang ausgegebenen Zeilen
	 */
	private int linesCount;

	/**
	 * Wenn das Zeilenlimit überschritten wurde: Wurde bereits ein Auslassungshinweis ("...") ausgegeben?
	 */
	private boolean limitInfoShown;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 * @param mode	Ausgabemodus
	 * @param linesLimit	Maximalanzahl an auszugebenden Zeilen (-1 für kein Limit)
	 */
	public ModelDescriptionBuilderSingleStation(final EditModel model, final Mode mode, final int linesLimit) {
		super(model);
		this.mode=mode;
		this.linesLimit=linesLimit;
		linesCount=0;
		limitInfoShown=false;
		description=new StringBuilder();
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 * @param mode	Ausgabemodus
	 */
	public ModelDescriptionBuilderSingleStation(final EditModel model, final Mode mode) {
		this(model,mode,-1);
	}

	/**
	 * Konstruktor der Klasse (Ausgabemodus: HTML)
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public ModelDescriptionBuilderSingleStation(final EditModel model) {
		this(model,Mode.HTML,-1);
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTML(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	@Override
	protected void processStation(ModelElementBox station, Map<Integer,List<String[]>> properties) {
		for (int key: properties.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray()) {
			for (String[] property: properties.get(key)) switch (mode) {
			case HTML: processPropertyHTML(property); break;
			case PLAIN:  processPropertyPlain(property); break;
			}
		}
	}

	/**
	 * Verarbeitet die Ausgabe im Reintext-Modus
	 * @param property	Auszugebende Zeilen
	 * @see #processStation(ModelElementBox, Map)
	 */
	private void processPropertyPlain(final String[] property) {
		if (linesLimit>0 && linesCount>linesLimit) {
			if (!limitInfoShown) {
				limitInfoShown=true;
				description.append("...\n");
			}
			return;
		}
		final String[] lines=property[1].split("\\\n");
		if (lines.length==1) {
			description.append(property[0]+": "+lines[0]+"\n");
			linesCount++;
		} else {
			description.append(property[0]+":\n");
			for (var line: lines) {
				linesCount++;
				if (linesLimit>0 && linesCount>linesLimit) {description.append("...\n"); limitInfoShown=true; break;}
				description.append(line+"\n");
			}
		}
	}

	/**
	 * Verarbeitet die Ausgabe im HTML-Modus
	 * @param property	Auszugebende Zeilen
	 * @see #processStation(ModelElementBox, Map)
	 */
	private void processPropertyHTML(final String[] property) {
		if (linesLimit>0 && linesCount>linesLimit) {
			if (!limitInfoShown) {
				limitInfoShown=true;
				description.append("<br>...");
			}
			return;
		}
		final String heading=encodeHTML(property[0]);
		final String[] lines=property[1].split("\\\n");
		if (description.length()>0) description.append("<br>\n");
		if (lines.length==1) {
			description.append("<b>"+heading+"</b>: "+encodeHTML(lines[0]));
			linesCount++;
		} else {
			description.append("<b>"+heading+"</b>:");
			for (var line: lines) {
				linesCount++;
				if (linesLimit>0 && linesCount>linesLimit) {description.append("<br>..."); limitInfoShown=true; break;}
				description.append("<br>\n"+encodeHTML(line));
			}
		}
	}

	@Override
	protected void processResources(List<String> resources) {
		/* findet für diesen Builder nicht statt */
	}

	@Override
	protected void processVariables(List<String> variables) {
		/* findet für diesen Builder nicht statt */
	}

	/**
	 * Liefert die Beschreibung zu der Station als html-Code
	 * @return	Beschreibung der Station als html-Code
	 */
	public String getDescription() {
		switch (mode) {
		case HTML: return "<p>\n"+description.toString()+"\n<br>&nbsp;</p>";
		case PLAIN: return description.toString();
		default: return "<p>\n"+description.toString()+"\n<br>&nbsp;</p>";
		}
	}
}
