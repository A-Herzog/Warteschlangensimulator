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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.swing.PlugableFileChooser;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;

/**
 * Zeigt aktuelle Animations-/Simulationsdaten zu einer bestimmten Station an
 * @author Alexander Herzog
 * @see ModelElement
 */
public class ModelElementAnimationInfoDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2428681388058860543L;

	/** Timer für automatische Aktualisierungen */
	private Timer timer;
	/** Simulationsmodell mit Informationen zu den Stationen usw. */
	private final RunModel model;
	/** Benutzerdefinierte Animationsicons */
	private final ModelAnimationImages modelImages;

	/** Tabs über den Bereichen */
	private final JTabbedPane tabs;
	/** Anzuzeigender Text im Content-Bereich */
	private final Supplier<String> info;
	/** Liste mit an der Station wartenden Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientWaitingInfo;
	/** Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientInfo;
	/** Ermöglicht das Abrufen eines tatsächlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden möglich ist) */
	private final Function<Long,RunDataClient> getRealClient;
	/** Schaltfläche "Aktualisieren" */
	private final JButton buttonUpdate;

	/** Schaltfläche zum Umschalten zwischen automatischer und manueller Aktualisierung */
	private final JButton buttonAutoUpdate;
	/** Schaltfläche zum Aktualisierung der Kundenliste */
	private final JButton buttonUpdateClients;

	/** Objekt welches die Icons für die Animation vorhält */
	private final AnimationImageSource images;

	/** Text-Ausgabebereich */
	private final JTextArea textArea;

	/** Liste der wartenden Kunden */
	private final JList<ClientInfo> listWaiting;
	/** Infozeile zu {@link #listWaiting} */
	private final JLabel listWaitingInfo;

	/** Liste der Kunden */
	private final JList<ClientInfo> listAll;
	/** InfoZeile zu {@link #listAll} */
	private final JLabel listAllInfo;

	/** Handelt es sich um den ersten Aufruf des "Alle Kunden"-Tabs? */
	private boolean firstAllClientsTabCall=true;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationInfoDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Anzuzeigender Titel	 *
	 * @param model	Simulationsmodell mit Informationen zu den Stationen usw.
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param info	Anzuzeigender Text im Content-Bereich
	 * @param clientWaitingInfo	Optionale Liste mit an der Station wartenden Kunden (kann <code>null</code> sein)
	 * @param clientInfo	Optionale Liste mit an der Station befindlichen Kunden (kann <code>null</code> sein)
	 * @param getRealClient	Ermöglicht das Abrufen eines tatsächlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden möglich ist)
	 */
	@SuppressWarnings("unchecked")
	public ModelElementAnimationInfoDialog(final Component owner, final String title, final RunModel model, final ModelAnimationImages modelImages, final Supplier<String> info, final Supplier<List<ClientInfo>> clientWaitingInfo, final Supplier<List<ClientInfo>> clientInfo, final Function<Long,RunDataClient> getRealClient) {
		super(owner,title);
		this.model=model;
		this.modelImages=modelImages;
		this.info=info;
		this.clientWaitingInfo=clientWaitingInfo;
		this.clientInfo=clientInfo;
		this.getRealClient=getRealClient;

		images=new AnimationImageSource();
		timer=null;

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(ModelElementAnimationInfoDialog.this.owner,"AnimationStatistics"));
		content.setLayout(new BorderLayout());

		/* Größe für Listeneinträge */
		final JLabel defaultSizeLabel=new JLabel("<html><body>Line1<br>Line2<br>Line3</body></html>");
		defaultSizeLabel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		/* Toolbar */
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		addButton(toolbar,Language.tr("Dialog.Button.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.CopyHint"),e->commandCopy());
		addButton(toolbar,Language.tr("Dialog.Button.Save"),Images.GENERAL_SAVE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveHint"),e->commandSave());
		buttonUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
		buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());
		buttonUpdateClients=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateClients"),Images.MODELPROPERTIES_CLIENTS.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateClientsHint"),e->commandUpdateClientList());
		buttonUpdateClients.setVisible(false);

		/* Tabs */
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		JPanel tab;
		Object[] data;

		/* Text-Datenfeld */
		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.Data"),tab=new JPanel(new BorderLayout()));
		textArea=new JTextArea(info.get());
		tab.add(new JScrollPane(textArea),BorderLayout.CENTER);
		textArea.setEditable(false);

		/* Warteschlange */
		if (clientWaitingInfo==null) {
			listWaiting=null;
			listWaitingInfo=null;
		} else {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients"),tab=new JPanel(new BorderLayout()));
			data=buildList(tab,true);
			listWaiting=(JList<ClientInfo>)data[0];
			listWaitingInfo=(JLabel)data[1];
			commandUpdateWaitingClientList();
		}

		/* Allgemeine Kundenliste */
		if (clientInfo==null) {
			listAll=null;
			listAllInfo=null;
		} else {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients"),tab=new JPanel(new BorderLayout()));
			data=buildList(tab,false);
			listAll=(JList<ClientInfo>)data[0];
			listAllInfo=(JLabel)data[1];
		}

		/* Icons auf den Tabs */
		int nr=0;
		tabs.setIconAt(nr++,Images.GENERAL_TABLE.getIcon());
		if (clientWaitingInfo!=null) tabs.setIconAt(nr++,Images.EXTRAS_QUEUE.getIcon());
		if (clientInfo!=null) tabs.setIconAt(nr++,Images.MODELPROPERTIES_CLIENTS.getIcon());

		/* Dialog starten */
		tabs.addChangeListener(e->{
			final int index=tabs.getSelectedIndex();
			final boolean isAllClientsTab=(this.clientInfo!=null && index==tabs.getTabCount()-1);
			buttonUpdate.setVisible(!isAllClientsTab);
			buttonAutoUpdate.setVisible(!isAllClientsTab);
			buttonUpdateClients.setVisible(isAllClientsTab);
			if (isAllClientsTab && getRealClient!=null && firstAllClientsTabCall) {
				firstAllClientsTabCall=false;
				commandUpdateClientList();
			}
		});
		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(800,600);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Fügt eine Kundenliste in ein Panel ein
	 * @param parent	Übergeordnetes Panel
	 * @param isWaitingClientsList	Handelt es sich um wartende Kunden?
	 * @return	2-elementiges Array aus Liste und aus Label mit weiteren Informationen unter der Liste
	 */
	private Object[] buildList(final JPanel parent, final boolean isWaitingClientsList) {
		/* Neue Liste */
		final JList<ClientInfo> list;
		final JLabel info;

		/* Anlegen und einfügen */
		parent.add(new JScrollPane(list=new JList<>(new DefaultListModel<ClientInfo>())),BorderLayout.CENTER);
		list.setCellRenderer(new JClientInfoRender(images,isWaitingClientsList));
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					if (getRealClient!=null && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
						commandEditClientData(list);
					} else {
						commandShowClientData(list);
					}
				}
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					if (getRealClient!=null && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
						commandEditClientData(list);
					} else {
						commandShowClientData(list);
					}
					e.consume();
					return;
				}
			}
		});
		list.setPrototypeCellValue(new ClientInfo(null,null,new RunDataClient(0,false,false,0)));
		final JPanel line;
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(info=new JLabel());

		return new Object[] {list,info};
	}

	/**
	 * Erstellt eine neue Schaltfläche und fügt sie zur Symbolleiste hinzu.
	 * @param toolbar	Symbolleiste auf der die neue Schaltfläche eingefügt werden soll
	 * @param name	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param icon	Optionales Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @param listener	Aktion die beim Anklicken der Schaltfläche ausgeführt werden soll
	 * @return	Neue Schaltfläche (ist bereits in die Symbolleiste eingefügt)
	 */
	private JButton addButton(final JToolBar toolbar, final String name, final Icon icon, final String hint, final ActionListener listener) {
		final JButton button=new JButton(name);
		if (icon!=null) button.setIcon(icon);
		if (hint!=null && !hint.isBlank()) button.setToolTipText(hint);
		button.addActionListener(listener);
		toolbar.add(button);
		return button;
	}

	/**
	 * Welches Tab ist momentan aktiv?
	 */
	private enum ActiveTab {
		/** Tab: Allgemeine Informationen */
		INFO,
		/** Tab: Liste der wartenden Kunden */
		QUEUE,
		/** Tab: Liste aller Kunden */
		CLIENTS,
	}

	/**
	 * Liefert die Bezeichnung des aktiven Tabs.
	 * @return	Bezeichnung des aktiven Tabs
	 * @see ActiveTab
	 */
	private ActiveTab getActiveTab() {
		if (tabs.getTabCount()==2) {
			switch (tabs.getSelectedIndex()) {
			case 0: return ActiveTab.INFO;
			case 1: return ActiveTab.CLIENTS;
			}
		} else {
			switch (tabs.getSelectedIndex()) {
			case 0: return ActiveTab.INFO;
			case 1: return ActiveTab.QUEUE;
			case 2: return ActiveTab.CLIENTS;
			}
		}
		return ActiveTab.INFO;
	}

	/**
	 * Erstellt eine Kundendatentabelle für den Export.
	 * @param list	Kundeliste
	 * @return	Kundentabelle
	 */
	public static Table buildTable(final List<ClientInfo> list) {
		final Table table=new Table();

		final Set<Integer> clientDataKeys=new HashSet<>();
		final Set<String> clientTextDataKeys=new HashSet<>();
		for (ClientInfo client: list) {
			clientDataKeys.addAll(client.clientData.keySet());
			clientTextDataKeys.addAll(client.clientTextData.keySet());
		}
		final ArrayList<Integer> clientDataKeysList=new ArrayList<>(clientDataKeys);
		clientDataKeysList.sort(null);
		final ArrayList<String> clientTextDataKeysList=new ArrayList<>(clientTextDataKeys);
		clientTextDataKeysList.sort(null);

		final List<String> heading=new ArrayList<>();
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.RunningNumber"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ID"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ClientType"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.IsWarmUp"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.InStatistics"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.BatchSize"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.CurrentStation"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Waiting"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Transfer"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Process"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Residence"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Waiting"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Transfer"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Process"));
		heading.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Path"));
		for (Integer key: clientDataKeysList) heading.add("ClientData("+key+")");
		for (String key: clientTextDataKeysList) heading.add("ClientData(\""+key+"\")");
		table.addLine(heading);

		for (ClientInfo client: list) {
			final List<String> row=new ArrayList<>();
			row.add(""+client.number);
			row.add(""+client.id);
			row.add(client.typeName);
			row.add(client.isWarmUp?Language.tr("Dialog.Button.Yes"):Language.tr("Dialog.Button.No"));
			row.add(client.inStatistics?Language.tr("Dialog.Button.Yes"):Language.tr("Dialog.Button.No"));
			row.add(""+client.batch);
			row.add(""+client.currentPosition);
			row.add(NumberTools.formatNumberMax(client.waitingTime));
			row.add(NumberTools.formatNumberMax(client.transferTime));
			row.add(NumberTools.formatNumberMax(client.processTime));
			row.add(NumberTools.formatNumberMax(client.residenceTime));
			row.add(NumberTools.formatNumberMax(client.waitingCosts));
			row.add(NumberTools.formatNumberMax(client.transferCosts));
			row.add(NumberTools.formatNumberMax(client.processCosts));
			if (client.path==null) {
				row.add(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Path.notRecorded"));
			} else {
				row.add(String.join(" -> ",Arrays.stream(client.path).mapToObj(i->""+i).toArray(String[]::new)));
			}
			for (Integer key: clientDataKeysList) {
				final Double D=client.clientData.get(key);
				if (D==null) row.add("0"); else row.add(NumberTools.formatNumberMax(D));
			}
			for (String key: clientTextDataKeysList) {
				final String value=client.clientTextData.get(key);
				if (value==null) row.add(""); else row.add(value);
			}
			table.addLine(row);
		}

		return table;
	}

	/**
	 * Befehl: Angezeigte Daten in die Zwischenablage kopieren
	 */
	private void commandCopy() {
		if (buttonAutoUpdate.isSelected()) commandAutoUpdate();

		String result=null;
		switch (getActiveTab()) {
		case INFO:
			result=textArea.getText();
			break;
		case QUEUE:
			result=buildTable(clientWaitingInfo.get()).toString();
			break;
		case CLIENTS:
			result=buildTable(clientInfo.get()).toString();
			break;
		}

		if (result!=null) {
			getToolkit().getSystemClipboard().setContents(new StringSelection(result),null);
		}
	}

	/**
	 * Befehl: Angezeigte Daten als Text speichern
	 */
	private void commandSave() {
		if (buttonAutoUpdate.isSelected()) commandAutoUpdate();

		switch (getActiveTab()) {
		case INFO:
			final var fc=new PlugableFileChooser(true);
			fc.setDialogTitle(Language.tr("FileType.Save.Text"));
			fc.addChoosableFileFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
			fc.setFileFilter("txt");
			fc.setAcceptAllFileFilterUsed(false);
			final File file=fc.showSaveDialogFileWithExtension(owner);
			if (file==null) return;

			if (file.exists()) {
				if (!MsgBox.confirmOverwrite(owner,file)) return;
			}

			try {
				if (file.isFile()) {
					if (!file.delete()) {
						MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
						return;
					}
				}
				Files.write(Paths.get(file.toURI()),textArea.getText().getBytes(),StandardOpenOption.CREATE_NEW);
			} catch (IOException e1) {
				MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
			}
			break;
		case QUEUE:
			final File queueFile=Table.showSaveDialog(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveWaitingClients"));
			if (queueFile==null) return;
			if (queueFile.exists()) {
				if (!MsgBox.confirmOverwrite(owner,queueFile)) return;
			}
			final Table queueTable=buildTable(clientWaitingInfo.get());
			if (!queueTable.save(queueFile)) {
				MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveWaitingClients.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveWaitingClients.ErrorInfo"),queueFile.toString()));
			}
			break;
		case CLIENTS:
			final File clientFile=Table.showSaveDialog(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveClients"));
			final Table clientTable=buildTable(clientInfo.get());
			if (clientFile==null) return;
			if (clientFile.exists()) {
				if (!MsgBox.confirmOverwrite(owner,clientFile)) return;
			}
			if (!clientTable.save(clientFile)) {
				MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveClients.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveClients.ErrorInfo"),clientFile.toString()));
			}
			break;
		}
	}

	/**
	 * Befehl: Anzeige aktualisieren
	 */
	private void commandUpdate() {
		/* Text-Datenfeld */
		textArea.setText(info.get());
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);

		/* Kundenliste */
		commandUpdateWaitingClientList();
	}

	/**
	 * Befehl: Liste der wartenden Kunden aktualisieren
	 */
	private void commandUpdateWaitingClientList() {
		if (clientWaitingInfo==null) return;

		final int index=listWaiting.getSelectedIndex();

		final DefaultListModel<ClientInfo> model=new DefaultListModel<>();
		new Thread(()->{
			final List<ClientInfo> list=clientWaitingInfo.get();
			if (list!=null) list.forEach(model::addElement);
			SwingUtilities.invokeLater(()->{
				listWaiting.setModel(model);
				if (index>=0 && index<model.size()) listWaiting.setSelectedIndex(index);

				if (listWaitingInfo!=null) {
					final int size=model.size();
					listWaitingInfo.setText(String.format((size==1)?Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Info.Singular"):Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Info.Plural"),size));
				}
			});
		},"ProcessWaitingClientsList").start();
	}

	/**
	 * Befehl: Liste der Kunden aktualisieren
	 */
	private void commandUpdateClientList() {
		if (clientInfo==null) return;

		final int index=listAll.getSelectedIndex();

		final DefaultListModel<ClientInfo> model=new DefaultListModel<>();
		new Thread(()->{
			final List<ClientInfo> list=clientInfo.get();
			if (list!=null) list.forEach(model::addElement);
			SwingUtilities.invokeLater(()->{
				listAll.setModel(model);
				if (index>=0 && index<model.size()) listAll.setSelectedIndex(index);

				if (listAllInfo!=null) {
					final int size=model.size();
					listAllInfo.setText(String.format((size==1)?Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients.Info.Singular"):Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients.Info.Plural"),size));
				}
			});
		},"ProcessAllClientsList").start();
	}

	/**
	 * Timer-Task zur Aktualisierung der Daten
	 * @see ModelElementAnimationInfoDialog#commandAutoUpdate()
	 */
	private class UpdateTimerTask extends TimerTask {
		/**
		 * Konstruktor der Klasse
		 */
		public UpdateTimerTask() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			if (buttonAutoUpdate.isSelected()) SwingUtilities.invokeLater(()->{
				final boolean isAllClientsTab=(clientInfo!=null && tabs.getSelectedIndex()==tabs.getTabCount()-1);
				if (!isAllClientsTab) commandUpdate();
				scheduleNextUpdate();
			});
		}
	}

	/**
	 * Befehl: Anzeige automatisch aktualisieren (an/aus)
	 */
	private void commandAutoUpdate() {
		buttonAutoUpdate.setSelected(!buttonAutoUpdate.isSelected());

		if (buttonAutoUpdate.isSelected()) {
			timer=new Timer("SimulationDataUpdate");
			scheduleNextUpdate();
		} else {
			if (timer!=null) {timer.cancel(); timer=null;}
		}
	}

	/**
	 * Plant den nächsten Update-Schritt ein.
	 * @see #commandAutoUpdate()
	 * @see UpdateTimerTask
	 */
	private void scheduleNextUpdate() {
		if (timer!=null) timer.schedule(new UpdateTimerTask(),250);
	}

	/**
	 * Befehl: Daten zu dem gewählten (wartenden) Kunden anzeigen
	 * @param list	Liste aus der der aktuell gewählte Kunde angezeigt werden soll
	 */
	private void commandShowClientData(final JList<ClientInfo> list) {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=list.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final ModelElementAnimationInfoClientDialog infoDialog=new ModelElementAnimationInfoClientDialog(this,model,clientInfo,list==listWaiting,getRealClient!=null);
		if (!infoDialog.getShowEditorDialog()) return;

		commandEditClientData(list);
	}

	/**
	 * Befehl: Editor zu dem gewählten (wartenden) Kunden anzeigen
	 * @param list	Liste aus der der aktuell gewählte Kunde bearbeitet werden soll
	 */
	private void commandEditClientData(final JList<ClientInfo> list) {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=list.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final RunDataClient client=getRealClient.apply(clientInfo.number);
		if (client==null) {
			MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient.Error"));
			return;
		}

		final ModelElementAnimationEditClientDialog editDialog=new ModelElementAnimationEditClientDialog(this,modelImages,model,client);
		if (editDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (list==listWaiting) commandUpdateWaitingClientList();
			if (list==listAll) commandUpdateClientList();
		}
	}

	@Override
	protected boolean closeButtonOK() {
		if (timer!=null) {timer.cancel(); timer=null;}
		return true;
	}

	/**
	 * Datensatz für die Informationen zu einem Kunden
	 */
	public static class ClientInfo {
		/** Benutzerdefinierte Animationsicons */
		private final ModelAnimationImages animationImages;

		/** ID des Kunden */
		public final long id;
		/** Fortlaufende Nummer des Kunden */
		public final long number;
		/** Nummer des Kundentyps */
		public final int typeId;
		/** Name des Kundentyps */
		public final String typeName;
		/** Icon für den Kunden */
		public final String icon;
		/** Wurde dieser Kunde in der Warm-up-Phase erstellt? */
		public boolean isWarmUp;
		/** Sollen die Daten zu diesem Kunden in der Statistik erfasst werden? */
		public boolean inStatistics;
		/** Anzahl an Kunden in dem Batch (0, wenn dieser Kunde kein temporärer Batch ist) */
		public final int batch;
		/** Kundentypnamen der Kunden in dem Batch (oder <code>null</code>, wenn dieser Kunde kein temporärer Batch ist) */
		public final String[] batchTypeNames;
		/** ID der Station an der sich der Kunde momentan befindet */
		public final int currentPosition;
		/** Bisherige Wartezeit des Kunden */
		public final double waitingTime;
		/** Bisherige Transferzeit des Kunden */
		public final double transferTime;
		/** Bisherige Bedienzeit des Kunden */
		public final double processTime;
		/** Bisherige Verweilzeit des Kunden */
		public final double residenceTime;
		/** Wartezeit-Kosten */
		public final double waitingCosts;
		/** Transferzeit-Kosten */
		public final double transferCosts;
		/** Bedienzeit-Kosten */
		public final double processCosts;
		/** Numerische Kundendatenfelder */
		public final Map<Integer,Double> clientData;
		/** Text-basierte Kundendatenfelder */
		public final Map<String,String> clientTextData;
		/** Aufgezeichneter Pfad (kann <code>null</code> sein, wenn kein Pfad aufgezeichnet wurde) */
		public final int[] path;

		/**
		 * Konstruktor der Klasse
		 * @param animationImages	Benutzerdefinierte Animationsicons
		 * @param model	Statisches Simulationsdatenmodell
		 * @param client	Kundendatenobjekt dem die Informationen entnommen werden sollen
		 */
		public ClientInfo(final ModelAnimationImages animationImages, final RunModel model, final RunDataClient client)  {
			this.animationImages=animationImages;

			id=client.hashCode();
			number=client.clientNumber;

			typeId=client.type;
			typeName=(model==null)?"A":model.clientTypes[client.type];
			icon=(client.icon==null)?"":client.icon;

			isWarmUp=client.isWarmUp;
			inStatistics=client.inStatistics;

			final List<RunDataClient> batchClients=client.getBatchData();
			if (batchClients==null) {
				batch=0;
				batchTypeNames=null;
			} else {
				batch=batchClients.size();
				batchTypeNames=new String[batch];
				for (int i=0;i<batch;i++) batchTypeNames[i]=(model==null)?"A":model.clientTypes[batchClients.get(i).type];
			}

			currentPosition=client.nextStationID;

			final double scaleToSeconds=(model==null)?(1.0/1000.0):model.scaleToSeconds;
			waitingTime=client.waitingTime*scaleToSeconds;
			transferTime=client.transferTime*scaleToSeconds;
			processTime=client.processTime*scaleToSeconds;
			residenceTime=client.residenceTime*scaleToSeconds;

			waitingCosts=client.waitingAdditionalCosts;
			transferCosts=client.transferAdditionalCosts;
			processCosts=client.processAdditionalCosts;

			clientData=new HashMap<>();
			for (int i=0;i<=client.getMaxUserDataIndex();i++) {
				final double value=client.getUserData(i);
				if (value!=0.0) clientData.put(i,value);
			}

			clientTextData=new HashMap<>();
			for (String key: client.getUserDataStringKeys()) clientTextData.put(key,client.getUserDataString(key));

			path=client.getPath();
		}

		/**
		 * Liefert den in dem Label anzuzeigenden Text
		 * @param isWaitingClientsList	Handelt es sich um einen noch wartenden Kunden?
		 * @return	In dem Label anzuzeigenden HTML-formatierter Text
		 * @see #buildLabel(AnimationImageSource, boolean)
		 */
		public String getLabelText(final boolean isWaitingClientsList) {
			final StringBuilder text=new StringBuilder();

			text.append("<html><body>");

			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.RunningNumber"));
			text.append(": <b>");
			text.append(number);
			text.append("</b>, ");
			text.append("id=<b>");
			text.append(id);
			text.append("</b>, ");
			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ClientType"));
			text.append(": <b>");
			text.append(typeName);
			text.append("</b> (id=");
			text.append(typeId);
			text.append(")<br>\n");
			text.append("w=");
			text.append(TimeTools.formatExactTime(waitingTime));
			text.append(", t=");
			text.append(TimeTools.formatExactTime(transferTime));
			text.append(", p=");
			text.append(TimeTools.formatExactTime(processTime));
			text.append(", v=");
			text.append(TimeTools.formatExactTime(residenceTime));
			if (isWaitingClientsList) {
				text.append(" (");
				text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"));
				text.append(")");
			}
			text.append("<br>\n");

			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Number")+": ");
			text.append(clientData.size());
			text.append(", ");
			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Text")+": ");
			text.append(clientTextData.size());

			text.append("</body></html>\n");

			return text.toString();
		}

		/**
		 * Erzeugt ein Label zur Anzeige in der Liste zu diesem Kundeninfo-Datensatz
		 * @param images	Objekt welches die Icons für die Animation vorhält
		 * @param isWaitingClientsList	Handelt es sich um einen noch wartenden Kunden?
		 * @return	Label zur Anzeige in der Liste
		 */
		public JLabel buildLabel(final AnimationImageSource images, final boolean isWaitingClientsList) {
			final JLabel label=new JLabel(getLabelText(isWaitingClientsList),getIcon(images),SwingConstants.LEADING);
			label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			return label;
		}

		/**
		 * Liefert das Icon für den Kunden.
		 * @param images	Objekt welches die Icons für die Animation vorhält
		 * @return	Icon für den Kunden
		 */
		public Icon getIcon(final AnimationImageSource images) {
			final BufferedImage image=images.get(icon,animationImages,24,1);
			return (image==null)?null:new ImageIcon(image);
		}

		/**
		 * Wandelt eine Liste mit Simulationsdaten-Kunden in Kundeninfo-Objekte um
		 * @param animationImages	Benutzerdefinierte Animationsicons
		 * @param model	Statisches Simulationsdatenmodell
		 * @param clients	Liste der umzuwandelnden Simulationsdaten-Kunden
		 * @return	Liste mit Kundeninfo-Objekten
		 */
		public static List<ClientInfo> getList(final ModelAnimationImages animationImages, final RunModel model, final List<RunDataClient> clients) {
			if (clients==null) return new ArrayList<>();
			return clients.stream().filter(client->client!=null).map(client->new ClientInfo(animationImages,model,client)).collect(Collectors.toList());
		}
	}

	/**
	 * Renderer für die Einträge der Liste der (wartenden) Kunden
	 * @see ModelElementAnimationInfoDialog#listWaiting
	 * @see ModelElementAnimationInfoDialog#listAll
	 */
	public static class JClientInfoRender implements ListCellRenderer<ClientInfo> {
		/** Objekt welches die Icons für die Animation vorhält */
		private final AnimationImageSource images;
		/** Handelt es sich um wartende Kunden? */
		private final boolean isWaitingClientsList;

		/**
		 * Konstruktor der Klasse
		 * @param images	Objekt welches die Icons für die Animation vorhält
		 * @param isWaitingClientsList	Handelt es sich um wartende Kunden?
		 */
		public JClientInfoRender(final AnimationImageSource images, final boolean isWaitingClientsList) {
			this.images=images;
			this.isWaitingClientsList=isWaitingClientsList;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends ClientInfo> list, ClientInfo value, int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel label=value.buildLabel(images,isWaitingClientsList);

			if (isSelected) {
				label.setBackground(list.getSelectionBackground());
				label.setForeground(list.getSelectionForeground());
				label.setOpaque(true);
			} else {
				label.setBackground(list.getBackground());
				label.setForeground(list.getForeground());
				label.setOpaque(false);
			}
			return label;
		}
	}
}