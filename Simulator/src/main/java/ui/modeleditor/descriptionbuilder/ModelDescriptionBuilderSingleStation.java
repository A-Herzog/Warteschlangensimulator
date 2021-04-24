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
	/** Sammelt die html-Ausgabezeilen */
	private final StringBuilder description;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public ModelDescriptionBuilderSingleStation(final EditModel model) {
		super(model);
		description=new StringBuilder();
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
			for (String[] property: properties.get(key)) {
				final String heading=encodeHTML(property[0]);
				final String[] lines=property[1].split("\\\n");
				if (description.length()>0) description.append("<br>\n");
				if (lines.length==1) {
					description.append("<b>"+heading+"</b>: "+encodeHTML(lines[0]));
				} else {
					description.append("<b>"+heading+"</b>:");
					for (String line: lines) description.append("<br>\n"+encodeHTML(line));
				}
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
		return "<p>\n"+description.toString()+"\n<br>&nbsp;</p>";
	}
}
