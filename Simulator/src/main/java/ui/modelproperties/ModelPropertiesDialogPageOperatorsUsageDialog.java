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
import java.util.HashMap;
import java.util.Map;

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
		super(owner,Language.tr("Resources.Usage.Dialog.Title"));

		builder=getInfo(model);

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
		setSizeRespectingScreensize(600,800);
		setMinSizeRespectingScreensize(600,800);
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * Liefert eine Zuordnung von Bedienergruppen zu Stationen und Anzahl an Bedienern an den Stationen
	 * @param surface	Zeichenfläche, aus der die Daten ausgelesen werden sollen
	 * @return	Zuordnung von Bedienergruppen zu Stationen und Anzahl an Bedienern an den Stationen
	 */
	private static Map<String,Map<String,Integer>> getMap(final ModelSurface surface) {
		final Map<String,Map<String,Integer>> map=new HashMap<>();

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelDataResourceUsage) {
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
				final Map<String,Integer> data=((ModelDataResourceUsage)element).getUsedResourcesInfo();
				for (Map.Entry<String,Integer> resource: data.entrySet()) {
					map.compute(resource.getKey(),(key,value)->{
						if (value==null) {
							final Map<String,Integer> sub=new HashMap<>();
							sub.put(name.toString(),resource.getValue());
							return sub;
						} else {
							value.put(name.toString(),resource.getValue());
							return value;
						}
					});
				}
				continue;
			}
			if (element instanceof ModelElementSub) {
				final Map<String,Map<String,Integer>> part=getMap(((ModelElementSub)element).getSubSurface());
				for (Map.Entry<String,Map<String,Integer>> entry: part.entrySet()) {
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
			return resource.getSchedule();
		}
		return "";
	}

	/**
	 * Erstellt einen Text, der Informationen über die Nutzung der Bedienergruppen enthält.
	 * @param model	Modell aus dem die Daten ausgelesen werden sollen
	 * @return	Text, der Informationen über die Nutzung der Bedienergruppen enthält
	 */
	private static StyledTextBuilder getInfo(final EditModel model) {
		final StyledTextBuilder builder=new StyledTextBuilder();
		final Map<String,Map<String,Integer>> map=getMap(model.surface);

		builder.addHeading(1,Language.tr("Resources.Usage.Dialog.Title"));
		for (String resourceName: map.keySet().stream().sorted().toArray(String[]::new)) {
			final Map<String,Integer> resourceData=map.get(resourceName);
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
			for (String stationName: resourceData.keySet().stream().sorted().toArray(String[]::new)) {
				builder.addLine(stationName+": "+resourceData.get(stationName));
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
