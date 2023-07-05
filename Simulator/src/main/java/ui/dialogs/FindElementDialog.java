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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.JRegExWikipediaLinkLabel;
import systemtools.JSearchSettingsSync;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ElementRendererTools;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Ermöglicht die Auswahl eines Elements durch die Eingabe der ID oder des Namens.
 * @author Alexander Herzog
 */
public class FindElementDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1687364491601255550L;

	/** Modell-Haupt-Surface, welches alle Elemente enthält */
	private final ModelSurface surface;
	/** Eingabefeld für die Suche */
	private final JTextField searchEdit;
	/** Suchmodus (IDs, Namen, alles) */
	private final JComboBox<String> optionsCombo;
	/** Auch Elemente auf ausgeblendeten Ebenen berücksichtigen? */
	private final JCheckBox includeHidden;
	/** Groß- und Kleinschreibung bei der Suche berücksichtigen? */
	private final JCheckBox optionCaseSensitive;
	/** Nur vollständige Übereinstimmungen als Treffer zählen */
	private final JCheckBox optionFullMatchOnly;
	/** Ist der Suchbegriff ein regulärer Ausdruck? */
	private final JCheckBox optionRegEx;
	/** Liste mit den Suchergebnissen */
	private final JList<ElementRendererTools.InfoRecord> resultsList;
	/** Datenmodell für die Liste mit den Suchergebnissen */
	private final DefaultListModel<ElementRendererTools.InfoRecord> resultsModel;
	/** IDs der Suchergebnisse */
	private final List<Integer> resultsIds;
	/** Ausgabefeld für Informationen zu den Suchergebnissen (Anzahl der Treffer usw.) */
	private final JLabel resultsInfo;
	/** Soll nach dem Schließen dieses Dialog der Volltextsuche-Dialog geöffnet werden? */
	private boolean openFindAndReplaceDialog;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param surface	Modell-Haupt-Surface, welches alle Elemente enthält
	 */
	public FindElementDialog(final Component owner, final ModelSurface surface) {
		super(owner,Language.tr("FindElementDirect.Title"));
		this.surface=surface;

		showCloseButton=true;
		addUserButton(Language.tr("Main.Menu.View.FindAndReplace"),Images.GENERAL_FONT.getIcon());
		final JPanel all=createGUI(()->Help.topicModal(getOwner(),"FindElement"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalFindElement);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		final JPanel setupArea=new JPanel();
		setupArea.setLayout(new BoxLayout(setupArea,BoxLayout.PAGE_AXIS));
		content.add(setupArea,BorderLayout.NORTH);

		/* Einstellungenbereich */

		JPanel line;
		JLabel label;

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("FindElementDirect.IdOrName")+":",""+getSmallestId());
		setupArea.add((JPanel)data[0]);
		searchEdit=(JTextField)data[1];
		searchEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {search();}
			@Override public void keyReleased(KeyEvent e) {search();}
			@Override public void keyPressed(KeyEvent e) {search();}
		});

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("FindElementDirect.Option")+":"));
		line.add(optionsCombo=new JComboBox<>(new String[]{
				Language.tr("FindElementDirect.Option.ID"),
				Language.tr("FindElementDirect.Option.Names"),
				Language.tr("FindElementDirect.Option.All")
		}));
		label.setLabelFor(optionsCombo);
		optionsCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_FIND_BY_ID,
				Images.GENERAL_FIND_BY_NAME,
				Images.GENERAL_FIND_BY_ALL
		}));
		optionsCombo.setSelectedIndex(2);
		optionsCombo.addActionListener(e->search());

		includeHidden=new JCheckBox(Language.tr("FindElementDirect.IncludeHidden"));
		if (!surface.getLayers().isEmpty()) {
			setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(includeHidden);
		}
		includeHidden.setToolTipText(Language.tr("FindElementDirect.IncludeHidden.Info"));
		includeHidden.addActionListener(e->search());

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionCaseSensitive=new JCheckBox(Language.tr("FindElementDirect.CaseSensitive"),JSearchSettingsSync.getCaseSensitive()));
		optionCaseSensitive.addActionListener(e->{JSearchSettingsSync.setCaseSensitive(optionCaseSensitive.isSelected()); search();});

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionFullMatchOnly=new JCheckBox(Language.tr("FindElementDirect.FullMatchOnly"),JSearchSettingsSync.getFullMatchOnly()));
		optionFullMatchOnly.addActionListener(e->{JSearchSettingsSync.setFullMatchOnly(optionFullMatchOnly.isSelected()); search();});

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionRegEx=new JCheckBox(Language.tr("FindElementDirect.RegEx"),JSearchSettingsSync.getRegEx()));
		line.add(new JRegExWikipediaLinkLabel(this));
		optionRegEx.addActionListener(e->{JSearchSettingsSync.setRegEx(optionRegEx.isSelected()); search();});

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(resultsInfo=new JLabel());

		/* Anzeigebereich */

		resultsIds=new ArrayList<>();
		content.add(new JScrollPane(resultsList=new JList<>(resultsModel=new DefaultListModel<>())),BorderLayout.CENTER);
		resultsList.setCellRenderer(new ElementRendererTools.InfoRecordListCellRenderer(ElementRendererTools.GradientStyle.OFF));
		resultsList.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {close(BaseDialog.CLOSED_BY_OK); e.consume(); return;}}
		});

		/* Fußzeile */

		final JPanel footer=new JPanel();
		footer.setLayout(new BoxLayout(footer,BoxLayout.PAGE_AXIS));
		content.add(footer,BorderLayout.SOUTH);
		footer.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("FindElementDirect.ClickInfo")));

		/* Starten */

		search();
		setMinSizeRespectingScreensize(550,500);
		setSizeRespectingScreensize(550,500);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Liefert die kleinste im Modell vorkommende ID.
	 * @return	Kleinste im Modell vorkommende ID oder 0, wenn das Modell leer ist
	 */
	private int getSmallestId() {
		int minId=Integer.MAX_VALUE;

		for (ModelElement element1: surface.getElements()) {
			if (element1.getId()<minId) minId=element1.getId();
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2.getId()<minId) minId=element2.getId();
			}
		}

		return (minId==Integer.MAX_VALUE)?0:minId;
	}

	/**
	 * Konfiguriert das {@link #resultsInfo}-Element
	 * @param htmlColor	Zu verwendende Farbe (in html-Notation)
	 * @param text	Auszugebender Text
	 * @see #resultsInfo
	 * @see #buildIDsList()
	 */
	private void setInfo(final String htmlColor, final String text) {
		resultsInfo.setText("<html><body><span style=\"color: "+htmlColor+"\">"+text+"</span></body></html>");
	}

	/**
	 * Prüft, ob eine Station zu den Sucheingaben passt.
	 * @param element	Zu prüfende Station
	 * @param search	Suchtext
	 * @param caseSensitive	Groß- und Kleinschreibung berücksichtigen?
	 * @param fullMatchOnly	Nur vollständige Übereinstimmungen als Treffer zählen
	 * @return	Liefert <code>true</code>, wenn die Station zu den Sucheingaben passt
	 * @see #buildIDsList()
	 */
	private boolean testName(final ModelElement element, final String search, final boolean caseSensitive, final boolean fullMatchOnly) {
		if (caseSensitive) {
			if (fullMatchOnly) {
				if (element.getName().equals(search)) return true;
				if (element.getContextMenuElementName().equals(search)) return true;
			} else {
				if (element.getName().contains(search)) return true;
				if (element.getContextMenuElementName().contains(search)) return true;
			}
		} else {
			final String searchLower=search.toLowerCase();
			if (fullMatchOnly) {
				if (element.getName().toLowerCase().equals(searchLower)) return true;
				if (element.getContextMenuElementName().toLowerCase().equals(searchLower)) return true;
			} else {
				if (element.getName().toLowerCase().contains(searchLower)) return true;
				if (element.getContextMenuElementName().toLowerCase().contains(searchLower)) return true;
			}
		}

		return false;
	}

	/**
	 * Prüft, ob eine Station zu dem eingegebenen regulären Ausdruck passt.
	 * @param element	Zu prüfende Station
	 * @param regexPattern	Regulärer Suchausdruck
	 * @param fullMatchOnly	Nur vollständige Übereinstimmungen als Treffer zählen
	 * @return	Liefert <code>true</code>, wenn die Station zu den Sucheingaben passt
	 */
	private boolean testNameRegEx(final ModelElement element, final Pattern regexPattern, final boolean fullMatchOnly) {
		Matcher matcher;

		matcher=regexPattern.matcher(element.getName());
		if (fullMatchOnly) {
			if (matcher.matches()) return true;
		} else {
			if (matcher.find()) return true;
		}

		matcher=regexPattern.matcher(element.getContextMenuElementName());
		if (fullMatchOnly) {
			if (matcher.matches()) return true;
		} else {
			if (matcher.find()) return true;
		}

		return false;
	}

	/**
	 * Erstellt eine Liste mit allen in dem Modell auftretenden IDs.
	 * @see #resultsIds
	 */
	private void buildIDsList() {
		resultsIds.clear();

		final String search=searchEdit.getText().trim();
		if (search.isEmpty()) {
			setInfo("red",Language.tr("FindElementDirect.NoSearchStringEntered"));
			return;
		}
		final boolean caseSensitive=optionCaseSensitive.isSelected();
		final boolean fullMatchOnly=optionFullMatchOnly.isSelected();

		/* Suche nach IDs */

		if (optionsCombo.getSelectedIndex()==0 || optionsCombo.getSelectedIndex()==2) {
			final Integer I=NumberTools.getNotNegativeInteger(search);
			if (I==null) {
				if (optionsCombo.getSelectedIndex()==0) {
					setInfo("red",String.format(Language.tr("FindElementDirect.InvalidNumber.Info"),search));
					return;
				}
			} else {
				ModelElement element=surface.getByIdIncludingSubModels(I.intValue());
				if (!includeHidden.isSelected() && !surface.isVisibleOnLayer(element)) element=null;
				if (element==null) {
					if (optionsCombo.getSelectedIndex()==0) {
						setInfo("red",String.format(Language.tr("FindElementDirect.UnknownId.Info"),I.intValue()));
						return;
					}
				} else {
					resultsIds.add(I);
				}
			}
		}

		/* Suche nach Namen */

		if (optionsCombo.getSelectedIndex()==1 || optionsCombo.getSelectedIndex()==2) {
			if (optionRegEx.isSelected()) {
				/* Regulärer Ausdruck */
				int flags=0;
				if (!caseSensitive) flags+=Pattern.CASE_INSENSITIVE;
				Pattern regexPattern;
				try {
					regexPattern=Pattern.compile(search,flags);
				} catch (PatternSyntaxException e) {
					regexPattern=null;
				}
				if (regexPattern!=null) for (ModelElement element1: surface.getElements()) {
					if (!includeHidden.isSelected() && !surface.isVisibleOnLayer(element1)) continue;
					if (!resultsIds.contains(element1.getId()) && testNameRegEx(element1,regexPattern,fullMatchOnly)) resultsIds.add(element1.getId());
					if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
						if (!includeHidden.isSelected() && !((ModelElementSub)element1).getSubSurface().isVisibleOnLayer(element2)) continue;
						if (!resultsIds.contains(element2.getId()) && testNameRegEx(element2,regexPattern,fullMatchOnly)) resultsIds.add(element2.getId());
					}
				}
			} else {
				/* Normale Suche */
				for (ModelElement element1: surface.getElements()) {
					if (!includeHidden.isSelected() && !surface.isVisibleOnLayer(element1)) continue;
					if (!resultsIds.contains(element1.getId()) && testName(element1,search,caseSensitive,fullMatchOnly)) resultsIds.add(element1.getId());
					if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
						if (!includeHidden.isSelected() && !((ModelElementSub)element1).getSubSurface().isVisibleOnLayer(element2)) continue;
						if (!resultsIds.contains(element2.getId()) && testName(element2,search,caseSensitive,fullMatchOnly)) resultsIds.add(element2.getId());
					}
				}
			}
		}

		/* Info ausgeben */

		if (resultsIds.isEmpty()) {
			setInfo("red",Language.tr("FindElementDirect.NoElementsFound"));
		} else {
			if (resultsIds.size()==1) {
				setInfo("green",Language.tr("FindElementDirect.OneElementFound"));
			} else {
				setInfo("green",String.format(Language.tr("FindElementDirect.ElementsFound"),resultsIds.size()));
			}
		}
	}

	/**
	 * Führt die Suche aus.
	 * @see #searchEdit
	 */
	private void search() {
		buildIDsList();

		resultsModel.clear();
		for (Integer id: resultsIds) resultsModel.addElement(ElementRendererTools.getRecord(surface,id));
		resultsList.setModel(resultsModel);
	}

	/**
	 * Liefert die ID des ausgewählten Elements bezogen auf die Hauptebene, d.h.
	 * bei Kind-Elementen in einem Untermodell die ID des Untermodell-Elements.
	 * Wenn nichts ausgewählt ist, wird -1 zurückgeliefert
	 * @return	ID des ausgewählten Elements oder -1, wenn nichts gewählt ist
	 */
	public int getSelectedId() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return -1;
		if (resultsList.getSelectedIndex()<0) return -1;
		final int selectedID=resultsIds.get(resultsList.getSelectedIndex());

		for (ModelElement element: surface.getElements()) {
			if (element.getId()==selectedID) return selectedID;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (sub.getId()==selectedID) return element.getId();
			}
		}

		return -1;
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		openFindAndReplaceDialog=true;
		close(BaseDialog.CLOSED_BY_OK);
	}

	/**
	 * Soll nach dem Schließen dieses Dialog der Volltextsuche-Dialog geöffnet werden?
	 * @return	Volltextsuche-Dialog öffnen?
	 */
	public boolean isOpenFindAndReplaceDialog() {
		return openFindAndReplaceDialog;
	}
}
