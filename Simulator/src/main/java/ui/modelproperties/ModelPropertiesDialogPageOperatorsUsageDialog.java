/**
 * Copyright 2022 Alexander Herzog
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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelDataResourceUsage;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSchedule;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.descriptionbuilder.StyledTextBuilder;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt einen Dialog an, in dem aufgelistet wird,
 * welche Bedienergruppen wo eingesetzt werden.
 * @author Alexander Herzog
 * @see ModelPropertiesDialogPageOperators
 */
public class ModelPropertiesDialogPageOperatorsUsageDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2157115477377687264L;

	/**
	 * Builder für die Textzusammenstellung
	 */
	private final StyledTextBuilder builder;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editormodell, dem die Daten entnommen werden sollen
	 */
	public ModelPropertiesDialogPageOperatorsUsageDialog(final Component owner, final EditModel model) {
		this(owner,model,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editormodell, dem die Daten entnommen werden sollen
	 * @param groupName	Betrachtete Bedienergruppe (<code>null</code> für alle)
	 */
	public ModelPropertiesDialogPageOperatorsUsageDialog(final Component owner, final EditModel model, final String groupName) {
		super(owner,(groupName==null)?Language.tr("Resources.Usage.Dialog.Title"):Language.tr("Resources.Usage.Dialog.TitleSingular"));

		builder=getInfo(model,groupName);

		/* GUI */
		showCloseButton=true;
		addUserButton(Language.tr("Resources.Usage.Dialog.Copy"),Images.EDIT_COPY.getIcon());
		addUserButton(Language.tr("Resources.Usage.Dialog.Save"),Images.GENERAL_SAVE.getIcon());
		final JPanel content=createGUI(600,800,null);

		final JTextPane info=new JTextPane();
		info.setEditable(false);
		info.setBackground(new Color(0xFF,0xFF,0xF8));
		builder.writeToTextPane(info);
		content.setLayout(new BorderLayout());
		content.add(new JScrollPane(info),BorderLayout.CENTER);
		info.setSelectionStart(0);
		info.setSelectionEnd(0);

		/* Dialog anzeigen */
		if (groupName!=null) {
			setSizeRespectingScreensize(600,450);
			setMinSizeRespectingScreensize(600,450);
		} else {
			setSizeRespectingScreensize(600,800);
			setMinSizeRespectingScreensize(600,800);
		}
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * Liefert einen Anzeigenamen für eine Station
	 * @param element	Station für die ein Anzeigename (z.B. für Listen und Menüs) ermittelt werden soll
	 * @return	Anzeigename der Station
	 */
	public static String getStationName(final ModelElement element) {
		StringBuilder name=new StringBuilder();
		name.append(element.getContextMenuElementName());
		if (!element.getName().isEmpty()) {
			name.append(" \"");
			name.append(element.getName());
			name.append("\"");
		}
		name.append(" (id=");
		name.append(element.getId());
		name.append(")");
		return name.toString();
	}

	/**
	 * Liefert eine Zuordnung von Bedienergruppen zu Stationen und Anzahl an Bedienern an den Stationen
	 * @param surface	Zeichenfläche, aus der die Daten ausgelesen werden sollen
	 * @return	Zuordnung von Bedienergruppen zu Stationen und Anzahl an Bedienern an den Stationen
	 */
	private static Map<String,Map<ModelElement,Integer>> getMap(final ModelSurface surface) {
		final Map<String,Map<ModelElement,Integer>> map=new HashMap<>();

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelDataResourceUsage) {
				final Map<String,Integer> data=((ModelDataResourceUsage)element).getUsedResourcesInfo();
				for (Map.Entry<String,Integer> resource: data.entrySet()) {
					map.compute(resource.getKey(),(key,value)->{
						if (value==null) {
							final Map<ModelElement,Integer> sub=new HashMap<>();
							sub.put(element,resource.getValue());
							return sub;
						} else {
							value.put(element,resource.getValue());
							return value;
						}
					});
				}
				continue;
			}
			if (element instanceof ModelElementSub) {
				final Map<String,Map<ModelElement,Integer>> part=getMap(((ModelElementSub)element).getSubSurface());
				for (Map.Entry<String,Map<ModelElement,Integer>> entry: part.entrySet()) {
					map.compute(entry.getKey(),(key,value)->{
						if (value==null) return entry.getValue();
						value.putAll(entry.getValue());
						return value;
					});
				}
				continue;
			}
		}

		return map;
	}

	/**
	 * Liefert eine Liste aller Stationen, die Ressourcen verwenden.
	 * @param surface	Zeichenfläche, aus der die Daten ausgelesen werden sollen
	 * @return	Liste aller Stationen, die Ressourcen verwenden
	 */
	private static List<ModelElement> getAllResourceUsageStations(final ModelSurface surface) {
		final List<ModelElement> list=new ArrayList<>();

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelDataResourceUsage) list.add(element);
			if (element instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (element2 instanceof ModelDataResourceUsage) list.add(element2);
			}
		}

		return list;
	}

	/**
	 * Liefert eine Liste von Stationen, die Ressourcen verwenden, aber die angegebene Ressource noch nich.
	 * @param ressourceName	Name der Ressource
	 * @param surface	Zeichenfläche, aus der die Daten ausgelesen werden sollen
	 * @return	Liste aller Stationen, an denen die Ressourcen hinzugefügt werden könnte
	 */
	public static List<ModelElement> getPossibleNewStationsForRessource(final String ressourceName, final ModelSurface surface) {
		final Map<ModelElement,Integer> resourceUsage=getMap(surface).get(ressourceName);
		return getAllResourceUsageStations(surface).stream().filter(element->resourceUsage==null || !resourceUsage.containsKey(element)).collect(Collectors.toList());
	}

	/**
	 * Liefert eine Zeichenkette zur Beschreibung der Anzahl an Bedienern in einer Bedienergruppe
	 * @param model	Modell aus dem die Daten ausgelesen werden sollen
	 * @param groupName	Name der Bedienergruppe
	 * @return	Beschreibung der Anzahl an Bedienern
	 */
	private static String getResourceCount(final EditModel model, final String groupName) {
		final ModelResource resource=model.resources.getNoAutoAdd(groupName);
		if (resource==null) return "";

		if (resource.getMode()==ModelResource.Mode.MODE_NUMBER) {
			final int value=resource.getCount();
			if (value<0) return Language.tr("Resources.Group.RowTitle.Count")+": "+Language.tr("Resources.Number.Infinite");
			return Language.tr("Resources.Group.RowTitle.Count")+": "+value;
		}
		if (resource.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
			final String scheduleName=resource.getSchedule();
			final ModelSchedule schedule=model.schedules.getSchedule(scheduleName);
			if (schedule==null) {
				return scheduleName;
			} else {
				final int min=schedule.getSlots().stream().mapToInt(Integer::intValue).min().orElse(0);
				final int max=schedule.getSlots().stream().mapToInt(Integer::intValue).max().orElse(0);
				return scheduleName+String.format(" (%d-%d %s)",min,max,Language.tr("Editor.Operator.Plural"));
			}
		}
		return "";
	}

	/**
	 * Erstellt einen Text, der Informationen über die Nutzung der Bedienergruppen enthält.
	 * @param model	Modell aus dem die Daten ausgelesen werden sollen
	 * @param groupName	Betrachtete Bedienergruppe (<code>null</code> für alle)
	 * @return	Text, der Informationen über die Nutzung der Bedienergruppen enthält
	 */
	private static StyledTextBuilder getInfo(final EditModel model, final String groupName) {
		final StyledTextBuilder builder=new StyledTextBuilder();
		final Map<String,Map<ModelElement,Integer>> map=getMap(model.surface);

		if (groupName==null) {
			builder.addHeading(1,Language.tr("Resources.Usage.Dialog.Title"));
		}
		for (String resourceName: map.keySet().stream().sorted().toArray(String[]::new)) {
			if (groupName!=null && !resourceName.equals(groupName)) continue;
			final Map<ModelElement,Integer> resourceData=map.get(resourceName);
			builder.addHeading(2,resourceName);
			/* Anzahl der Bediener in der Ressource */
			final String count=getResourceCount(model,resourceName);
			if (count!=null && !count.isEmpty()) {
				builder.beginParagraph();
				builder.addLine(count);
				builder.endParagraph();
			}
			/* Einsatzorte der Ressource */
			builder.beginParagraph();
			final Map<String,Integer> resourceDataNames=new HashMap<>();
			for (Map.Entry<ModelElement,Integer> resource: resourceData.entrySet()) resourceDataNames.put(getStationName(resource.getKey()),resource.getValue());

			for (String stationName: resourceDataNames.keySet().stream().sorted().toArray(String[]::new)) {
				builder.addLine(stationName+": "+resourceDataNames.get(stationName));
			}
			builder.endParagraph();
		}

		return builder;
	}

	/**
	 * Speichert einen Text in einer Datei und gibt im
	 * Fehlerfall eine Fehlermeldung aus.
	 * @param text	Zu speichernder Text
	 * @param file	Ausgabedatei
	 */
	private void saveTextToFile(final String text, final File file) {
		try {
			if (file.isFile()) {
				if (!file.delete()) {
					MsgBox.error(this,Language.tr("Resources.Usage.Dialog.Save.Failed.Title"),String.format(Language.tr("Resources.Usage.Dialog.Save.Failed.Info"),file.toString()));
					return;
				}
			}
			Files.write(file.toPath(),text.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("Resources.Usage.Dialog.Save.Failed.Title"),String.format(Language.tr("Resources.Usage.Dialog.Save.Failed.Info"),file.toString()));
		}
	}

	/**
	 * Zeigt einen Dateiauswahldialog an und speichert dann,
	 * sofern die Auswahl nicht abgebrochen wurde, die Beschreibung
	 * in der angegebenen Datei.
	 * @see #saveTextToFile(String, File)
	 */
	private void saveToFile() {
		final File file=StyledTextBuilder.getSaveFile(this,Language.tr("Resources.Usage.Dialog.Save.Title"));
		if (file==null) return;

		if (file.getName().toLowerCase().endsWith(".txt")) {
			saveTextToFile(builder.getText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".rtf")) {
			saveTextToFile(builder.getRTFText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".html")) {
			saveTextToFile(builder.getHTMLText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".md")) {
			saveTextToFile(builder.getMDText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".docx")) {
			if (!builder.saveDOCX(file,null)) {
				MsgBox.error(this,Language.tr("Resources.Usage.Dialog.Save.Failed.Title"),String.format(Language.tr("Resources.Usage.Dialog.Save.Failed.Info"),file.toString()));
			}
		}

		if (file.getName().toLowerCase().endsWith(".odt")) {
			if (!builder.saveODT(file)) {
				MsgBox.error(this,Language.tr("Resources.Usage.Dialog.Save.Failed.Title"),String.format(Language.tr("Resources.Usage.Dialog.Save.Failed.Info"),file.toString()));
			}
		}

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			if (!builder.savePDF(this,file,null)) {
				MsgBox.error(this,Language.tr("Resources.Usage.Dialog.Save.Failed.Title"),String.format(Language.tr("Resources.Usage.Dialog.Save.Failed.Info"),file.toString()));
			}
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0: builder.copyToClipboard(); break;
		case 1: saveToFile(); break;
		}
	}
}
