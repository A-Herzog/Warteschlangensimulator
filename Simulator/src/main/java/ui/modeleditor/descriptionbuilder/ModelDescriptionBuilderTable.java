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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import simulator.editmodel.EditModel;
import systemtools.statistics.XWPFDocumentPictureTools;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Erzeugt eine Beschreibungen für das Modell
 * in einem Tabellenformat.
 * @author Alexander Herzog
 * @see ModelDescriptionBuilder
 */
public class ModelDescriptionBuilderTable extends ModelDescriptionBuilder {
	/**
	 * Daten zu den einzelnen Stationen
	 * @see StationTable
	 */
	private final List<StationTable> tables;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public ModelDescriptionBuilderTable(final EditModel model) {
		this(model,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 * @param elements	Liste der Elemente, die berücksichtigt werden sollen (kann <code>null</code> sein, dann werden alle Elemente verarbeitet)
	 */
	public ModelDescriptionBuilderTable(final EditModel model, final List<ModelElement> elements) {
		super(model,elements);
		tables=new ArrayList<>();
	}

	/**
	 * Erstellt den Beschreibungstext für eine Station aus den Daten der Station
	 * @param station	Station, für die ein Beschreibungstext erstellt werden soll
	 * @param properties	Eigenschaften der Station
	 */
	@Override
	protected void processStation(ModelElementBox station, Map<Integer, List<String[]>> properties) {
		if (properties.size()==0) return;

		final StationTable table=new StationTable(getStationName(station));
		tables.add(table);

		for (int key: properties.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray()) {
			for (String[] property: properties.get(key)) table.properties.add(property);
		}
	}

	@Override
	protected void processVariables(final List<String> variables) {
		/* Variablenwerte werden in der Tabelle nicht ausgegeben */
	}

	@Override
	protected void processResources(final List<String> resources) {
		/* Ressourcenwerte werden in der Tabelle nicht ausgegeben */
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTML(final String line) {
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Liefert die Modellbeschreibung als HTML-Fragment
	 * @return	Modellbeschreibung als HTML-Fragment
	 */
	public String getHTML() {
		done();

		final StringBuilder output=new StringBuilder();
		output.append("<h1>");
		output.append(getModelDescriptionTitle());
		output.append("</h1>\n");

		for (StationTable table: tables) {
			output.append("<h2>"+table.name+"</h2>\n");
			output.append("<table>\n");

			for (String[] property: table.properties) {
				output.append("  <tr>\n");
				output.append("    <td>"+encodeHTML(property[0])+"</td>\n");
				final String[] lines=property[1].split("\\n");
				if (lines.length==1) {
					output.append("    <td>"+encodeHTML(lines[0])+"</td>\n");
				} else {
					output.append("    <td>\n");
					for (int i=0;i<lines.length;i++) {
						output.append("      "+encodeHTML(lines[i]));
						if (i<lines.length-1) output.append("<br>");
						output.append("\n");
					}
					output.append("    </td>\n");
				}
				output.append("  </tr>\n");
			}

			output.append("</table>\n");
		}

		return output.toString();
	}

	/**
	 * Liefert die Modellbeschreibung als Markdown
	 * @return	Modellbeschreibung als Markdown
	 */
	public String getMD() {
		done();

		final StringBuilder output=new StringBuilder();
		output.append("# "+getModelDescriptionTitle()+"  \n");

		for (StationTable table: tables) {
			output.append("\n");
			output.append("## "+table.name+"  \n");
			output.append("<table>\n");

			for (String[] property: table.properties) {
				output.append("  <tr>\n");
				output.append("    <td>"+encodeHTML(property[0])+"</td>\n");
				final String[] lines=property[1].split("\\n");
				if (lines.length==1) {
					output.append("    <td>"+encodeHTML(lines[0])+"</td>\n");
				} else {
					output.append("    <td>\n");
					for (int i=0;i<lines.length;i++) {
						output.append("      "+encodeHTML(lines[i]));
						if (i<lines.length-1) output.append("<br>");
						output.append("\n");
					}
					output.append("    </td>\n");
				}
				output.append("  </tr>\n");
			}

			output.append("</table>  \n");
		}

		return output.toString();
	}

	/**
	 * Liefert ein Bild des Modells
	 * @return	Bild des Modells
	 */
	private BufferedImage getModelImage() {
		ModelSurfacePanel surfacePanel=new ModelSurfacePanel();
		surfacePanel.setSurface(getModel(),getModel().surface,getModel().clientData,getModel().sequences);
		return surfacePanel.getImage(2000,2000);
	}

	/**
	 * Schreibt die Modellbeschreibung in ein Word-Dokument
	 * @param doc	Word-Dokument in das die Modellbeschreibung geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveDOCX(XWPFDocument doc) {
		XWPFParagraph p;
		XWPFRun r;

		p=doc.createParagraph();
		r=p.createRun();
		r.setBold(true);
		r.setFontSize(18);
		r.setText(getModelDescriptionTitle());

		for (StationTable table: tables) {

			p=doc.createParagraph();
			r=p.createRun();
			r.setBold(true);
			r.setFontSize(15);
			r.setText(table.name);

			XWPFTable t=doc.createTable();

			for (int i=0;i<table.properties.size();i++) {
				XWPFTableRow row;
				if (t.getRows().size()>i) row=t.getRows().get(i); else row=t.createRow();
				String[] property=table.properties.get(i);
				for (int j=0;j<property.length;j++) {
					XWPFTableCell cell;
					if (row.getTableCells().size()>j) cell=row.getTableCells().get(j); else cell=row.addNewTableCell();
					if (j==0) {
						cell.setText(property[j]);
					} else {
						final String[] lines=property[j].split("\\n");
						if (lines.length==1) {
							cell.setText(lines[0]);
						} else {
							int count=cell.getParagraphs().size();
							for (int k=0;k<lines.length;k++) {
								if (k<count) p=cell.getParagraphs().get(k); else p=cell.addParagraph();
								r=p.createRun();
								r.setText(lines[k]);
							}
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Schreibt die Modellbeschreibung in ein Word-Dokument
	 * @param file	Dateiname unter dem das Word-Dokument gespeichert werden soll
	 * @param includeModelPicture	Fügt ein Bild des Modells in das Dokument ein
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveDOCX(File file, final boolean includeModelPicture) {
		try(XWPFDocument doc=new XWPFDocument()) {

			if (includeModelPicture) {
				final BufferedImage image=getModelImage();
				try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
					try {if (!ImageIO.write(image,"jpg",streamOut)) return false;} catch (IOException e) {return false;}
					if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_JPEG,image.getWidth(),image.getHeight())) return false;
				} catch (IOException e) {return false;}
			}

			if (!saveDOCX(doc)) return false;
			try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);}
			return true;
		} catch (IOException e) {return false;}
	}

	/**
	 * Tabellendaten zu einer einzelnen Station
	 * @see ModelDescriptionBuilderTable#tables
	 */
	private class StationTable {
		/** Name der Station */
		public final String name;
		/** Eigenschaften der Station */
		public final List<String[]> properties;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der Station
		 */
		public StationTable(final String name) {
			this.name=name;
			properties=new ArrayList<>();
		}
	}
}