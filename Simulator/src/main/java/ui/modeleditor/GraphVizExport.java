/**
 * Copyright 2021 Alexander Herzog
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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.EditorPanelStatistics;
import ui.MainFrame;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderSingleStation;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementSubConnect;
import ui.modeleditor.elements.ModelElementSubIn;
import ui.modeleditor.elements.ModelElementSubOut;
import ui.modeleditor.elements.ModelElementVertex;
import ui.modeleditor.fastpaint.BrighterColor;

/**
 * Ermöglicht den Export der Zeichnung als GraphViz-Diagramm<br>
 * <a href="https://graphviz.org/">https://graphviz.org/</a>
 * @author Alexander Herzog
 */
public class GraphVizExport {
	/**
	 * Auszugebende Textzeilen
	 * @see #save(File)
	 */
	private final List<String> output;

	/**
	 * Referenz auf das Objekt zur Erstellung von helleren Farben
	 */
	private final BrighterColor brighter;

	/**
	 * Hilfsklasse zur Generierung der Statistikinformationen zu den Stationen
	 */
	private final EditorPanelStatistics statisticsHelper;

	/**
	 * Konstruktor der Klasse
	 */
	public GraphVizExport() {
		output=new ArrayList<>();
		brighter=new BrighterColor();
		statisticsHelper=new EditorPanelStatistics(false);
	}

	/**
	 * Überträgt den Inhalt des Modells in ein GraphViz-Diagramm
	 * @param model	Modell-Objekt aus dem die Daten ausgelesen werden sollen
	 * @param statistics	Optionales Statistikobjekt aus dem Statistikinformationen zu den Stationen ausgelesen werden (darf <code>null</code> sein)
	 */
	public void process(final EditModel model, final Statistics statistics) {
		output.add("digraph Model {");
		output.add("");

		output.add("  /*");
		output.add("  "+String.format(Language.tr("GraphVizExport.Info1"),MainFrame.PROGRAM_NAME));
		output.add("  "+Language.tr("GraphVizExport.Info2"));
		output.add("  */");

		output.add("");
		output.add("  node [fontname=\"Sans-Serif\"]");
		output.add("  edge [fontname=\"Sans-Serif\", fontsize=9.0]");

		if (!model.name.trim().isEmpty()) {
			output.add("");
			output.add("  label=<"+encodeText(model.name.trim())+">");
			output.add("  fontname=\"Sans-Serif\"");
			output.add("  fontsize=18.0");
		}
		output.add("");
		process(model,statistics,model.surface,"  ");
		output.add("");
		output.add("}");
	}

	/**
	 * Verarbeitet die Daten einer einzelnen Zeichenfläche (Hauptzeichenfläche oder Untermodell)
	 * @param model	Modell-Objekt aus dem die Daten ausgelesen werden sollen
	 * @param statistics	Optionales Statistikobjekt aus dem Statistikinformationen zu den Stationen ausgelesen werden (darf <code>null</code> sein)
	 * @param surface	Zeichenfläche
	 * @param indent Einrückung der Ausgabezeilen
	 * @see #process(EditModel, Statistics)
	 */
	private void process(final EditModel model, final Statistics statistics, final ModelSurface surface, final String indent) {
		/* Stationen zeichnen */
		for (ModelElement element: surface.getElements()) if ((element instanceof ModelElementBox) && !(element instanceof ModelElementSub)) {
			final ModelElementBox box=(ModelElementBox)element;
			output.add(indent+element.getId()+" [");
			output.add(indent+"  shape=record");

			final ModelDescriptionBuilderSingleStation builder=new ModelDescriptionBuilderSingleStation(model,ModelDescriptionBuilderSingleStation.Mode.PLAIN);
			box.buildDescription(builder);
			builder.done();
			final String description=builder.getDescription();

			if (box instanceof ModelElementSubConnect) {
				output.add(indent+"  label=<"+encodeLabel(box.getTypeName(),null,null)+">,");
			} else {
				output.add(indent+"  label=<"+encodeLabel(box.getTypeName(),box.getName(),statisticsHelper.getTooltip(statistics,box))+">,");
			}
			output.add(indent+"  tooltip=\""+encodeTooltip(description)+"\",");
			final Color color=box.getDrawBackgroundColor();
			output.add(indent+"  fillcolor=\""+getColor(color)+":"+getColor(brighter.get(color))+"\",");
			output.add(indent+"  style=\"filled\"");
			output.add(indent+"]");
		}

		/* Untermodelle zeichnen */
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementSub) {
			final ModelElementSub sub=(ModelElementSub)element;
			output.add("");
			output.add(indent+"subgraph cluster_"+element.getId()+" {");
			output.add(indent+"  label=<"+encodeLabel(sub.getTypeName(),sub.getName(),null)+">");
			output.add(indent+"  fontname=\"Sans-Serif\"");
			output.add("");
			process(model,statistics,sub.getSubSurface(),indent+"  ");
			output.add(indent+"}");
		}

		output.add("");

		/* Verbindungskanten einzeichnen */
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			final ModelElementBox box=(ModelElementBox)element;

