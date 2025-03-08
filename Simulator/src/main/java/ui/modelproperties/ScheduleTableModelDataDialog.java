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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import language.Language;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelSchedule;
import ui.tools.WindowSizeStorage;

/**
 * Dieser Dialog erlaubt das grafische Bearbeiten eines Zeitplans.
 * @author Alexander Herzog
 * @see ModelSchedule
 * @see ScheduleTableModelDataDialog
 */
public class ScheduleTableModelDataDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2493379995138787054L;

	/** Hilfe-Callback */
	private final Runnable help;

	/** Im Konstruktor übergebenes {@link ModelSchedule}-Objekt in das beim Schließen des Dialogs die Daten zurückgeschrieben werden */
	private ModelSchedule originalSchedule;
	/** Anzahl an Sekunden, die ein Zeitslot dauern soll */
	private int durationPerSlot;
	/**  Maximaler y-Achsen-Wert im Editor */
	private int editorMaxY;
	/**  Wie soll der Plan nach seinem Ende fortgesetzt werden? */
	private ModelSchedule.RepeatMode repeatMode;

	/** Schaltfläche "Anfang" */
	private final JButton buttonHome;
	/** Schaltfläche "Zurück" */
	private final JButton buttonLeft;
	/** Panel in dem die Zeitslots des Zeitplans als Balken dargestellt werden */
	private final SchedulePanel schedulePanel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param schedule	Zu bearbeitender Zeitplan
	 */
	public ScheduleTableModelDataDialog(final Component owner, final Runnable help, final ModelSchedule schedule) {
		super(owner,String.format(Language.tr("Schedule.EditDialog.Title"),schedule.getName()));
		this.help=help;
		originalSchedule=schedule;
		durationPerSlot=schedule.getDurationPerSlot();
		editorMaxY=schedule.getEditorMaxY();
		editorMaxY=Math.max(editorMaxY,schedule.getSlots().stream().mapToInt(Integer::intValue).max().orElseGet(()->0));
		repeatMode=schedule.getRepeatMode();

		/* GUI */
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		/* Zeitslot-Editor */
		content.add(schedulePanel=new SchedulePanel(schedule.getSlots(),editorMaxY,durationPerSlot,40),BorderLayout.CENTER);
		schedulePanel.addStartPositionChanged(()->enableButtons());

		/* Toolbar */
		final JToolBar toolBar=new JToolBar();
		toolBar.setFloatable(false);
		content.add(toolBar,BorderLayout.NORTH);

		toolBar.add(buttonHome=getButton(Language.tr("Schedule.EditDialog.ToTheStart"),Language.tr("Schedule.EditDialog.ToTheStart.Hint"),Images.GENERAL_HOME,(button,e)->schedulePanel.setStartPosition(0)));
		toolBar.add(buttonLeft=getButton(Language.tr("Schedule.EditDialog.TimeStepBack"),Language.tr("Schedule.EditDialog.TimeStepBack.Hint"),Images.ARROW_LEFT_SHORT,(button,e)->{
			final int delta=((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0)?Math.max(1,86400/durationPerSlot):1;
			final int startSlot=schedulePanel.getStartPosition();
			schedulePanel.setStartPosition(startSlot-delta);
		}));
		toolBar.add(getButton(Language.tr("Schedule.EditDialog.TimeStepFurther"),Language.tr("Schedule.EditDialog.TimeStepFurther.Hint"),Images.ARROW_RIGHT_SHORT,(button,e)->{
			final int delta=((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0)?Math.max(1,86400/durationPerSlot):1;
			final int startSlot=schedulePanel.getStartPosition();
			schedulePanel.setStartPosition(startSlot+delta);
		}));
		toolBar.add(getButton(Language.tr("Schedule.EditDialog.TimeStepEnd"),Language.tr("Schedule.EditDialog.TimeStepEnd.Hint"),Images.ARROW_RIGHT,(button,e)->{
			final int slotCount=schedulePanel.getDisplaySlots();
			final int lastUsedSlot=schedulePanel.getLastNonNullSlot();
			schedulePanel.setStartPosition(Math.max(0,lastUsedSlot-slotCount+1));
		}));

		toolBar.addSeparator();

		toolBar.add(getButton(Language.tr("Schedule.EditDialog.Settings"),Language.tr("Schedule.EditDialog.Settings.Hint"),Images.GENERAL_SETUP,(button,e)->commandSettingsDialog()));
		toolBar.add(getButton(Language.tr("Schedule.EditDialog.Range"),Language.tr("Schedule.EditDialog.Range.Hint"),Images.MODELPROPERTIES_SCHEDULES,(button,e)->commandRangeDialog(button)));

		toolBar.addSeparator();

		toolBar.add(getButton(Language.tr("Schedule.EditDialog.Load"),Language.tr("Schedule.EditDialog.Load.Hint"),Images.GENERAL_SELECT_FILE,(button,e)->commandPopupLoad(button)));
		toolBar.add(getButton(Language.tr("Schedule.EditDialog.Save"),Language.tr("Schedule.EditDialog.Save.Hint"),Images.GENERAL_SAVE,(button,e)->commandPopupSave(button)));

		enableButtons();

		/* Info */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.SOUTH);
		line.add(new JLabel("<html><body>"+Language.tr("Schedule.EditDialog.EditInfo")+"</body></html>"));

		/* Drag & Drop einrichten */
		new FileDropper(this,e->{
			final FileDropperData data=(FileDropperData)e.getSource();
			loadFile(data.getFile());
			data.dragDropConsumed();
		});

		/* Hotkeys registrieren */
		final InputMap input=content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		input.put(KeyStroke.getKeyStroke('C',InputEvent.CTRL_DOWN_MASK),"cmdCopy");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_DOWN_MASK),"cmdCopy");
		input.put(KeyStroke.getKeyStroke('V',InputEvent.CTRL_DOWN_MASK),"cmdPaste");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.SHIFT_DOWN_MASK),"cmdPaste");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,0),"cmdUp");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0),"cmdUp");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,0),"cmdDown");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,0),"cmdDown");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK),"cmdUp");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK),"cmdDown");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"cmdPageUp");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"cmdPageDown");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0),"cmdPageUp");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0),"cmdPageDown");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,0),"cmdFromPrevious");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,InputEvent.CTRL_DOWN_MASK),"cmdLeft");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.CTRL_DOWN_MASK),"cmdRight");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"cmdPageLeft");
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"cmdPageRight");

		addAction(content,"cmdCopy",a->copyToClipboard());
		addAction(content,"cmdPaste",a->loadFromClipboard());
		addAction(content,"cmdUp",a->schedulePanel.changeCurrentValueDelta(1));
		addAction(content,"cmdDown",a->schedulePanel.changeCurrentValueDelta(-1));
		addAction(content,"cmdPageUp",a->schedulePanel.changeCurrentValueDelta(10));
		addAction(content,"cmdPageDown",a->schedulePanel.changeCurrentValueDelta(-10));
		addAction(content,"cmdFromPrevious",a->schedulePanel.changeCurrentValueToPreviousValue());
		addAction(content,"cmdLeft",a->schedulePanel.setStartPosition(schedulePanel.getStartPosition()-1));
		addAction(content,"cmdRight",a->schedulePanel.setStartPosition(schedulePanel.getStartPosition()+1));
		addAction(content,"cmdPageLeft",a->schedulePanel.setStartPosition(schedulePanel.getStartPosition()-Math.max(1,86400/durationPerSlot)));
		addAction(content,"cmdPageRight",a->schedulePanel.setStartPosition(schedulePanel.getStartPosition()+Math.max(1,86400/durationPerSlot)));

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(900,800);
		setLocationRelativeTo(getOwner());
		WindowSizeStorage.window(this,"schedules");
		setVisible(true);
	}

	/**
	 * Legt eine Aktion an und fügt diese in die {@link ActionMap} eines Panels ein.
	 * @param name	Name der Aktion
	 * @param action	Auszuführendes Callback beim Aufruf der Aktion
	 * @param panel	Panel in dem die Aktion registriert werden soll
	 */
	private void addAction(final JPanel panel, final String name, final Consumer<ActionEvent> action) {
		panel.getActionMap().put(name,new AbstractAction() {
			private static final long serialVersionUID=-7682578458732771324L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (action!=null) action.accept(e);
			}
		});
	}

	/**
	 * Erstellt eine neue Schaltfläche.
	 * @param name	Beschriftung (darf nicht <code>null</code> sein)
	 * @param tooltip	Tooltip (kann <code>null</code> oder leer sein)
	 * @param icon	Icon (darf nicht <code>null</code> sein)
	 * @param action	Auszuführende Aktion
	 * @return	Neue Schaltfläche
	 */
	private JButton getButton(final String name, final String tooltip, final Images icon, final BiConsumer<JButton, ActionEvent> action) {
		final JButton button=new JButton(name,icon.getIcon());
		if (tooltip!=null && !tooltip.isBlank()) button.setToolTipText(tooltip);
		if (action!=null) button.addActionListener(e->action.accept(button,e));
		return button;
	}

	/**
	 * Aktiviert oder deaktiviert {@link #buttonHome} und {@link #buttonLeft}
	 * in Abhängigkeit davon, welcher Balken gerade ganz links in dem Diagramm angezeigt wird.
	 * @see #buttonHome
	 * @see #buttonLeft
	 */
	private void enableButtons() {
		buttonHome.setEnabled(schedulePanel.getStartPosition()>0);
		buttonLeft.setEnabled(schedulePanel.getStartPosition()>0);
	}

	@Override
	protected void storeData() {
		originalSchedule.setDurationPerSlot(durationPerSlot);
		originalSchedule.setEditorMaxY(editorMaxY);
		originalSchedule.setRepeatMode(repeatMode);

		final List<Integer> data=schedulePanel.getData();
		while (data.size()>1 && data.get(data.size()-1)==0) data.remove(data.size()-1);
		originalSchedule.setSlots(data);
	}

	/**
	 * Zeigt den Dialog mit weiteren Einstellungen zu dem Zeitplan an.
	 */
	private void commandSettingsDialog() {
		final int neededMaxY=schedulePanel.getData().stream().mapToInt(Integer::intValue).max().orElseGet(()->0);
		final ScheduleTableModelSetupDialog dialog=new ScheduleTableModelSetupDialog(this,help,durationPerSlot,editorMaxY,neededMaxY,repeatMode);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		durationPerSlot=dialog.getDurationPerSlot();
		editorMaxY=dialog.getEditorMaxY();
		repeatMode=dialog.getRepeatMode();
		schedulePanel.setSetupData(editorMaxY,durationPerSlot);
	}

	/**
	 * Zeigt einen Dialog zur Auswahl des anzuzeigenden Bereichs an.
	 * @param parent	Button zur Ausrichtung des Popupmenüs
	 */
	private void commandRangeDialog(final JButton parent) {
		final int initialPosition=schedulePanel.getStartPosition();
		final int max=Math.max(initialPosition,schedulePanel.getLastNonNullSlot()-schedulePanel.getDisplaySlots());

		final JPopupMenu popup=new JPopupMenu();

		final JPanel panel1=new JPanel(new FlowLayout(FlowLayout.LEFT));
		popup.add(panel1);
		final JPanel panel2=new JPanel(new FlowLayout(FlowLayout.LEFT));
		popup.add(panel2);

		if (max==0) {
			panel1.add(new JLabel(Language.tr("Schedule.EditDialog.Range.FullRangeIsVisible")));
			panel2.setVisible(false);
		} else {
			final JSlider slider=new JSlider(0,max,initialPosition);
			panel1.add(slider);

			slider.setPaintTicks(true);
			slider.setMajorTickSpacing((int)Math.round(Math.max(1,Math.ceil((max-1)/5.0))));
			final int minor=(int)Math.round(Math.max(1,Math.ceil((max-1)/20.0)));
			slider.setMinorTickSpacing(minor);
			if (minor==1) slider.setSnapToTicks(true);

			final JLabel label=new JLabel();
			panel2.add(label);
			label.setText(TimeTools.formatLongTime(durationPerSlot*initialPosition)+" - "+TimeTools.formatLongTime(durationPerSlot*(initialPosition+schedulePanel.getDisplaySlots())));

			slider.addChangeListener(e->{
				final int pos=slider.getValue();
				schedulePanel.setStartPosition(pos);
				label.setText(TimeTools.formatLongTime(durationPerSlot*pos)+" - "+TimeTools.formatLongTime(durationPerSlot*(pos+schedulePanel.getDisplaySlots())));
			});
		}

		popup.show(parent,0,parent.getHeight());
	}

	/**
	 * Zeigt ein Popupmenü zum Importieren eines Zeitplans an.
	 * @param parent	Button zur Ausrichtung des Popupmenüs
	 */
	private void commandPopupLoad(final JButton parent) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Schedule.EditDialog.Load.Clipboard"),Images.EDIT_PASTE.getIcon()));
		item.addActionListener(e->loadFromClipboard());

		popup.add(item=new JMenuItem(Language.tr("Schedule.EditDialog.Load.File"),Images.GENERAL_SELECT_FILE.getIcon()));
		item.addActionListener(e->{
			final File file=Table.showLoadDialog(this,Language.tr("Schedule.EditDialog.Load.File"));
			if (file==null) return;
			loadFile(file);
		});

		popup.show(parent,0,parent.getHeight());
	}

	/**
	 * Versucht einen Zeitplan aus der Zwischenablage zu laden.
	 */
	private void loadFromClipboard() {
		final Transferable content=getToolkit().getSystemClipboard().getContents(this);
		if (content==null) return;
		String stringContent=null;
		try {stringContent=(String)content.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {stringContent=null;}
		if (stringContent==null) return;
		final Table table=new Table();
		table.load(stringContent);
		if (!schedulePanel.setDataFromTable(table)) {
			MsgBox.error(this,Language.tr("Schedule.EditDialog.Load.Clipboard.ErrorTitle"),Language.tr("Schedule.EditDialog.Load.Clipboard.ErrorInfo"));
			return;
		}
		editorMaxY=Math.max(editorMaxY,schedulePanel.getData().stream().mapToInt(Integer::intValue).max().orElse(0));
		schedulePanel.setSetupData(editorMaxY,durationPerSlot);
		enableButtons();
	}

	/**
	 * Lädt einen Zeitplan aus einer Datei.
	 * @param file	Zu ladende Datei
	 */
	private void loadFile(final File file) {
		final Table table=new Table();
		if (!table.load(file)) {
			MsgBox.error(this,Language.tr("Schedule.EditDialog.Load.File.ErrorTitle"),String.format(Language.tr("Schedule.EditDialog.Load.File.ErrorFileInfo"),file.toString()));
			return;
		}
		if (!schedulePanel.setDataFromTable(table)) {
			MsgBox.error(this,Language.tr("Schedule.EditDialog.Load.File.ErrorTitle"),String.format(Language.tr("Schedule.EditDialog.Load.File.ErrorDataInfo"),file.toString()));
			return;
		}
		editorMaxY=Math.max(editorMaxY,schedulePanel.getData().stream().mapToInt(Integer::intValue).max().orElse(0));
		schedulePanel.setSetupData(editorMaxY,durationPerSlot);
		enableButtons();
	}

	/**
	 * Zeigt ein Popupmenü zum Exportieren eines Zeitplans an.
	 * @param parent	Button zur Ausrichtung des Popupmenüs
	 */
	private void commandPopupSave(final JButton parent) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Schedule.EditDialog.Save.Clipboard"),Images.EDIT_COPY.getIcon()));
		item.addActionListener(e->copyToClipboard());

		popup.add(item=new JMenuItem(Language.tr("Schedule.EditDialog.Save.File"),Images.GENERAL_SAVE.getIcon()));
		item.addActionListener(e->{
			final File file=Table.showSaveDialog(this,Language.tr("Schedule.EditDialog.Save.File"));
			if (file==null) return;
			if (file.exists()) {if (!MsgBox.confirmOverwrite(this,file)) return;}
			if (!schedulePanel.getDataAsTable().save(file)) MsgBox.error(this,Language.tr("Schedule.EditDialog.Save.File.ErrorTitle"),String.format(Language.tr("Schedule.EditDialog.Save.File.ErrorInfo"),file.toString()));
		});

		popup.show(parent,0,parent.getHeight());
	}

	/**
	 * Kopiert den Zeitplan in die Zwischenablage.
	 */
	private void copyToClipboard() {
		getToolkit().getSystemClipboard().setContents(new StringSelection(schedulePanel.getDataAsTable().toString()),null);
	}
}