			if (element instanceof ModelElementEdgeOut) {
				process(statistics,box,((ModelElementEdgeOut)element).getEdgeOut(),indent);
			}
			if (element instanceof ModelElementEdgeMultiOut) {
				for (ModelElementEdge edge: ((ModelElementEdgeMultiOut)element).getEdgesOut()) process(statistics,box,edge,indent);
			}
		}
	}

	/**
	 * Fügt eine Kante zu der Ausgabe hinzu.
	 * @param statistics	Optionales Statistikobjekt aus dem Statistikinformationen zu den Stationen ausgelesen werden (darf <code>null</code> sein)
	 * @param source	Ausgangselement der Kante
	 * @param edge	Kante
	 * @param indent Einrückung der Ausgabezeilen
	 */
	private void process(final Statistics statistics, ModelElementBox source, ModelElementEdge edge, final String indent) {
		if (edge==null) return;
		final ModelElementEdge firstEdge=edge;

		ModelElementBox destination;
		while (true) {
			if (edge==null) return;
			final ModelElement element=edge.getConnectionEnd();
			if (element==null) return;
			if (element instanceof ModelElementVertex) {
				edge=((ModelElementVertex)element).getEdgeOut();
				continue;
			}
			if (element instanceof ModelElementBox) {
				destination=(ModelElementBox)element;
				break;
			}
			return;
		}

		if (source instanceof ModelElementSub) {
			final ModelElementSub sub=(ModelElementSub)source;
			final List<ModelElementEdge> edges=Arrays.asList(sub.getEdgesOut());
			final int nr=edges.indexOf(firstEdge);
			if (nr>=0) for (ModelElement subElement: sub.getSubSurfaceReadOnly().getElements()) if (subElement instanceof ModelElementSubOut) {
				final ModelElementSubOut connect=(ModelElementSubOut)subElement;
				if (connect.getConnectionNr()==nr) {
					source=connect;
					break;
				}
			}
		}

		if (destination instanceof ModelElementSub) {
			final ModelElementSub sub=(ModelElementSub)destination;
			final List<ModelElementEdge> edges=Arrays.asList(sub.getEdgesIn());
			final int nr=edges.indexOf(edge);
			if (nr>=0) for (ModelElement subElement: sub.getSubSurfaceReadOnly().getElements()) if (subElement instanceof ModelElementSubIn) {
				final ModelElementSubIn connect=(ModelElementSubIn)subElement;
				if (connect.getConnectionNr()==nr) {
					destination=connect;
					break;
				}
			}
		}

		final String connection=indent+source.getId()+" -> "+destination.getId();
		final String edgeLabel=firstEdge.getName().trim();
		final String throughput=EditorPanelStatistics.getEdgeThroughput(statistics,source.getId(),destination.getId());

		if (edgeLabel.isEmpty() && throughput==null) {
			output.add(connection+";");
		} else {
			String text=edgeLabel;
			if (throughput!=null) {
				if (!text.isEmpty()) text=text+"\n"+throughput; else text=throughput;
			}

			output.add(connection+" [label=<"+encodeText(text)+">];");
		}
	}

	/**
	 * Verarbeitet einen Text für die Ausgabe in einem HTML-formatierten Eintrag
	 * @param text	Umzuwandelnder Text
	 * @return	Ausgabetext
	 */
	private String encodeText(final String text) {
		return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br/>");
	}

	/**
	 * Verarbeitet einen Stations-Label für die Ausgabe in einem HTML-formatierten Eintrag
	 * @param type	Stationstyp
	 * @param name	Stationsname (darf <code>null</code> oder leer sein)
	 * @param statistic	Statistikinformationen (darf <code>null</code> oder leer sein)
	 * @return	Ausgabetext
	 */
	private String encodeLabel(final String type, final String name, final String statistic) {
		final StringBuilder result=new StringBuilder();

		if (name==null || name.isEmpty()) {
			result.append(encodeText(type));
		} else {
			result.append("<font point-size=\"10\">");
			result.append(encodeText(type));
			result.append("</font>");
			result.append("<br/>");
			result.append("<b>");
			result.append(encodeText(name));
			result.append("</b>");
		}

		if (statistic!=null && !statistic.isEmpty()) {
			result.append("<br/>");
			result.append("<font point-size=\"10\">");
			result.append(statistic);
			result.append("</font>");
		}

		return result.toString();
	}

	/**
	 * Verarbeitet einen Text für die Ausgabe in einem Reintext-Eintrag
	 * @param text	Umzuwandelnder Text
	 * @return	Ausgabetext
	 */
	private String encodeTooltip(final String text) {
		return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","\\n").replace("\"","\\\"");
	}

	/**
	 * Lieft einen CSS-Farbcode für eine Farbe
	 * @param color	Umzuwandelnde Farbe
	 * @return	CSS-Farbcode
	 */
	private String getColor(final Color color) {
		return String.format("#%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue());
	}

	/**
	 * Speichert das Diagramm.
	 * @param file	Ausgabedatei
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public boolean save(final File file) {
		final String outputString=String.join("\n",output);
		return Table.saveTextToFile(outputString,file);
	}
}